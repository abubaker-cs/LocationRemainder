package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch

class SaveReminderViewModel(val app: Application, private val dataSource: ReminderDataSource) :
    BaseViewModel(app) {

    // Title of the reminder
    val reminderTitle = MutableLiveData<String?>()

    // Description of the reminder
    val reminderDescription = MutableLiveData<String?>()

    // Selected Location of the reminder
    val reminderSelectedLocationStr = MutableLiveData<String?>()

    // Selected POI (Point of Interest)
    private val selectedPOI = MutableLiveData<PointOfInterest?>()

    // Geo-Coordinates of the selected location
    val latitude = MutableLiveData<Double?>()
    val longitude = MutableLiveData<Double?>()


    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {

        // Clear the title
        reminderTitle.value = null

        // Clear the description
        reminderDescription.value = null

        // Clear the location
        reminderSelectedLocationStr.value = null

        // Clear the POI (Point of Interest)
        selectedPOI.value = null

        // Clear the Geo-Coordinates
        latitude.value = null
        longitude.value = null
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    fun validateAndSaveReminder(reminderData: ReminderDataItem) {

        // Validate the entered data
        if (validateEnteredData(reminderData)) {

            // Save the reminder when the validation is successful
            saveReminder(reminderData)

        }

    }

    /**
     * onLocationSelected
     */
    fun onLocationSelected(selectedLocation: LatLng, selectedLocationDescription: String?) {

        // Set the Geo-Coordinates
        latitude.value = selectedLocation.latitude
        longitude.value = selectedLocation.longitude

        // Set the description for the location
        reminderSelectedLocationStr.value = selectedLocationDescription

        // Navigate the user back to the previous screen
        navigationCommand.value = NavigationCommand.Back

    }

    /**
     * Save the reminder to the data source
     */
    fun saveReminder(reminderData: ReminderDataItem) {

        // Show the loading indicator
        showLoading.value = true

        // Use coroutines to save the reminder and navigate back to the previous screen
        viewModelScope.launch {

            // Save the reminder
            dataSource.saveReminder(

                // Create a ReminderDTO object, based on the structure defined in the:
                // @/locationremainder/data/dto/ReminderDTO.kt
                ReminderDTO(

                    // Set the title
                    reminderData.title,

                    // Set the description
                    reminderData.description,

                    // Set the location
                    reminderData.location,

                    // Set the Geo-Coordinates
                    reminderData.latitude,
                    reminderData.longitude,

                    // Set the ID
                    reminderData.id
                )

            )

            // Hide the loading indicator
            showLoading.value = false

            // Show a toast message: Reminder Saved !
            showToast.value = app.getString(R.string.reminder_saved)

            // Navigate the user back to the previous screen
            navigationCommand.value = NavigationCommand.Back

        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    fun validateEnteredData(reminderData: ReminderDataItem): Boolean {

        // check if the title is not null | empty
        if (reminderData.title.isNullOrEmpty()) {

            // otherwise alert the user using SnackBar using following message:
            showSnackBarInt.value = R.string.err_enter_title

            // return false
            return false
        }

        // check if the location data is not null | empty
        if (reminderData.location.isNullOrEmpty()) {

            // otherwise alert the user using SnackBar using following message:
            showSnackBarInt.value = R.string.err_select_location

            // return false
            return false

        }

        return true

    }
}
