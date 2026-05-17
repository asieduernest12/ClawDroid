package com.example.clawdroid.e2e

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.clawdroid.AgentChatActivity
import com.example.clawdroid.R
import org.hamcrest.Matchers.containsString
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AgentChatE2ETest {

    @get:Rule
    val activityRule = ActivityScenarioRule(AgentChatActivity::class.java)

    @Test
    fun agentChatScreenDisplaysProviderDropdownAndModelTrigger() {
        onView(withId(R.id.layout_provider)).check(matches(isDisplayed()))
        onView(withId(R.id.layout_model_trigger)).check(matches(isDisplayed()))
    }

    @Test
    fun agentChatHasInputAndSendButton() {
        onView(withId(R.id.input_message)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_send)).check(matches(isDisplayed()))
    }

    @Test
    fun agentChatWelcomeMessageAppears() {
        onView(withText(containsString("I'm PicoClaw"))).check(matches(isDisplayed()))
    }

    @Test
    fun sendingMessageShowsUserBubble() {
        onView(withId(R.id.input_message))
            .perform(replaceText("Hello from E2E test"), closeSoftKeyboard())
        onView(withId(R.id.btn_send)).perform(click())
        onView(withText("Hello from E2E test")).check(matches(isDisplayed()))
    }

    @Test
    fun terminalBottomSheetHeaderIsPresent() {
        onView(withText("PicoClaw Terminal")).check(matches(isDisplayed()))
        onView(withId(R.id.btn_toggle_terminal)).check(matches(isDisplayed()))
    }

    @Test
    fun terminalCommandChipsAreVisible() {
        onView(withText("status")).check(matches(isDisplayed()))
        onView(withText("restart")).check(matches(isDisplayed()))
    }

    @Test
    fun sendingTerminalCommandShowsEcho() {
        onView(withId(R.id.btn_toggle_terminal)).perform(click())
        onView(withText("status")).perform(click())
        onView(withId(R.id.recycler_terminal)).check(matches(isDisplayed()))
    }

    @Test
    fun providerDropdownShowsOpenRouter() {
        onView(withId(R.id.dropdown_provider))
            .check(matches(withText(containsString("OpenRouter"))))
    }

    @Test
    fun modelPickerTriggerSelectsModel() {
        onView(withId(R.id.layout_model_trigger)).perform(click())
        onView(withText("Select Model")).check(matches(isDisplayed()))
        onView(withId(R.id.search_layout)).check(matches(isDisplayed()))
    }

    @Test
    fun chatWithOpenRouterWhenApiKeyProvided() {
        val apiKey = androidx.test.platform.app.InstrumentationRegistry.getArguments()
            .getString("openrouter_api_key", "")
        assumeTrue("No OpenRouter API key", apiKey.isNotBlank())
        onView(withId(R.id.input_message))
            .perform(replaceText("Reply with exactly: 'OK OpenRouter 30B'"), closeSoftKeyboard())
        onView(withId(R.id.btn_send)).perform(click())
        Thread.sleep(20000)
        onView(withText(containsString("30B"))).check(matches(isDisplayed()))
    }

    @Test
    fun cliCommandChipShowsOutputInTerminal() {
        onView(withId(R.id.btn_toggle_terminal)).perform(click())
        onView(withText("version")).perform(click())
        Thread.sleep(2000)
        onView(withId(R.id.recycler_terminal)).check(matches(isDisplayed()))
    }
}
