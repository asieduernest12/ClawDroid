package com.example.clawdroid.terminal

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import java.util.zip.ZipInputStream

class TermuxBootstrapManager(private val context: Context) {

    private val prefixDir: File get() = File(context.filesDir, TERMUX_PREFIX)

    val isExtracted: Boolean get() = File(prefixDir, "usr/bin/bash").exists()

    fun getBootstrapDir(): File = prefixDir

    private val _arch = detectArch()

    fun getEnv(): Map<String, String> {
        val prefix = prefixDir.absolutePath
        return mapOf(
            "PATH" to "${prefix}/usr/bin:${prefix}/usr/bin/applets:/system/bin:/system/xbin",
            "LD_LIBRARY_PATH" to "${prefix}/usr/lib",
            "HOME" to "${prefix}/home",
            "PREFIX" to "${prefix}/usr",
            "TMPDIR" to "${prefix}/tmp",
            "TERMUX_APP_PACKAGE" to context.packageName,
            "TERMUX_VERSION" to "0.118.0"
        )
    }

    fun install(): Flow<InstallProgress> = flow {
        if (isExtracted) {
            emit(InstallProgress.Completed)
            return@flow
        }

        emit(InstallProgress.Extracting)

        try {
            prefixDir.mkdirs()

            val extracted = extractFromAsset()
            if (!extracted) {
                extractFromNetwork()
            }

            File(prefixDir, "home").mkdirs()
            File(prefixDir, "tmp").mkdirs()
            ensureBinariesExecutable(File(prefixDir, "usr/bin"))

            emit(InstallProgress.Completed)
        } catch (e: Exception) {
            Log.e(TAG, "Bootstrap installation failed", e)
            emit(InstallProgress.Failed(e.message ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.IO)

    private fun extractFromAsset(): Boolean {
        val assetName = "termux/bootstrap/bootstrap-${_arch}.zip"
        return try {
            context.assets.open(assetName).use { input ->
                val zis = ZipInputStream(input.buffered())
                try {
                    var entry = zis.nextEntry
                    while (entry != null) {
                        val targetFile = File(prefixDir, entry.name)
                        if (entry.isDirectory) {
                            targetFile.mkdirs()
                        } else {
                            targetFile.parentFile?.mkdirs()
                            FileOutputStream(targetFile).use { output ->
                                zis.copyTo(output)
                            }
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                } finally {
                    zis.close()
                }
            }
            Log.d(TAG, "Extracted bootstrap from bundled asset ($assetName)")
            true
        } catch (e: Exception) {
            Log.w(TAG, "No bundled bootstrap asset for $_arch, falling back to network", e)
            false
        }
    }

    private fun extractFromNetwork() {
        val url = "$BOOTSTRAP_BASE_URL/bootstrap-${_arch}.zip"
        Log.d(TAG, "Downloading bootstrap from $url")
        val tempFile = File(context.cacheDir, "bootstrap-${System.nanoTime()}.zip")
        try {
            URL(url).openStream().use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            extractZip(tempFile)
            tempFile.delete()
        } catch (e: Exception) {
            tempFile.delete()
            throw e
        }
    }

    private fun extractZip(zipFile: File) {
        FileInputStream(zipFile).use { input ->
            val zis = ZipInputStream(input.buffered())
            try {
                var entry = zis.nextEntry
                while (entry != null) {
                    val targetFile = File(prefixDir, entry.name)
                    if (entry.isDirectory) {
                        targetFile.mkdirs()
                    } else {
                        targetFile.parentFile?.mkdirs()
                        FileOutputStream(targetFile).use { output ->
                            zis.copyTo(output)
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            } finally {
                zis.close()
            }
        }
    }

    private fun detectArch(): String {
        val arch = System.getProperty("os.arch")?.lowercase() ?: "aarch64"
        return when {
            arch.contains("aarch64") || arch.contains("arm64") -> "aarch64"
            arch.contains("x86_64") || arch.contains("amd64") -> "x86_64"
            arch.contains("arm") -> "arm"
            arch.contains("x86") || arch.contains("i686") -> "i686"
            else -> "aarch64"
        }
    }

    private fun ensureBinariesExecutable(binDir: File) {
        binDir.listFiles()?.forEach { file ->
            if (file.isFile && !file.canExecute()) {
                file.setExecutable(true)
            }
        }
    }

    sealed class InstallProgress {
        data object Extracting : InstallProgress()
        data object Completed : InstallProgress()
        data class Failed(val error: String) : InstallProgress()
    }

    companion object {
        private const val TAG = "TermuxBootstrapManager"
        private const val TERMUX_PREFIX = "termux"
private const val BOOTSTRAP_BASE_URL =
        "https://github.com/termux/termux-packages/releases/latest/download"
    }
}