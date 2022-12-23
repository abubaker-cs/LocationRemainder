package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

// FakeDataSource will act as a test double for the LocalDataSource
class FakeDataSource : ReminderDataSource {

    // reminderServiceData will be used to save reminders in the fake data source
    private var remindersServiceData: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()

    // Default false value for the shouldReturnError variable
    private var shouldReturnError = false

    // This function will update the value for the shouldReturnError variable
    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    // getReminder() = This function will return a reminder from the fake data source
    override suspend fun getReminder(id: String): Result<ReminderDTO> {

        // If an error has been found while retrieving the reminders, then return an error message
        if (shouldReturnError) {
            return Result.Error("Test exception ")
        }

        // SUCCESS - Upon success, return the outcome message
        remindersServiceData[id]?.let {
            return Result.Success(it)
        }

        // ERROR - Return an error message: "Could not find reminder"
        return Result.Error("Could not find reminder")
    }

    // getReminders() = This function will return the list of reminders from the fake data source
    override suspend fun getReminders(): Result<List<ReminderDTO>> {

        // If an error has been found while retrieving the reminders, then return an error message
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }

        // Otherwise, return the list of reminders
        return Result.Success(remindersServiceData.values.toList())

    }

    // saveReminder() = This function will save a reminder in the fake data source
    override suspend fun saveReminder(reminder: ReminderDTO) {

        // Add reminder to the remindersServiceData (fake data source)
        remindersServiceData.values.add(reminder)

    }

    // deleteAllReminders() = This function will delete all reminders from the fake data source
    override suspend fun deleteAllReminders() {

        // Clear the data
        remindersServiceData.clear()

    }

}
