package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Target: RemindersDao.kt
 */

@SmallTest
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {

        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(

            // Context used to open or create the database.
            ApplicationProvider.getApplicationContext(),

            // The RemindersDatabase class.
            RemindersDatabase::class.java

        )
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()

            // Build the database.
            .build()
    }

    @After
    fun closeDb() {

        // Close the database
        database.close()

    }

    @Suppress("DEPRECATION")
    @Test
    fun saveAndDeleteAllReminder_checkIsEmpty() = runBlockingTest {

        val legsDay = ReminderDTO(
            "Workout",
            "Visit gym for the legs workout",
            "Bahira Town",
            31.37150220702937,
            74.18466379217382,
        )

        val bicepsDay = ReminderDTO(
            "Workout",
            "Visit gym for the biceps workout",
            "Bahira Town",
            31.37150220702937,
            74.18466379217382,
        )

        val tricepsDay = ReminderDTO(
            "Workout",
            "Visit gym for the triceps workout",
            "Bahira Town",
            31.37150220702937,
            74.18466379217382,
        )

        // Save the reminders
        database.reminderDao().saveReminder(legsDay)
        database.reminderDao().saveReminder(bicepsDay)
        database.reminderDao().saveReminder(tricepsDay)

        // Delete all reminders
        database.reminderDao().deleteAllReminders()

        // Get all reminders
        val reminders = database.reminderDao().getReminders()

        // Check if the retrieved list is empty
        assertThat(reminders, `is`(emptyList()))

    }

    @Suppress("DEPRECATION")
    @Test
    fun saveReminder_getById() = runBlockingTest {

        // GIVEN - insert a sample reminder
        val reminder = ReminderDTO(
            "Workout",
            "Visit gym for the legs workout",
            "Bahira Town",
            31.37150220702937,
            74.18466379217382,
            "random"
        )

        // Save the reminder
        database.reminderDao().saveReminder(reminder)

        // WHEN - Get the reminder by id from the database
        val loaded = database.reminderDao().getReminderById(reminder.id)

        // THEN - The loaded data contains the expected values
        assertThat(loaded as ReminderDTO, notNullValue())

        // ID
        assertThat(loaded.id, `is`(reminder.id))

        // Title
        assertThat(loaded.title, `is`(reminder.title))

        // Description
        assertThat(loaded.description, `is`(reminder.description))

        // Location
        assertThat(loaded.latitude, `is`(reminder.latitude))

        // Latitude
        assertThat(loaded.longitude, `is`(reminder.longitude))

    }

}

