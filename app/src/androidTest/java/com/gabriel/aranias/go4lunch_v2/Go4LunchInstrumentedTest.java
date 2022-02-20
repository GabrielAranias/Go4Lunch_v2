package com.gabriel.aranias.go4lunch_v2;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.view.Gravity;

import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.gabriel.aranias.go4lunch_v2.ui.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class Go4LunchInstrumentedTest {

    @Rule
    public ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void useAppContext() {
        // App context under test
        Context appContext = getInstrumentation().getTargetContext();
        assertEquals("com.gabriel.aranias.go4lunch_v2", appContext.getPackageName());
    }

    @Test
    public void checkThatMapFragmentIsInitiallyDisplayed() {
        onView(withId(R.id.nav_map)).check(matches(isDisplayed()));
        onView(withId(R.id.main_toolbar)).check(matches(hasDescendant(withText(R.string.nav_hungry))));
    }

    @Test
    public void checkThatFragmentIsDisplayedOnClick() {
        onView(withId(R.id.nav_list)).perform(click());
        onView(withId(R.id.nav_list)).check(matches(isDisplayed()));
    }

    @Test
    public void checkThatListIsDisplayedOnChipClick() {
        onView(withId(R.id.nav_list)).perform(click());
        onView(allOf(withId(R.id.list_place_group), isDisplayed())).perform(click());
        onView(withId(R.id.list_rv)).check(matches(isDisplayed()));
    }

    @Test
    public void checkThatSearchOptionIsFunctional() {
        onView(withId(R.id.action_search)).perform(click());
    }

    @Test
    public void checkThatNavigationDrawerIsDisplayedOnClick() {
        onView(withId(R.id.drawer_layout)).
                check(matches(isClosed(Gravity.START))).perform(DrawerActions.open());
    }

    // @Ignore if tests are run together due to logout
    @Test
    public void checkThatLogOutIsFunctional() {
        checkThatMapFragmentIsInitiallyDisplayed();
        checkThatNavigationDrawerIsDisplayedOnClick();
        onView(withId(R.id.nd_logout)).perform(click());
    }
}