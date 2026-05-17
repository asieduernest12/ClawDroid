package com.example.clawdroid.e2e

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.clawdroid.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FullUserJourneyTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun userCanNavigateToConfigAndSeeSettings() {
        onView(withText("Welcome to ClawDroid"))
            .check(matches(isDisplayed()))

        onView(withText("Settings"))
            .check(matches(isDisplayed()))
            .perform(click())

        onView(withText("PicoClaw Configuration"))
            .check(matches(isDisplayed()))
    }
}
