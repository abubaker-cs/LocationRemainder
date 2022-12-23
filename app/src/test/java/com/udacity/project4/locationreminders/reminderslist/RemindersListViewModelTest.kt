package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.DelayController
import kotlinx.coroutines.test.resumeDispatcher
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

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    // Our fake dataSource will be used to inject data into the RemindersListViewModel
    private lateinit var dataSource: FakeDataSource

    // Subject under test: RemindersListViewModel
    private lateinit var viewModel: RemindersListViewModel

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
        viewModel = RemindersListViewModel(

            // Pass the application context
            ApplicationProvider.getApplicationContext(),

            // Pass the fake dataSource
            dataSource

        )
    }

    @Suppress("DEPRECATION")
    @Test
    fun check_loading() {

        // PAUSE DISPATCHER - pauseDispatcher() pauses the execution of coroutines
        (mainCoroutineRule.coroutineContext[ContinuationInterceptor]!! as DelayController).pauseDispatcher()

        // GIVEN - We are loading the list of reminders
        viewModel.loadReminders()

        // WHEN - Since we have already paused the dispatcher, the loading indicator should be true
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        // RESUME DISPATCHER - resumeDispatcher() resumes the execution of coroutines
        mainCoroutineRule.resumeDispatcher()

        // Then - Since we have resumed the dispatcher, thus the loading indicator should be false
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))

    }

    @Suppress("DEPRECATION")
    @Test
    fun shouldReturn_Error() = runBlockingTest {

        // Delete all reminders from the fake dataSource
        dataSource.deleteAllReminders()

        // GIVEN - Since we already deleted them so make the dataSource (repository) return errors
        // by setting its value to ture
        dataSource.setReturnError(true)

        // WHEN - Try to load all reminders from the fake dataSource
        viewModel.loadReminders()

        // THEN - Display the error in the snackbar: "Couldn't retrieve reminders"
        val actual =
            viewModel.showSnackBar.getOrAwaitValue() == "Couldn't retrieve reminders"
        assertThat(actual, not(nullValue()))
    }

    @Test
    fun reminders_isEmpty() = runBlockingTest {

        // Delete all reminders from the fake dataSource
        dataSource.deleteAllReminders()

        // Load reminders from the fake dataSource
        viewModel.loadReminders()

        val actual = viewModel.showNoData.value

        // Check that the showNoData value is true
        MatcherAssert.assertThat(actual, CoreMatchers.`is`(true))

    }

}
