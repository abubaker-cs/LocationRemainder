package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.Constants.REQUEST_LOCATION_PERMISSION
import com.udacity.project4.Constants.TAG_LOGIN
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()

    private var _binding: FragmentSelectLocationBinding? = null
    private val binding get() = _binding!!

    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var map: GoogleMap

    private var latitude: Double = -33.852
    private var longitude: Double = 151.211
    private var name: String = "DEFAULT"

    /**
     * Override: onCreateView()
     */
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

        // SupportMapFragment: It is a subclass of Fragment that displays a Google map.
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        /**
         * A GoogleMap must be acquired using getMapAsync(OnMapReadyCallback).
         * This class automatically initializes the maps system and the view.
         */
        mapFragment.getMapAsync(this)

        requestPermission()

        // Call this function after the user confirms on the selected location
        // Set the onClickListener for the save remainder location button
        binding.onSaveButtonClicked = View.OnClickListener {
            onLocationSelected()
        }

        return binding.root
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


//    ---------------------------------------------

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
        val zoomLevel = 15f

        // Move the Camera to our custom geo-location
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))

        // Add a marker @ our home location
        map.addMarker(
            MarkerOptions()
                .position(homeLatLng)
                .title("Abubaker's Home")
        )

        // Allow the user to create new markers on the map
        setMapLongClick(map)

        // Display info about POI when the user clicks on it
        setPoiClick(map)

        // Set the map style using the JSON file
        setMapStyle(map)

        // Enable the My Location layer if the fine location permission has been granted.
        // enableMyLocation()

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

            map.addMarker(
                MarkerOptions()

                    // Set the position of the marker
                    .position(userSelectedLatLng)

                    // Title
                    .title(getString(R.string.dropped_pin))

                    // Display coordinates of the new marker
                    .snippet(formatCoordinates)

                    // Custom Icon color
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

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

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()
        ) {
            map.isMyLocationEnabled = true
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ),
            )
        }
    }

//    private fun enableMyLocations() {
//        if (isPermissionGranted()) {
//            map.isMyLocationEnabled = true
//        } else {
//            ActivityCompat.requestPermissions(
//                requireContext(),
//                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
//                REQUEST_LOCATION_PERMISSION
//            )
//        }
//    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocations() {
        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->

            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
                name = "Current Location"
                val currentLatLng = LatLng(latitude, longitude)
                val markerOptions = MarkerOptions().position(currentLatLng)
                map.addMarker(markerOptions)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15F))
            }

        }
    }


    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    // This method is called when the menu is created.
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        // Inflate the Map Options Menu, this adds items to the action bar if it is present.
        inflater.inflate(R.menu.map_options, menu)

    }

    // This method is called when the user clicks on a menu item.
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

    @RequiresApi(Build.VERSION_CODES.N)
    private fun requestPermission() {
        locationPermissionRequest =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
            { permissions ->
                when {
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        enableUserLocation()
                    }
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                        enableUserLocation()
                    }
                    else -> {
                        Toast.makeText(
                            context,
                            "Location permission was not granted.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    @SuppressLint("MissingPermission")
    private fun enableUserLocation() {
        when {
            (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
                    == PackageManager.PERMISSION_GRANTED) -> {
                map.isMyLocationEnabled = true
                getCurrentLocation()
                Toast.makeText(context, "Location permission is granted.", Toast.LENGTH_LONG).show()
            }
            else -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }

        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
                name = "Current Location"
                val currentLatLng = LatLng(latitude, longitude)
                val markerOptions = MarkerOptions().position(currentLatLng)
                map.addMarker(markerOptions)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15F))
            }
        }
    }

//    ---------------------------------------------


