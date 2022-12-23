package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.DelayController
import org.hamcrest.CoreMatchers
import org.hamcrest.core.Is
import org.hamcrest.core.IsNot
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import kotlin.coroutines.ContinuationInterceptor

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //DONE: provide testing to the RemindersListViewModel and its live data objects
    //Completed: provide testing to the RemindersListViewModel and its live data objects

    // Subject under test
    private lateinit var remindersListViewModel: RemindersListViewModel

    // Use a fake repository to be injected into the view model.
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
        stopKoin()
        // Initialise the repository with no reminders.
        dataSource = FakeDataSource()

        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(), dataSource
        )
    }

    @Test
    fun loadReminders_loading() {

        // GIVEN - we are loading reminders
        (mainCoroutineRule.coroutineContext[ContinuationInterceptor]!! as DelayController).pauseDispatcher()
        remindersListViewModel.loadReminders()

        // WHEN - the dispatcher is paused, showLoading is true
        Assert.assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), Is.`is`(true))
        (mainCoroutineRule.coroutineContext[ContinuationInterceptor]!! as DelayController).resumeDispatcher()

        // THEN - when the dispatcher is resumed, showloading is false
        Assert.assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), Is.`is`(false))

    }

    @Test
    fun loadRemindersWhenUnavailable_causesError() {
        // GIVEN - there's a problem loading reminders
        // Make the repository return errors
        dataSource.setReturnError(true)

        // WHEN - we want to load rhe reminders
        remindersListViewModel.loadReminders()

        // THEN - It's an error, there's a snackbar
        Assert.assertThat(
            remindersListViewModel.showSnackBar.getOrAwaitValue(),
            IsNot.not(CoreMatchers.nullValue())
        )
    }

}
