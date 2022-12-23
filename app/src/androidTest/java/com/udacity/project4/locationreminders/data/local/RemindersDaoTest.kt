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

// Small Test to test the RemindersDao
@SmallTest
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RemindersDaoTest {

    // DONE: Add testing implementation to the RemindersDao.kt
    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

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
    fun insertReminderAndGetById() = runBlockingTest {

        // GIVEN - insert a reminder
        val reminder = ReminderDTO(
            "title",
            "description",
            "somewhere",
            12.0,
            12.0,
            "random"
        )

        database.reminderDao().saveReminder(reminder)

        // WHEN - Get the reminder by id from the database
        val loaded = database.reminderDao().getReminderById(reminder.id)

        // THEN - The loaded data contains the expected values
        assertThat(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

}
