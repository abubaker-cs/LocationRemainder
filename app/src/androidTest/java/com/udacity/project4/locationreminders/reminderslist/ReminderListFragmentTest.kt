package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

/**
 * Target: ReminderListFragment
 */

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    private lateinit var appContext: Application

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        stopKoin()
        appContext = getApplicationContext()

        /**
         * use Koin Library as a service locator
         */
        val myModule = module {
            // Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }

            // Declare singleton definitions to be later injected using by inject()
            single {
                //This view model is declared singleton to be used across multiple fragments
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }

            // RemindersLocalRepository
            single { RemindersLocalRepository(get()) }

            /**
             * Note: Adding following line of code has resolved this issue:
             * single<ReminderDataSource> { get<RemindersLocalRepository>() }
             * =====================================================
             * Caused by: org.koin.core.error.NoBeanDefFoundException: No definition found for
             * 'com.udacity.project4.locationreminders.data.ReminderDataSource' has been found.
             * Check your module definitions.
             *
             * Reference:
             * https://stackoverflow.com/questions/57535800/koin-nobeandeffoundexception-check-your-module-definitions
             */

            // ReminderDataSource
            single<ReminderDataSource> { get<RemindersLocalRepository>() }

            single {
                Room.inMemoryDatabaseBuilder(
                    getApplicationContext(),
                    RemindersDatabase::class.java
                )
                    .allowMainThreadQueries()
                    .build()
                    .reminderDao()
            }
        }

        startKoin {
            androidContext(appContext)
            modules(listOf(myModule))
        }

    }

    // Passed test
    @Test
    fun clickTask_navigateToSaveReminderFragment() {
        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Click on the first list item
        onView(withId(R.id.addReminderFAB))
            .perform(click())


        // THEN - Verify that we navigate to the first detail screen
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    // Passed Test
    @Test
    fun reminderListFragment_DisplayedInUi() = runBlockingTest {

        val remindersDao: RemindersDao = get()

        val reminder = ReminderDTO(
            "Workout",
            "Visit gym for the legs workout",
            "Bahira Town",
            31.37150220702937,
            74.18466379217382,
        )


        remindersDao.saveReminder(reminder)

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(ViewMatchers.withText(reminder.title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withText(reminder.description)).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )
        onView(ViewMatchers.withText(reminder.location)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

    }

    // Passed test
    @Test
    fun deleteAllReminders_displayNoDataTextView() = runBlockingTest {

        val remindersDao: RemindersDao = get()

        val reminder = ReminderDTO(
            "Workout",
            "Visit gym for the biceps workout",
            "Bahira Town",
            31.37150220702937,
            74.18466379217382,
        )

        remindersDao.saveReminder(reminder)

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        remindersDao.deleteAllReminders()

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

    }
}
