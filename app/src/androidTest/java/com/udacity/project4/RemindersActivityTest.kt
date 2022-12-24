package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.core.Is
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject

/**
 * Target: RemindersActivity
 */

@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityTest : KoinTest {

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @Before
    fun init() {

        //stop the original app koin
        stopKoin()

        appContext = getApplicationContext()

        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) }
            single { LocalDB.createRemindersDao(appContext) }
        }

        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }

        //Get our real repository
        val repository: ReminderDataSource by inject()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    /**
     * Protection against memory leaks and delete all reminders after each test
     */
    @After
    fun unregisterIdlingResource() = runBlocking {

        // Unregister your idling resource so it can be garbage collected and does not leak any memory.
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)

        // clear the data to start fresh
        repository.deleteAllReminders()

    }

    /**
     * A helper function to get the activity from the activityScenario
     */
    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {

        // Create a new variable to hold the activity
        var activity: Activity? = null

        //  Use the activityScenario variable to call the onActivity method
        activityScenario.onActivity {

            // Set the activity variable to the activity in the onActivity lambda
            activity = it

        }

        // Return the activity
        return activity
    }

    /**
     * Add a new reminder
     */
    @Test
    fun saveReminder_displayReminder() = runBlocking {

        // start the reminders screen
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        // Monitor the activity
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // + FAB: click on the FAB to add a new reminder from teh RemindersListFragment
        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        // Confirm that we are on the SaveReminderFragment by using @+id/reminderTitle
        onView(withId(R.id.reminderTitle)).check(ViewAssertions.matches(isDisplayed()))

//        onView(withId(R.id.reminderTitle)).perform(ViewActions.replaceText("New title"))
//        onView(withId(R.id.reminderDescription)).perform(ViewActions.replaceText("New description"))
//        onView(withId(R.id.selectLocation)).perform(ViewActions.click())
//        onView(withId(R.id.map)).perform(ViewActions.longClick())
//        onView(withId(R.id.save_button)).perform(ViewActions.click())
//        onView(withId(R.id.saveReminder)).perform(ViewActions.click())
//        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
//        onView(withId(com.google.android.material.R.id.snackbar_text)).check(ViewAssertions.matches(withText(R.string.geofences_added)))

        // Make sure the activity is closed
        // ================================
        // 1. Finishes the managed activity and cleans up device's state.
        // 2. It is highly recommended to call this method after you test is done to keep the device
        //    state clean although this is optional.
        activityScenario.close()

    }

    /**
     * Add a new reminder and check that it is displayed on the screen using the Toast message
     */
    @Test
    fun show_toast_message() {

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        onView(withId(R.id.reminderTitle))
            .perform(ViewActions.replaceText("Test Title"))

        onView(withId(R.id.reminderDescription))
            .perform(ViewActions.replaceText("Test Description"))

        Thread.sleep(1000)

        onView(withId(R.id.selectLocation)).perform(ViewActions.click())
        onView(withId(R.id.map)).perform(ViewActions.click())

        Thread.sleep(3000)

        onView(withId(R.id.save_button)).perform(ViewActions.click())

        onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        onView(withText(R.string.reminder_saved)).inRoot(
            RootMatchers.withDecorView(
                CoreMatchers.not(
                    Is.`is`(
                        getActivity(activityScenario)!!.window.decorView
                    )
                )
            )
        ).check(ViewAssertions.matches(isDisplayed()))

        activityScenario.close()
    }

    /**
     * Add a new reminder and check that it is displayed on the screen using the Snackbar message
     */
    @Test
    fun show_Snackbar_message() {

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        onView(withId(R.id.reminderDescription))
            .perform(ViewActions.replaceText("Test Description"))

        Thread.sleep(1000)

        onView(withId(R.id.selectLocation)).perform(ViewActions.click())
        onView(withId(R.id.map)).perform(ViewActions.click())

        Thread.sleep(3000)

        onView(withId(R.id.save_button)).perform(ViewActions.click())
        onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.save_reminder_error_desc)))
        activityScenario.close()
    }

}
