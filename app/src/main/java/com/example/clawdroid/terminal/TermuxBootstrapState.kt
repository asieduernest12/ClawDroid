package com.example.clawdroid.terminal

sealed class TermuxBootstrapState {
    data object Uninitialized : TermuxBootstrapState()
    data object Checking : TermuxBootstrapState()
    data object Extracting : TermuxBootstrapState()
    data object Ready : TermuxBootstrapState()
    data class Error(val message: String) : TermuxBootstrapState()
}
