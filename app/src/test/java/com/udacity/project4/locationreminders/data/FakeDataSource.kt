package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    // DONE: Create a fake data source to act as a double to the real data source
    private var remindersServiceData: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {

        // ERROR
        // shouldReturnError is a boolean flag that can be set to true to force the
        // getReminders() method to return an error.
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }

        // Return the reminders
        return Result.Success(remindersServiceData.values.toList())

    }

    override suspend fun saveReminder(reminder: ReminderDTO) {

        // Add reminder to the remindersServiceData
        remindersServiceData.values.add(reminder)

    }

    // getRemainder() returns a Result object. If the Result object is a success, then
    // the reminder is returned. If the Result object is an error, then the error is
    // returned.
    override suspend fun getReminder(id: String): Result<ReminderDTO> {

        // shouldReturnError is a boolean flag that can be set to true to force the
        // getReminders() method to return an error.
        if (shouldReturnError) {
            return Result.Error("Test exception ")
        }

        // SUCCESS - Check if the reminder is in the list
        remindersServiceData[id]?.let {
            return Result.Success(it)
        }

        // ERROR - Return an error
        return Result.Error("Could not find reminder")
    }

    override suspend fun deleteAllReminders() {

        // Clear the data to avoid test pollution.
        remindersServiceData.clear()

    }

}
