package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    // Our fake dataSource will be used to inject data into the SaveReminderViewModelTest
    private lateinit var dataSource: FakeDataSource

    // Subject under test: SaveReminderViewModelTest
    private lateinit var viewModel: SaveReminderViewModel

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

        // Create a new instance of the SaveReminderViewModelTest
        viewModel = SaveReminderViewModel(

            // Pass the application context
            ApplicationProvider.getApplicationContext(),

            // Pass the fake dataSource
            dataSource

        )

    }

    @Test
    fun whenIncompleteInfo_validationReturnsNull() {

        // GIVEN - incomplete reminder fields, title is null
        viewModel.onClear()
        viewModel.reminderTitle.value = null
        viewModel.reminderDescription.value = "some description"
        viewModel.reminderSelectedLocation.value = null
        viewModel.longitude.value = 10.0
        viewModel.latitude.value = 10.0

        // WHEN - attempting to validate
        val result = viewModel.validateEnteredData(
            ReminderDataItem(
                viewModel.reminderTitle.value,
                viewModel.reminderDescription.value,
                viewModel.reminderSelectedLocation.value,
                viewModel.longitude.value,
                viewModel.latitude.value,
                "someId"
            )
        )

        // THEN - result is false
        MatcherAssert.assertThat(result, Is.`is`(false))

    }

}
