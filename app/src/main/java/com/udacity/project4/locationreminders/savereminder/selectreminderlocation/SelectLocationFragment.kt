package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.Constants.DEFAULT_ZOOM_LEVEL
import com.udacity.project4.Constants.REQUEST_LOCATION_PERMISSION
import com.udacity.project4.Constants.TAG_LOGIN
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*

@Suppress("DEPRECATION")
class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    // override val _viewModel: SaveReminderViewModel by inject()

    // https://insert-koin.io/docs/reference/koin-android/viewmodel#injecting-your-viewmodel
    override val _viewModel: SaveReminderViewModel by sharedViewModel()

    private var _binding: FragmentSelectLocationBinding? = null
    private val binding get() = _binding!!

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // New Lahore City, Lahore - Pakistan
    private var latitude: Double = 31.342046534659723
    private var longitude: Double = 74.14047456871482
    private val defaultLocation = LatLng(latitude, longitude)

    private lateinit var lastKnownLocation: Location

    private var name: String = "DEFAULT"

    @RequiresApi(Build.VERSION_CODES.Q)
    private val callback = OnMapReadyCallback { googleMap ->

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */

        map = googleMap

        initializeMapWithCustomConfigurations()

    }

    /**
     * Override: onCreateView()
     */
    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSelectLocationBinding.inflate(inflater, container, false)

        // Set the ViewModel
        binding.viewModel = _viewModel

        // Set the LifecycleOwner
        binding.lifecycleOwner = viewLifecycleOwner

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setHasOptionsMenu(true)

        // Set the DisplayHomeAsUpEnabled to true
        setDisplayHomeAsUpEnabled(true)

        // Call this function after the user confirms on the selected location
        // Set the onClickListener for the save remainder location button
        binding.onSaveButtonClicked = View.OnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // A Map component in an app. This fragment is the simplest way to place a map in an application.
        // It's a wrapper around a view of a map to automatically handle the necessary life cycle needs.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?

        // A GoogleMap must be acquired using getMapAsync(OnMapReadyCallback).
        // This class automatically initializes the maps system and the view.
        mapFragment?.getMapAsync(callback)

    }

    private fun onLocationSelected() {

        // Latitude and Longitude
        _viewModel.latitude.value = latitude
        _viewModel.longitude.value = longitude

        // Location Name
        _viewModel.reminderSelectedLocation.value = name

        // Navigate back to the previous fragment
        _viewModel.navigationCommand.value = NavigationCommand.Back

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun initializeMapWithCustomConfigurations() {

        // Custom Coordinates
        val latitude = 31.34201831781176
        val longitude = 74.1404663519627
        val homeLatLng = LatLng(latitude, longitude)

        /**
         * 1: World
         * 5: Landmass/continent
         * 10: City
         * 15: Streets
         * 20: Buildings
         */
        val zoomLevel = DEFAULT_ZOOM_LEVEL // 15f

        // Move the Camera to our custom geo-location
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))

        // Add a marker @ our home location
        map.addMarker(
            MarkerOptions()
                .position(homeLatLng)
                .title("Abubaker's Home")
        )

        // +/- Zoom Buttons
        map.uiSettings.isZoomControlsEnabled = true

        // Allow the user to create new markers on the map
        setMapLongClick(map)

        // Display info about POI when the user clicks on it
        setPoiClick(map)

        // Set the map style using the JSON file
        setMapStyle(map)

        // Enable the My Location layer if the fine location permission has been granted.
        enableMyLocation()
    }

    // Load the JSON file for styling the map
    private fun setMapStyle(map: GoogleMap) {
        try {

            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(

                    // Context
                    requireActivity(),

                    // JSON File
                    R.raw.map_style

                )
            )

            // If the styling is unsuccessful, print a log that the parsing has failed.
            if (!success) {
                Log.e(TAG_LOGIN, "Style parsing failed.")
            }


        } catch (e: Resources.NotFoundException) {

            // If the file is missing
            Log.e(TAG_LOGIN, "Can't find style. Error: ", e)

        }
    }

    // Configuration for the new marker
    private fun setMapLongClick(map: GoogleMap) {

        map.setOnMapLongClickListener { userSelectedLatLng ->

            // A snippet is additional text that's displayed below the title.
            val formatCoordinates = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                userSelectedLatLng.latitude,
                userSelectedLatLng.longitude
            )

            // Add a marker
            map.addMarker(

                // Configurations for the new marker
                MarkerOptions()

                    // Set the position of the marker
                    .position(userSelectedLatLng)

                    // Set the title of the marker
                    .title(getString(R.string.dropped_pin))

                    // Set the snippet of the marker
                    .snippet(formatCoordinates)

                    // Set the color for the marker
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

            )

            map.addCircle(

                CircleOptions()

                    // Set the center of the circle
                    .center(userSelectedLatLng)

                    // Set the radius of the circle
                    .radius(150.0)

                    // Set the stroke width of the circle
                    .strokeColor(Color.argb(255, 0, 0, 255))

                    // Set the fill color of the circle
                    .fillColor(Color.argb(60, 0, 0, 255)).strokeWidth(5F)
            )

        }

    }

    /**
     * Display information about POI (Point of Interest) on the map
     */
    private fun setPoiClick(map: GoogleMap) {

        // 1. This click listener places a marker on the map immediately when the user clicks a POI.
        // 2. The click listener also displays an info window that contains the POI name.
        map.setOnPoiClickListener { pointOfInterest ->

            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(pointOfInterest.latLng)
                    .title(pointOfInterest.name)
            )

            // To immediately display the info window
            poiMarker!!.showInfoWindow()

        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {

        Log.d("TAG_SELECT_LOCATION ", "enableMyLocation: ")

        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        try {
            if (isPermissionGranted()
            ) {

                // SHOW: Find My Location Button on the map
                map.isMyLocationEnabled = true

                // Get reference to the user's last known location
                val locationResult = fusedLocationProviderClient.lastLocation

                locationResult.addOnCompleteListener(requireActivity()) { task ->

                    if (task.isSuccessful) {

                        // Set the map's camera position to the current location of the device.
                        if (task.result != null) {
                            lastKnownLocation = task.result!!

                            // Move the camera to the last known location with the default Zoom Level: 15
                            map.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation.latitude,
                                        lastKnownLocation.longitude
                                    ),
                                    DEFAULT_ZOOM_LEVEL
                                )
                            )

                        }

                    } else {

                        // Log Messages
                        Log.d("TAG_SELECT_LOCATION", "Current location is null. Using defaults.")
                        Log.e("TAG_SELECT_LOCATION", "Exception: %s", task.exception)

                        // Move the camera to the default location with the default Zoom Level: 15
                        map.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM_LEVEL)
                        )

                        // HIDE: Find My Location Button on the map
                        map.uiSettings.isMyLocationButtonEnabled = false

                    }
                }

            } else {

                // locationPermissionRequest
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                )


            }

        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }

    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.Q)
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("<<SelectLocFrag>>", "onRequestPermissionsResult: ")
            enableMyLocation()
        } else {
            Snackbar.make(
                binding.selectLocationFragment,
                R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
            ).setAction(android.R.string.ok) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
            }.show()
        }

    }

    // This method is called when the menu is created.
    @Deprecated(
        "Deprecated in Java", ReplaceWith(
            "inflater.inflate(R.menu.map_options, menu)",
            "com.udacity.project4.R"
        )
    )
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        // Inflate the Map Options Menu, this adds items to the action bar if it is present.
        inflater.inflate(R.menu.map_options, menu)

    }

    // This method is called when the user clicks on a menu item.
    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        // Change the map type based on the user's selection.

        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }

        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }

        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }

        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    enableMyLocation()
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    enableMyLocation()
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
