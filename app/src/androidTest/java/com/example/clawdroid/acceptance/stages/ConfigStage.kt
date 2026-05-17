package com.example.clawdroid.acceptance.stages

import android.content.Context
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.clawdroid.R
import com.example.clawdroid.config.ConfigRepository
import com.example.clawdroid.config.model.PicoClawConfig
import org.hamcrest.Matchers.allOf

object ConfigStage {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val repo = ConfigRepository(context)

    fun configTitleIsDisplayed() {
        onView(withText("PicoClaw Configuration"))
            .check(matches(isDisplayed()))
    }

    fun defaultPortIsDisplayed() {
        onView(withId(R.id.server_port_input))
            .check(matches(withText("8080")))
    }

    fun changePortTo(port: Int) {
        onView(withId(R.id.server_port_input))
            .perform(clearText(), typeText(port.toString()))
    }

    fun clickSave() {
        onView(withText("Save")).perform(click())
    }

    fun waitForSavedIndicator() {
        onView(withText("Configuration saved"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
    }

    fun clickReset() {
        onView(withText("Reset Defaults")).perform(click())
    }

    fun validationErrorIsDisplayed(field: String) {
        onView(withText("$field cannot be empty"))
            .check(matches(isDisplayed()))
    }

    fun portValidationErrorIsDisplayed() {
        onView(allOf(
            isDescendantOfA(withId(R.id.server_port_layout)),
            withText("Port must be between 1024 and 65535")
        )).check(matches(isDisplayed()))
    }

    fun loadConfig(): PicoClawConfig = repo.loadConfig()

    fun saveConfig(config: PicoClawConfig) {
        repo.saveConfig(config)
    }

    fun resetConfig() {
        repo.resetToDefaults()
    }
}
