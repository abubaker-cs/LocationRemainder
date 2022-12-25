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
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

/**
 * Target: RemindersActivity
 */

@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityTest : AutoCloseKoinTest() {

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * An espresso idling resource implementation that reports idle status for all data binding
     * layouts. Data Binding uses a mechanism to post messages which Espresso doesn't track yet.
     *
     * Since this application only uses fragments, the resource only checks the fragments and their
     * children instead of the whole view tree.
     */
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    // Execute this init() code before each test
    @Before
    fun init() {

        // Stop the previous instance of Koin
        stopKoin()

        // Get the context of the application
        appContext = getApplicationContext()

        val myModule = module {

            // RemindersListViewModel
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }

            // SaveReminderViewModel
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }

            // dataSource: RemindersLocalRepository
            single {
                RemindersLocalRepository(get())
            }

            // ReminderDataSource
            single<ReminderDataSource> { get<RemindersLocalRepository>() }

            // DAO
            single {
                LocalDB.createRemindersDao(appContext)
            }

        }

        // Start Koin with the recently defined modules
        startKoin {
            modules(listOf(myModule))
        }

        // Get the repository
        // val repository: ReminderDataSource by inject()
        repository = get()

        // Clear the data to start fresh
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
    // Passed Test
    @Test
    fun saveReminder_displayReminder() = runBlocking {

        // Start and Monitory the RemindersActivity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // + FAB: click on the FAB to add a new reminder from teh RemindersListFragment
        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        // Confirm that we are on the SaveReminderFragment by using @+id/reminderTitle
        onView(withId(R.id.reminderTitle)).check(ViewAssertions.matches(isDisplayed()))

        onView(withId(R.id.reminderTitle)).perform(ViewActions.replaceText("New title"))
        onView(withId(R.id.reminderDescription)).perform(ViewActions.replaceText("New description"))
        onView(withId(R.id.selectLocation)).perform(ViewActions.click())
        onView(withId(R.id.map)).perform(ViewActions.longClick())
        onView(withId(R.id.save_button)).perform(ViewActions.click())
        onView(withId(R.id.saveReminder)).perform(ViewActions.click())
        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(
            ViewAssertions.matches(
                withText(R.string.geofences_added)
            )
        )

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
    // Passed test
    @Test
    fun show_toast_message() {

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // + FAB: click on the FAB to add a new reminder from the RemindersListFragment
        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        // Confirm that we are on the SaveReminderFragment by using @+id/reminderTitle
        onView(withId(R.id.reminderTitle))
            .perform(ViewActions.replaceText("Test Title"))

        // Add "Test Description" to the @+id/reminderDescription
        onView(withId(R.id.reminderDescription))
            .perform(ViewActions.replaceText("Test Description"))

        // sleep | wait for 2.5 seconds
        Thread.sleep(2500)

        // Click on the @+id/selectLocation to select a geo location
        onView(withId(R.id.selectLocation)).perform(ViewActions.click())

        // Click on the map to select a geo location
        onView(withId(R.id.map)).perform(ViewActions.click())

        // sleep | wait for 2.5 seconds
        Thread.sleep(2500)

        // Click on the @+id/save_button to save the geo location
        onView(withId(R.id.save_button)).perform(ViewActions.click())

        // saveReminder | click on the @+id/saveReminder to save the reminder
        onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        // Check if the Toast message "Reminder Saved!" is displayed
        onView(withText(R.string.reminder_saved)).inRoot(
            RootMatchers.withDecorView(
                CoreMatchers.not(
                    Is.`is`(
                        getActivity(activityScenario)!!.window.decorView
                    )
                )
            )
        ).check(ViewAssertions.matches(isDisplayed()))

        // Make sure the activity is closed
        activityScenario.close()

    }

    /**
     * Add a new reminder and check that it is displayed on the screen using the Snackbar message
     */
    // Failed test
    @Test
    fun show_Snackbar_message() {

        // Start and Monitory the RemindersActivity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // + FAB: click on the FAB to add a new reminder from the RemindersListFragment
        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())


        onView(withId(R.id.reminderDescription))
            .perform(ViewActions.replaceText("Test Description"))

        // sleep | wait for 2.5 seconds
        Thread.sleep(2500)

        // Click on the @+id/selectLocation to select a geo location
        onView(withId(R.id.selectLocation)).perform(ViewActions.click())

        // Click on the map to select a geo location
        onView(withId(R.id.map)).perform(ViewActions.click())

        // sleep | wait for 2.5 seconds
        Thread.sleep(2500)

        // Click on the @+id/save_button to save the geo location
        onView(withId(R.id.save_button)).perform(ViewActions.click())

        // saveReminder | click on the @+id/saveReminder to save the reminder
        onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        // Check if the Snackbar message "Please make sure you\'ve selected a location and added
        // title and description before saving" is displayed
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.err_enter_title)))

        // Make sure the activity is closed
        activityScenario.close()

    }

}
