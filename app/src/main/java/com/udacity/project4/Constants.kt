package com.udacity.project4

/**
 * Constant Variables
 */
object Constants {

    // API Key
    const val API_KEY = BuildConfig.MAPS_API_KEY

    const val TAG_LOGIN = "LoginFragment"
    const val SIGN_IN_RESULT_CODE = 1001

    // For Live-Location
    const val REQUEST_LOCATION_PERMISSION = 1

    //
    const val LOCATION_PERMISSION_INDEX = 0
    const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
    const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
    const val REQUEST_TURN_DEVICE_LOCATION_ON = 29

    // Geofence
    const val ACTION_GEOFENCE_EVENT =
        "GeofenceBroadcastReceiver.project4.action.ACTION_GEOFENCE_EVENT"
}
