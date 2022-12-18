package com.udacity.project4

/**
 * Constant Variables
 */
object Constants {

    // API Key
    const val API_KEY = BuildConfig.MAPS_API_KEY

    const val TAG_LOGIN = "LoginFragment"
    const val TAG_SAVE = "SaveReminderFragment"
    const val SIGN_IN_RESULT_CODE = 1001

    // For Live-Location
    const val REQUEST_LOCATION_PERMISSION = 1

    //
    const val LOCATION_PERMISSION_INDEX = 0
    const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
    const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
    const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
    const val REQUEST_TURN_DEVICE_LOCATION_ON = 29

    // Geofence
    const val ACTION_GEOFENCE_EVENT =
        "GeofenceBroadcastReceiver.project4.action.ACTION_GEOFENCE_EVENT"

    const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

    // To be used in SaveReimbursementFragment
    const val GEOFENCE_RADIUS_IN_METERS = 500f
}
