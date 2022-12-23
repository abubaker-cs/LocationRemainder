package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.DelayController
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsNot.not
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import kotlin.coroutines.ContinuationInterceptor

@Suppress("DEPRECATION")
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    // Subject under test
    private lateinit var remindersListViewModel: RemindersListViewModel

    // Our fake dataSource will be used to inject data into the RemindersListViewModel
    private lateinit var dataSource: FakeDataSource

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    /**
     * setupViewModel() is called before each test.
     */
    @Before
    fun setupViewModel() {

        // Stop Koin before each test
        stopKoin()

        // Create a new instance of the fake dataSource
        dataSource = FakeDataSource()

        // Create a new instance of the RemindersListViewModel
        remindersListViewModel = RemindersListViewModel(

            // Pass the application context
            ApplicationProvider.getApplicationContext(),

            // Pass the fake dataSource
            dataSource

        )
    }

    @Test
    fun check_loading() {

        // PAUSE DISPATCHER - pauseDispatcher() pauses the execution of coroutines
        (mainCoroutineRule.coroutineContext[ContinuationInterceptor]!! as DelayController).pauseDispatcher()

        // GIVEN - We are loading the list of reminders
        remindersListViewModel.loadReminders()

        // WHEN - Since we have already paused the dispatcher, the loading indicator should be true
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // RESUME DISPATCHER - resumeDispatcher() resumes the execution of coroutines
        mainCoroutineRule.resumeDispatcher()

        // Then - Since we have resumed the dispatcher, thus the loading indicator should be false
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))

    }

    @Test
    fun shouldReturnError() = runBlockingTest {

        // Delete all reminders from the fake dataSource
        dataSource.deleteAllReminders()

        // GIVEN - Since we already deleted them so make the dataSource (repository) return errors
        // by setting its value to ture
        dataSource.setReturnError(true)

        // WHEN - Try to load all reminders from the fake dataSource
        remindersListViewModel.loadReminders()

        // THEN - Display the error in the snackbar: "Couldn't retrieve reminders"
        val actual =
            remindersListViewModel.showSnackBar.getOrAwaitValue() == "Couldn't retrieve reminders"
        assertThat(actual, not(nullValue()))
    }

    @Test
    fun reminders_isEmpty() = runBlockingTest {

        // Delete all reminders from the fake dataSource
        dataSource.deleteAllReminders()

        // Load reminders from the fake dataSource
        remindersListViewModel.loadReminders()

        val actual = remindersListViewModel.showNoData.value

        // Check that the showNoData value is true
        MatcherAssert.assertThat(actual, CoreMatchers.`is`(true))

    }

}
