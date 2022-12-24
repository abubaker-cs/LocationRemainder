package com.udacity.project4.locationreminders.data.local

import androidx.room.Room
import androidx.test.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Target: RemindersLocalRepository.kt
 */

@MediumTest
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RemindersLocalRepositoryTest {

    // DONE: Add testing implementation to the RemindersLocalRepository.kt
    private lateinit var database: RemindersDatabase
    private lateinit var dao: RemindersDao
    private lateinit var repository: RemindersLocalRepository

    @Suppress("DEPRECATION")
    @Before
    fun init() {

        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(

            // Context used to open or create the database.
            InstrumentationRegistry.getInstrumentation().context,

            // The RemindersDatabase class.
            RemindersDatabase::class.java

        )

            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()

            // Build the database.
            .build()

        // Get a reference to the DAO
        dao = database.reminderDao()

        // Get a reference to the repository
        repository = RemindersLocalRepository(dao)

    }

    @After
    fun closeDb() {

        // Close the database
        database.close()

    }

    @Test
    fun dataNotFound_errorMessageDisplayed() = runBlocking {

        val reminder = ReminderDTO(
            "Workout",
            "Visit gym for the biceps workout",
            "Bahira Town",
            31.37150220702937,
            74.18466379217382,
            "workout2"
        )

        val result = (repository.getReminder(reminder.id) as Result.Error).message

        assertThat(result, `is`("Reminder not found!"))

    }

    @Test
    fun saveReminder_matchesRepository() = runBlocking {

        // GIVEN - insert new reminders in the database
        val legsDay = ReminderDTO(
            "Workout",
            "Visit gym for the legs workout",
            "Bahira Town",
            31.37150220702937,
            74.18466379217382,
            "workout1"
        )

        val bicepsDay = ReminderDTO(
            "Workout",
            "Visit gym for the biceps workout",
            "Bahira Town",
            31.37150220702937,
            74.18466379217382,
            "workout2"
        )

        val tricepsDay = ReminderDTO(
            "Workout",
            "Visit gym for the triceps workout",
            "Bahira Town",
            31.37150220702937,
            74.18466379217382,
            "workout3"
        )

        // Save the reminders
        database.reminderDao().saveReminder(legsDay)
        database.reminderDao().saveReminder(bicepsDay)
        database.reminderDao().saveReminder(tricepsDay)

        // Sort the reminders by their ids in ascending order
        val remindersList = listOf(legsDay, bicepsDay, tricepsDay).sortedBy {
            it.id
        }

        // WHEN - Get all the reminders from the database
        val reminders = database.reminderDao().getReminders()

        val sortedListByID = reminders.sortedBy { it.id }

        // Search for the reminder with id "fake", this should return an Error
        val reminder = repository.getReminder("fake") as Result.Error

        assertThat(reminder.message, `is`("Reminder not found!"))
        assertThat(sortedListByID[0].id, `is`(remindersList[0].id))
        assertThat(sortedListByID[1].id, `is`(remindersList[1].id))
        assertThat(sortedListByID[2].id, `is`(remindersList[2].id))
    }

    @Test
    fun deleteAllReminders_checkIsEmpty() = runBlocking {

        val reminder = ReminderDTO(
            "title",
            "dec",
            "loc",
            0.0,
            0.0
        )

        repository.saveReminder(reminder)

        repository.deleteAllReminders()

        val repo = (repository.getReminders() as Result.Success).data

        assertThat(repo, `is`(emptyList()))
    }
}