//    /**
//     * onMapReady() - Called when the map is ready to be used.
//     */
//    @SuppressLint("MissingPermission")
//    override fun onMapReady(googleMap: GoogleMap) {
//
//        // Get the map
//        map = googleMap
//
//        map.uiSettings.isZoomControlsEnabled = true
//
//        // Display info about the newly created marker
//        setMapLongClick(map)
//
//        // Display information about POI (Point of Interest | Public Places)
//        setPoiClick(map)
//
//        // Set the custom map style using the JSON style file
//        setMapStyle(map)
//
//        map.isMyLocationEnabled = true
//        map.uiSettings.isMyLocationButtonEnabled = false
//
//        // Enable the My Location layer if the fine location permission has been granted.
//        // enableMyLocation()
//        getCurrentLocation()
//
//    }
//
//    /**
//     * setCustomMapStyleUsingJSONFile()
//     */
//    private fun setMapStyle(map: GoogleMap) {
//
//        try {
//
//            // Set the map style from map_style.json file
//            val success = map.setMapStyle(
//                // Load the style JSON file
//                MapStyleOptions.loadRawResourceStyle(
//                    requireActivity(),
//                    R.raw.map_style
//                )
//            )
//
//            // If the map style failed to load, log an error message to the user.
//            if (!success) {
//                Log.e(TAG_LOGIN, "Style parsing failed.")
//            }
//
//        } catch (e: Resources.NotFoundException) {
//
//            // Missing File
//            Log.e(TAG_LOGIN, "Can't find style. Error: ", e)
//
//        }
//    }
//
//    /**
//     * setMapLongClick() - Add a new marker when the user long clicks on the map
//     */
//    private fun setMapLongClick(map: GoogleMap) {
//
//        // Add a new marker when the user long clicks on the map
//        map.setOnMapLongClickListener { userSelectedLatLng ->
//
//            // Removes all markers, polylines, polygons, overlays, etc from the map.
//            map.clear()
//
//            // Note: A Snippet is additional text that's displayed below the title.
//            val formatCoordinates = String.format(
//
//                // The default locale is used to get the language code.
//                Locale.getDefault(),
//
//                // The Format for the Geo Coordinates
//                "Lat: %1$.5f, Long: %2$.5f",
//
//                // The latitude and longitude
//                userSelectedLatLng.latitude,
//                userSelectedLatLng.longitude
//
//            )
//
//            // Add a marker
//            map.addMarker(
//
//                // Configurations for the new marker
//                MarkerOptions()
//
//                    // Set the position of the marker
//                    .position(userSelectedLatLng)
//
//                    // Set the title of the marker
//                    .title(getString(R.string.dropped_pin))
//
//                    // Set the snippet of the marker
//                    .snippet(formatCoordinates)
//
//                    // Set the color for the marker
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//
//            )
//
//            map.addCircle(
//
//                CircleOptions()
//
//                    // Set the center of the circle
//                    .center(userSelectedLatLng)
//
//                    // Set the radius of the circle
//                    .radius(150.0)
//
//                    // Set the stroke width of the circle
//                    .strokeColor(Color.argb(255, 0, 0, 255))
//
//                    // Set the fill color of the circle
//                    .fillColor(Color.argb(60, 0, 0, 255)).strokeWidth(5F)
//            )
//
//        }
//
//    }
//
//    /**
//     * setPoiClick() - Point of Interest (POI) | Public Places
//     */
//    private fun setPoiClick(map: GoogleMap) {
//
//        // 1. This click listener places a marker on the map immediately when the user clicks a POI.
//        // 2. The click listener also displays an info window that contains the POI name.
//        map.setOnPoiClickListener { pointOfInterest ->
//
//            map.clear()
//
//            // Create a new marker for the POI that the user selected.
//            val poiMarker = map.addMarker(
//                MarkerOptions()
//
//                    // Set the position of the marker
//                    .position(pointOfInterest.latLng)
//
//                    // Set the title of the marker
//                    .title(pointOfInterest.name)
//            )
//
//            map.addCircle(
//
//                CircleOptions()
//
//                    // Set the center of the circle
//                    .center(pointOfInterest.latLng)
//
//                    // Set the radius of the circle
//                    .radius(150.0)
//
//                    // Set the stroke width of the circle
//                    .strokeColor(Color.argb(255, 0, 0, 255))
//
//                    // Set the fill color of the circle
//                    .fillColor(Color.argb(60, 0, 0, 255)).strokeWidth(5F)
//
//            )
//
//            poiMarker!!.showInfoWindow()
//
//        }
//
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun getCurrentLocation() {
//        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
//
//            // Got last known location. In some rare situations this can be null.
//            if (location != null) {
//                latitude = location.latitude
//                longitude = location.longitude
//                name = "Current Location"
//                val currentLatLng = LatLng(latitude, longitude)
//                val markerOptions = MarkerOptions().position(currentLatLng)
//                map.addMarker(markerOptions)
//                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15F))
//            }
//
//        }
//    }
//
//
//    @SuppressLint("MissingPermission")
//    private fun enableMyLocation() {
//        if (isPermissionGranted()) {
//
//            // In order to set the value, you will have to assign a value like this
//            map.isMyLocationEnabled = true
//            Toast.makeText(context, "Location permission is granted.", Toast.LENGTH_SHORT).show()
//
//        } else {
//
//            locationPermissionRequest.launch(
//                arrayOf(
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                )
//            )
//
//        }
//    }
//
//    // isPermissionGranted() - Check if the user has granted the location permission
//    private fun isPermissionGranted(): Boolean {
//        return ContextCompat.checkSelfPermission(
//            requireContext(),
//            Manifest.permission.ACCESS_FINE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED
//    }
//
//    /**
//     * Request Permissions For Coarse & Fine Locations
//     */
//    @RequiresApi(Build.VERSION_CODES.Q)
//    private fun requestPermission() {
//        locationPermissionRequest =
//            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
//            { permissions ->
//                when {
//                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
//                        enableMyLocation()
//                    }
//                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
//                        enableMyLocation()
//                    }
//                    else -> {
//                        Toast.makeText(
//                            context,
//                            "Location permission was not granted.",
//                            Toast.LENGTH_LONG
//                        ).show()
//                    }
//                }
//            }
//
//        locationPermissionRequest.launch(
//            arrayOf(
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            )
//        )
//    }
//
//    @Deprecated("Deprecated in Java")
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        // Check if location permissions are granted and if so enable the
//        // location data layer.
//        if (requestCode == REQUEST_LOCATION_PERMISSION) {
//            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
//                enableMyLocation()
//            }
//        }
//    }
//
//    /**
//     * onCreateOptionsMenu()
//     */
//    @Deprecated(
//        "Deprecated in Java", ReplaceWith(
//            "inflater.inflate(R.menu.map_options, menu)",
//            "com.udacity.project4.R"
//        )
//    )
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//
//        // Inflate the Map Options Menu, this adds items to the action bar if it is present.
//        inflater.inflate(R.menu.map_options, menu)
//
//    }
//
//    /**
//     * onOptionsItemSelected()
//     */
//    @Deprecated("Deprecated in Java")
//    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
//
//        // NOTE: Change the map type based on the user's selection.
//        R.id.normal_map -> {
//            map.mapType = GoogleMap.MAP_TYPE_NORMAL
//            true
//        }
//        R.id.hybrid_map -> {
//            map.mapType = GoogleMap.MAP_TYPE_HYBRID
//            true
//        }
//        R.id.satellite_map -> {
//            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
//            true
//        }
//        R.id.terrain_map -> {
//            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
//            true
//        }
//        else -> super.onOptionsItemSelected(item)
//    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
