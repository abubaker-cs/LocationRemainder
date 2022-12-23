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
    fun returnNull_onValidationFailure() {

        // Clear the live data objects
        viewModel.onClear()

        /**
         * GIVEN - User | Sample Data is incomplete
         */

        // Title
        viewModel.reminderTitle.value = "Do Laundry"

        // Description
        viewModel.reminderDescription.value = "Get laundry done at the weekend"

        // Location
        viewModel.reminderSelectedLocation.value = null // Set to null, so validation should fail

        // Latitude
        viewModel.longitude.value = 31.374418717270036

        // Longitude
        viewModel.latitude.value = 74.16895371705361


        /**
         * Validate sample data
         */

        // WHEN - attempting to validate
        val result = viewModel.validateEnteredData(
            ReminderDataItem(

                // Title
                viewModel.reminderTitle.value,

                // Description
                viewModel.reminderDescription.value,

                // Location
                viewModel.reminderSelectedLocation.value,

                // Longitude
                viewModel.longitude.value,

                // Latitude
                viewModel.latitude.value,

                // ID
                "dummyId"
            )
        )

        // THEN - result is false
        MatcherAssert.assertThat(result, Is.`is`(false))

    }

}
