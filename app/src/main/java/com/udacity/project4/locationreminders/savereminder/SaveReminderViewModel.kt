package com.udacity.project4.locationreminders.savereminder

import android.app.Application
//  import android.os.Build.VERSION_CODES.R
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch

class SaveReminderViewModel(app: Application, val dataSource: ReminderDataSource) :
    BaseViewModel(app) {

    // Title
    val reminderTitle = MutableLiveData<String?>()

    // Description
    val reminderDescription = MutableLiveData<String?>()

    // Selected Location
    val reminderSelectedLocation = MutableLiveData<String?>()

    // These variables will be used to save the selected location's geo-coordinates
    val latitude = MutableLiveData<Double?>()
    val longitude = MutableLiveData<Double?>()

    /**
     * This will clear the live data objects
     */
    fun onClear() {

        // Title
        reminderTitle.value = null

        // Description
        reminderDescription.value = null

        // Location
        reminderSelectedLocation.value = null

        // Geo-Coordinates
        latitude.value = null
        longitude.value = null
    }

    /**
     * Validate and save the user provided data
     */
    fun validateAndSaveReminder(reminderData: ReminderDataItem) {

        if (validateEnteredData(reminderData)) {

            //  SAve the reminder data to the DataSource
            saveReminder(reminderData)

        }

    }


    /**
     * Save the reminder to the data source
     */
    fun saveReminder(reminderData: ReminderDataItem) {

        // showLoading is used to show a loading spinner
        showLoading.value = true

        // Coroutines are used to perform background operations
        viewModelScope.launch {

            //  Save the reminder data to the DataSource
            dataSource.saveReminder(

                ReminderDTO(

                    // Title
                    reminderData.title,

                    // Description
                    reminderData.description,

                    // Location
                    reminderData.location,

                    // Latitude
                    reminderData.latitude,

                    // Longitude
                    reminderData.longitude,

                    // ID
                    reminderData.id

                )

            )

            // This will stop the loading spinner
            showLoading.value = false

            // Toast Message: "Reminder Saved!"
            showToast.value = "Reminder Saved!"

            // Navigate back to the reminders list screen
            navigationCommand.value = NavigationCommand.Back
        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    fun validateEnteredData(reminderData: ReminderDataItem): Boolean {

        // Error: Please enter title
        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        // Error: Please select location
        if (reminderData.location.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }

        return true

    }
}
