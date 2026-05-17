package com.example.clawdroid.acceptance.stages

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import com.example.clawdroid.MainActivity
import com.example.clawdroid.terminal.TermuxBootstrapState

object AppStage {

    private val context: Context = ApplicationProvider.getApplicationContext()

    fun launchApp(): MainActivity {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return MainActivity()
    }

    fun welcomeTextIsDisplayed() {
        onView(withText("ClawDroid"))
            .check(matches(isDisplayed()))
    }

    fun settingsButtonIsDisplayed() {
        onView(withText("SETTINGS"))
            .check(matches(isDisplayed()))
    }

    fun clickSettings() {
        onView(withText("SETTINGS")).perform(click())
    }

    fun configScreenIsDisplayed() {
        onView(withText("PicoClaw Configuration"))
            .check(matches(isDisplayed()))
    }

    fun bootstrapStateIsReady(): Boolean {
        val app = context.applicationContext
            as com.example.clawdroid.App
        return app.bootstrapState.value is TermuxBootstrapState.Ready
    }

    fun waitForBootstrap(timeoutMs: Long = 120000): Boolean {
        val app = context.applicationContext
            as com.example.clawdroid.App
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (app.bootstrapState.value is TermuxBootstrapState.Ready) {
                return true
            }
            if (app.bootstrapState.value is TermuxBootstrapState.Error) {
                return false
            }
            Thread.sleep(500)
        }
        return app.bootstrapState.value is TermuxBootstrapState.Ready
    }
}
