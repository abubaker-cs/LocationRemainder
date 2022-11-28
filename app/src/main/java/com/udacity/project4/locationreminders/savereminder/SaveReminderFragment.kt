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
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    private lateinit var geofencingClient: GeofencingClient
    private var title: String? = null
    private var description: String? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var radius = 500f
    private var location: String? = null
    private lateinit var id: String
    private lateinit var contxt: Context


    // val 01 - geofencePendingIntent
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this.contxt as Activity, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(
            this.contxt,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    /**
     * val 02 - requestPermissionLauncher
     */
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    checkPermissionsAndStartGeofencing()
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    checkPermissionsAndStartGeofencing()
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_BACKGROUND_LOCATION, false) -> {
                    checkPermissionsAndStartGeofencing()
                }
                else -> {
                    Log.i("Permission: ", "Denied")
                    Toast.makeText(
                        context,
                        "Location permission was not granted.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
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
     * override - onCreateView()
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        //
        geofencingClient = LocationServices.getGeofencingClient(this.contxt as Activity)

        return binding.root
    }

    /**
     * override - onViewCreated()
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude
            val longitude = _viewModel.longitude.value
            id = UUID.randomUUID().toString()

//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db


            if (
                title == null || description == null || latitude == null || longitude == null
            ) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.save_reminder_error_desc),
                    Snackbar.LENGTH_SHORT
                )
                    .show()
            } else {
                checkPermissionsAndStartGeofencing()
            }

        }
    }

    /**
     * 01 checkPermissionsAndStartGeofencing()
     */
    private fun checkPermissionsAndStartGeofencing() {
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            checkDeviceLocationSettingsAndStartGeofence()
        } else {
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

        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettingsAndStartGeofence(false)
        }

    }

    /**
     * 03 checkDeviceLocationSettingsAndStartGeofence()
     */
    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
        val locationSettingsResponseTask =
            checkDeviceLocationSettings(resolve)
        locationSettingsResponseTask?.addOnCompleteListener {
            if (it.isSuccessful) {
                addGeofence()
                val reminderDataItem =
                    ReminderDataItem(title, description, location, latitude, longitude, id = id)
                _viewModel.saveReminder(reminderDataItem)
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
     * 05 addGeofence()
     */
    @SuppressLint("MissingPermission")
    private fun addGeofence() {
        val geofence = latitude?.let {
            longitude?.let { it1 ->
                Geofence.Builder()
                    .setCircularRegion(it, it1, radius)
                    .setRequestId(id)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setLoiteringDelay(1000)
                    .build()
            }
        }

        val geofenceRequest = geofence?.let {
            GeofencingRequest.Builder()
                .addGeofence(it)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build()
        }

        geofencingClient.addGeofences(geofenceRequest!!, geofencePendingIntent).run {
            addOnSuccessListener {
                Toast.makeText(
                    contxt,
                    contxt.getString(R.string.geofence_added),
                    Toast.LENGTH_LONG
                ).show()
            }
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
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    /**
     * override - onAttach()
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        contxt = context
    }

}

/**
 * val
 * =============================
 * 01 geofencePendingIntent
 * 02 requestPermissionLauncher
 *
 *
 * override
 * =============================
 * 01 onCreateView
 * 02 onViewCreated
 * 03 onActivityResult
 * 04 onDestroy
 * 05 onAttach
 * 06 onRequestPermissionsResult
 *
 *
 * fun
 * =============================
 * 01 checkPermissionsAndStartGeofencing
 * 02 foregroundAndBackgroundLocationPermissionApproved
 * 03 checkDeviceLocationSettingsAndStartGeofence
 * 04 checkDeviceLocationSettings
 * 05 addGeofence
 *
 */
