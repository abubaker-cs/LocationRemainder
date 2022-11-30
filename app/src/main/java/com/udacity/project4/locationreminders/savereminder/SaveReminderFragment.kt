package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.Constants.ACTION_GEOFENCE_EVENT
import com.udacity.project4.Constants.BACKGROUND_LOCATION_PERMISSION_INDEX
import com.udacity.project4.Constants.GEOFENCE_RADIUS_IN_METERS
import com.udacity.project4.Constants.LOCATION_PERMISSION_INDEX
import com.udacity.project4.Constants.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
import com.udacity.project4.Constants.REQUEST_TURN_DEVICE_LOCATION_ON
import com.udacity.project4.Constants.TAG_LOGIN
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

@RequiresApi(Build.VERSION_CODES.S)
class SaveReminderFragment : BaseFragment() {

    //Get the view model this time as a single to be shared with the another fragment
    override val baseViewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    private lateinit var geofencingClient: GeofencingClient

    private var title: String? = null
    private var description: String? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var location: String? = null


    private lateinit var id: String
    private lateinit var contxt: Context

    // val 01 - geofencePendingIntent
    private val geofencePendingIntent: PendingIntent by lazy {

        // Intent to start the GeofenceBroadcastReceiver
        val intent = Intent(this.contxt as Activity, GeofenceBroadcastReceiver::class.java)

        // Assign the action to the intent
        intent.action = ACTION_GEOFENCE_EVENT

        // getBroadcast(context, requestCode, intent, flags)
        PendingIntent.getBroadcast(
            this.contxt,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

    }


    /**
     * override - onCreateView()
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        // Set whether home should be displayed as an "up" affordance.
        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = baseViewModel

        geofencingClient = LocationServices.getGeofencingClient(this.contxt as Activity)

        return binding.root
    }

    /**
     * override - onViewCreated()
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.lifecycleOwner = this

        // FAB: Select Location
        binding.selectLocation.setOnClickListener {

            // Navigate to: SelectLocationFragment
            baseViewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())

        }


        // Button: Save Reminder
        binding.saveReminder.setOnClickListener {

            // Title
            val title = baseViewModel.reminderTitle.value

            // Description
            val description = baseViewModel.reminderDescription.value

            // Location
            val location = baseViewModel.reminderSelectedLocationStr.value

            // Latitude: X-Axis
            val latitude = baseViewModel.latitude.value

            // Longitude: Y-Axis
            val longitude = baseViewModel.longitude.value

            id = UUID.randomUUID().toString()

            // Check if the user has entered all the required fields
            if (title == null || description == null || latitude == null || longitude == null) {

                // Show a Toast message: "Please enter all the required fields"
                Snackbar.make(
                    binding.root,
                    getString(R.string.save_reminder_error_desc),
                    Snackbar.LENGTH_SHORT
                ).show()

            } else {

                // Create a ReminderDataItem object
                checkPermissionsAndStartGeofencing()

            }

        }

    }

    /**
     * val 02 - requestPermissionLauncher
     */
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            // Check if the user has granted the permissions
            when {

                // ACCESS_FINE_LOCATION ?
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {

                    checkPermissionsAndStartGeofencing()

                }

                // ACCESS_COARSE_LOCATION ?
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    checkPermissionsAndStartGeofencing()
                }

                // ACCESS_BACKGROUND_LOCATION ?
                permissions.getOrDefault(Manifest.permission.ACCESS_BACKGROUND_LOCATION, false) -> {
                    checkPermissionsAndStartGeofencing()
                }

                else -> {

                    Log.i("Permission: ", "Denied")

                    // Show a Toast message: "Location permission was not granted."
                    Toast.makeText(
                        context,
                        "Location permission was not granted.",
                        Toast.LENGTH_LONG
                    ).show()

                }
            }
        }

    /**
     * 01 checkPermissionsAndStartGeofencing()
     */
    private fun checkPermissionsAndStartGeofencing() {

        // Check if the user has granted the ACCESS_FINE_LOCATION permission
        if (foregroundAndBackgroundLocationPermissionApproved()) {

            // Check if the device's location settings are enabled
            checkDeviceLocationSettingsAndStartGeofence()

        } else {

            // Request for following permissions:
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )

        }
    }

    /**
     * 03 checkDeviceLocationSettingsAndStartGeofence()
     */
    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {

        // Create a LocationRequest object
        val locationSettingsResponseTask = checkDeviceLocationSettings(resolve)

        // Check if the device's location settings are enabled
        locationSettingsResponseTask?.addOnCompleteListener {

            if (it.isSuccessful) {

                // Add the geofence
                addGeofence()

                // Prepare the items to be stored in the database
                val reminderDataItem = ReminderDataItem(
                    title,
                    description,
                    location,
                    latitude,
                    longitude,
                    id = id
                )

                // Save the reminder to the local database
                baseViewModel.saveReminder(reminderDataItem)

            }

        }
    }


    /**
     * 04 checkDeviceLocationSettings()
     */
    private fun checkDeviceLocationSettings(
        resolve: Boolean
    ): Task<LocationSettingsResponse>? {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = this.activity?.let { LocationServices.getSettingsClient(it) }
        val locationSettingsResponseTask =
            settingsClient?.checkLocationSettings(builder.build())
        locationSettingsResponseTask?.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        REQUEST_TURN_DEVICE_LOCATION_ON,
                        null, 0, 0, 0, null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(
                        TAG_LOGIN,
                        "Error getting location settings resolution: " + sendEx.message
                    )
                }
            } else {

                //
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettings(true)
                }.show()
            }
        }
        return locationSettingsResponseTask
    }


    /**
     * override 06 - onRequestPermissionsResult()
     */
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG_LOGIN, "onRequestPermissionResult")

        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED)
        ) {
            Snackbar.make(
                binding.root,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else {
            checkDeviceLocationSettingsAndStartGeofence(true)
        }
    }


    /**
     * 02 foregroundAndBackgroundLocationPermissionApproved
     */
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            contxt,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ))
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            this.contxt, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    //
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        // Check if the user has granted the ACCESS_FINE_LOCATION permission
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {

            //
            checkDeviceLocationSettingsAndStartGeofence(false)

        }

    }


    /**
     * 05 addGeofence()
     */
    @SuppressLint("MissingPermission")
    private fun addGeofence() {

        // Create a Geofence object
        val geofence = latitude?.let {

            longitude?.let { longitudinalAxis ->
                Geofence.Builder()

                    // Sets the request ID of the geofence.
                    .setRequestId(id)

                    // Sets the region of this geofence based on:
                    // 1. Latitude: it
                    // 2. Longitude: longitudinalAxis
                    // 3. Radius: radius
                    .setCircularRegion(it, longitudinalAxis, GEOFENCE_RADIUS_IN_METERS)

                    // Sets the transition types of interest.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)

                    // Sets the expiration duration of geofence.
                    // This geofence will be removed automatically after this period of time.
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)

                    // Sets the delay between Geofence.GEOFENCE_TRANSITION_ENTER and
                    // Geofence.GEOFENCE_TRANSITION_DWELL in milliseconds.
                    .setLoiteringDelay(1000)

                    // Creates a geofence object.
                    .build()

            }

        }

        // Create a GeofencingRequest object
        val geofenceRequest = geofence?.let {

            // Create a GeofencingRequest object
            GeofencingRequest.Builder()

                // Sets whether to monitor the entry to the geofence.
                .addGeofence(it)

                // Sets whether to monitor the exit from the geofence.
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)

                // Creates a GeofencingRequest object.
                .build()
        }

        // Depending on the Success or Failure: Display Toast messages
        geofencingClient.addGeofences(geofenceRequest!!, geofencePendingIntent).run {

            // Success: Geofence added successfully
            addOnSuccessListener {
                Toast.makeText(
                    contxt,
                    contxt.getString(R.string.geofence_added),
                    Toast.LENGTH_LONG
                ).show()
            }

            // Failure: An exception occurred *****
            addOnFailureListener {
                Toast.makeText(
                    contxt,
                    "An exception occurred: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

        }

    }


    /**
     * override - onDestroy()
     */
    override fun onDestroy() {

        // Called when the fragment is no longer in use.
        // This is called after onStop() and before onDetach().
        super.onDestroy()

        // make sure to clear the view model after destroy, as it's a single view model.
        baseViewModel.onClear()

    }

    /**
     * override - onAttach()
     */
    override fun onAttach(context: Context) {

        // Called when a fragment is first attached to its context.
        // onCreate(Bundle) will be called after this.
        super.onAttach(context)

        contxt = context

    }

}
