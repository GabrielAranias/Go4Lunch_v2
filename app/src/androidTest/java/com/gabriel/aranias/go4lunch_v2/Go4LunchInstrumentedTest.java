package com.gabriel.aranias.go4lunch_v2;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.view.Gravity;

import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.gabriel.aranias.go4lunch_v2.ui.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class Go4LunchInstrumentedTest {

    @Rule
    public ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void useAppContext() {
        // App context under test
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.gabriel.aranias.go4lunch_v2", appContext.getPackageName());
    }

    @Test
    public void checkThatMapFragmentIsDisplayed() {
        onView(withId(R.id.nav_map)).perform(click());
        onView(withId(R.id.map)).check(matches(isDisplayed()));
        onView(withId(R.id.main_toolbar)).check(matches(hasDescendant(withText(R.string.nav_hungry))));
    }

    @Test
    public void checkThatListFragmentIsDisplayed() {
        onView(withId(R.id.nav_list)).perform(click());
        onView(withId(R.id.list_rv)).check(matches(isDisplayed()));
        onView(withId(R.id.main_toolbar)).check(matches(hasDescendant(withText(R.string.nav_hungry))));
    }

    @Test
    public void checkThatWorkmateFragmentIsDisplayed() {
        onView(withId(R.id.nav_workmates)).perform(click());
        onView(withId(R.id.workmate_rv)).check(matches(isDisplayed()));
        onView(withId(R.id.main_toolbar)).check(matches(hasDescendant(withText(R.string.nav_workmates))));
    }

    @Test
    public void checkThatChatFragmentIsDisplayed() {
        onView(withId(R.id.nav_chat)).perform(click());
        onView(withId(R.id.chat_list_rv)).check(matches(isDisplayed()));
        onView(withId(R.id.main_toolbar)).check(matches(hasDescendant(withText(R.string.nav_chat))));
    }

    @Test
    public void checkThatSettingActivityIsDisplayed() {
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.START))).perform(DrawerActions.open());
        onView(withId(R.id.nd_settings)).perform(click());
        onView(withId(R.id.setting_update_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.setting_switch_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.setting_delete_btn)).check(matches(isDisplayed()));
    }

    @Test
    public void checkThatDetailActivityIsDisplayed() {
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.START))).perform(DrawerActions.open());
        onView(withId(R.id.nd_your_lunch)).perform(click());
        onView(withId(R.id.detail_appbar_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.detail_lunch_spot_fab)).check(matches(isDisplayed()));
        onView(withId(R.id.detail_content)).check(matches(isDisplayed()));
    }
}