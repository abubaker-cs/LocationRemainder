package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
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
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    // DONE: test the navigation of the fragments.


    // DONE: test the displayed data on the UI.


    // DONE: add testing for the error messages.
    private lateinit var dataSource: ReminderDataSource
    private lateinit var appContext: Application

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        stopKoin()
        appContext = getApplicationContext()

        /**
         * use Koin Library as a service locator
         */
        val myModule = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                RemindersListViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            //Declare singleton definitions to be later injected using by inject()
            single {
                //This view model is declared singleton to be used across multiple fragments
                SaveReminderViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) }
            single { LocalDB.createRemindersDao(appContext) }
        }

        startKoin {
            androidContext(appContext)
            modules(listOf(myModule))
        }

        // TODO - get() jUnit
        // remindersRepository = get()
        dataSource = RemindersLocalRepository(LocalDB.createRemindersDao(appContext))


        runBlocking {
            dataSource.deleteAllReminders()
        }
    }

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

    @Test
    fun reminderIsShownInRecyclerView() {
        runBlocking {
            // GIVEN - one reminder
            val reminder1 = ReminderDTO(
                "title1",
                "description1",
                "somewhere1",
                11.0,
                11.0,
                "random1"
            )
            dataSource.saveReminder(reminder1)

            // WHEN - ReminderListFragment is displayed
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

            // THEN - the reminder is displayed
            onView(ViewMatchers.withText(reminder1.title)).check(
                ViewAssertions.matches(
                    ViewMatchers.isDisplayed()
                )
            )

            onView(ViewMatchers.withText(reminder1.description)).check(
                ViewAssertions.matches(
                    ViewMatchers.isDisplayed()
                )
            )

            onView(ViewMatchers.withText(reminder1.location)).check(
                ViewAssertions.matches(
                    ViewMatchers.isDisplayed()
                )
            )

        }
    }
}
