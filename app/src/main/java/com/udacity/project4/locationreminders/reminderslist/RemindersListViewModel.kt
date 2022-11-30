package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.launch

class RemindersListViewModel(
    app: Application,
    private val dataSource: ReminderDataSource
) : BaseViewModel(app) {

    // list that holds the reminder data to be displayed on the UI
    val remindersList = MutableLiveData<List<ReminderDataItem>>()

    /**
     * Get all the reminders from the DataSource and add them to the remindersList to be shown on the UI,
     * or show error if any
     */
    fun loadReminders() {

        // Set the showLoading value to true
        showLoading.value = true

        // I am using coroutine to get the reminders from the database and add them to the observable list
        viewModelScope.launch {

            //interacting with the dataSource has to be through a coroutine
            val result = dataSource.getReminders()

            //
            showLoading.postValue(false)

            when (result) {

                // Success
                is Result.Success<*> -> {

                    // dataList is a list of ReminderDataItem objects
                    val dataList = ArrayList<ReminderDataItem>()

                    // Append all of the elements in the specified collection to the end of this list
                    dataList.addAll((result.data as List<ReminderDTO>).map { reminder ->

                        //map the reminder data from the DB to the be ready to be displayed on the UI
                        ReminderDataItem(

                            // Title
                            reminder.title,

                            // Description
                            reminder.description,

                            // Location
                            reminder.location,

                            // Reference for storing Geo-location information
                            reminder.latitude,
                            reminder.longitude,

                            // ID
                            reminder.id
                        )

                    })
                    remindersList.value = dataList
                }

                // Error: show error message
                is Result.Error ->
                    showSnackBar.value = result.message
            }

            //check if no data has to be shown
            invalidateShowNoData()
        }
    }

    /**
     * Inform the user that there's not any data if the remindersList is empty
     */
    private fun invalidateShowNoData() {
        showNoData.value = remindersList.value == null || remindersList.value!!.isEmpty()
    }
}
