package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.Constants.ACTION_GEOFENCE_EVENT
import com.udacity.project4.Constants.BACKGROUND_LOCATION_PERMISSION_INDEX
import com.udacity.project4.Constants.GEOFENCE_RADIUS_IN_METERS
import com.udacity.project4.Constants.LOCATION_PERMISSION_INDEX
import com.udacity.project4.Constants.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
import com.udacity.project4.Constants.REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
import com.udacity.project4.Constants.REQUEST_TURN_DEVICE_LOCATION_ON
import com.udacity.project4.Constants.TAG_SAVE
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*

class SaveReminderFragment : BaseFragment() {

    // Get the view model this time as a single to be shared with the another fragment
    @Suppress("DEPRECATION")
    override val _viewModel: SaveReminderViewModel by sharedViewModel()

    private var _binding: FragmentSaveReminderBinding? = null
    private val binding get() = _binding!!

    // Geofencing Client
    private lateinit var geofencingClient: GeofencingClient

    // Reminder Data Item
    private lateinit var reminder: ReminderDataItem

    // Location Request
    private val runningQOrLater = Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.Q


    // onCreateView()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        _binding = FragmentSaveReminderBinding.inflate(inflater, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        // Initialize the geofencing client.
        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        return binding.root
    }

    // onViewCreated()
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        // Set viewLifecycleOwner to the viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Button: Select Map
        binding.selectLocation.setOnClickListener {

            // Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())

        }

        // Button: Save Reminder
        binding.saveReminder.setOnClickListener {

            // Title
            val title = _viewModel.reminderTitle.value

            // Description
            val description = _viewModel.reminderDescription.value

            // Selected Location
            val location = _viewModel.reminderSelectedLocation.value

            // Geo-Coordinates
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            // Reminder Data Item
            reminder = ReminderDataItem(
                title,
                description,
                location,
                latitude,
                longitude
            )

            if (_viewModel.validateEnteredData(reminder)) {
                checkPermissionsAndStartGeofencing()
            }

        }
    }

    // Activity Result
    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettingsAndStartGeofence(false)
        }
    }

    /**
     * Check permissions and start geofencing
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkPermissionsAndStartGeofencing() {

        // Check if the foreground and background location permissions have been granted
        if (foregroundAndBackgroundLocationPermissionApproved()) {

            // Check if the device location is enabled
            checkDeviceLocationSettingsAndStartGeofence()

        } else {

            // Request foreground and background location permissions
            requestForegroundAndBackgroundLocationPermissions()

        }
    }

    /**
     * Check if the Foreground and background location permissions are approved
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {

        // Foreground Permission
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ))

        // Background Permission
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }

        // Return
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    /**
     * checkDeviceLocationSettingsAndStartGeofence() - Check if the device location is enabled,
     * and if it is, start saving geofence
     */
    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }

        // Configurations
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())

        // On Failure: Request Location
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {

                    // Start Intent Sender
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        REQUEST_TURN_DEVICE_LOCATION_ON, null, 0, 0, 0, null
                    )

                } catch (e: IntentSender.SendIntentException) {

                    // Log error
                    Log.d("Can't get location: ", e.message!!)

                }
            } else {

                // Request the user to enable device location
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {

                    // Check Device Location Settings
                    checkDeviceLocationSettingsAndStartGeofence()

                }.show()
            }
        }

        // On Success: Add Geofence
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                addGeofenceForReminder()
            }
        }
    }

    /**
     * Add Geofence Reminder
     */
    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    private fun addGeofenceForReminder() {

        if (this::reminder.isInitialized) {

            val currentGeofenceData = reminder

            // Prepare the Geofence object
            val geofence = Geofence.Builder()
                .setRequestId(currentGeofenceData.id)
                .setCircularRegion(
                    currentGeofenceData.latitude!!,
                    currentGeofenceData.longitude!!,
                    GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()

            // Prepare the Geofencing Request
            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            // Prepare the Geofence Pending Intent
            val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
            intent.action = ACTION_GEOFENCE_EVENT

            // PendingIntent.getBroadcast(context, requestCode, intent, flags)
            val geofencePendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                0,
                intent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Add Geofence
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {

                // On Success
                addOnSuccessListener {
                    _viewModel.showSnackBarInt.value = R.string.geofences_added
                    _viewModel.validateAndSaveReminder(reminder)
                    Log.i("Added geofence: ", geofence.requestId)
                }

                // On Failure:
                addOnFailureListener {
                    _viewModel.showSnackBarInt.value = R.string.geofences_not_added
                    if ((it.message != null)) {
                        Log.e("Failed to add geofence:", it.message!!)
                    }
                }
            }
        }

        // Navigate to Reminders List
        _viewModel.navigationCommand.value = NavigationCommand.BackTo(R.id.reminderListFragment)

    }

    /**
     * Request foreground and background location permissions
     */
    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestForegroundAndBackgroundLocationPermissions() {

        if (foregroundAndBackgroundLocationPermissionApproved()) return

        // Prepare the list of permissions to be requested
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        // Prepare the resultCode
        val resultCode = when {
            runningQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }

        // Request Permissions
        requestPermissions(permissionsArray, resultCode)
    }

    /**
     * onRequestPermissionsResult
     */
    @Deprecated("Deprecated in Java")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        // Log the message
        Log.d(TAG_SAVE, "onRequestPermissionResult")

        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED)
        ) {

            // Permission denied.
            Snackbar.make(
                binding.saveReminderFragment,
                R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    // Displays App settings screen.
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        // data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()

        } else {

            // Check Device Location Settings and Start Geofence
            checkDeviceLocationSettingsAndStartGeofence()

        }
    }


    override fun onDestroy() {
        super.onDestroy()

        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()

        _binding = null
    }

}
