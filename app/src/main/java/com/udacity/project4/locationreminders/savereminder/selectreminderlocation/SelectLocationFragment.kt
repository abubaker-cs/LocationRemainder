package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
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

    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var name: String = ""

    /**
     * Override: onCreateView()
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        // Set the ViewModel
        binding.viewModel = _viewModel

        // Set the LifecycleOwner
        binding.lifecycleOwner = this

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setHasOptionsMenu(true)

        // Set the DisplayHomeAsUpEnabled to true
        setDisplayHomeAsUpEnabled(true)

        // SupportMapFragment is a fragment that displays a Google map
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        // Get the map asynchronously
        mapFragment.getMapAsync(this)

        requestPermission()

        // Call this function after the user confirms on the selected location
        // Set the onClickListener for the save remainder location button
        binding.onSaveButtonClicked = View.OnClickListener { onLocationSelected() }

        return binding.root
    }

    /**
     * onCreateOptionsMenu()
     */
    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        // Inflate the Map Options Menu, this adds items to the action bar if it is present.
        inflater.inflate(R.menu.map_options, menu)

    }

    /**
     * onOptionsItemSelected()
     */
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        // NOTE: Change the map type based on the user's selection.
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

    /**
     * onLocationSelected()
     */
    private fun onLocationSelected() {
        _viewModel.latitude.value = latitude
        _viewModel.longitude.value = longitude
        _viewModel.reminderSelectedLocationStr.value = name
        _viewModel.navigationCommand.value = NavigationCommand.Back
    }

    /**
     * onMapReady() - Called when the map is ready to be used.
     */
    override fun onMapReady(googleMap: GoogleMap) {

        // Get the map
        map = googleMap

        map.uiSettings.isZoomControlsEnabled = true

        // Set the custom map style using the JSON style file
        setMapStyle(map)

        // Add a new marker when the user long clicks on the map
        setPoiClick(map)

        // Display info about the newly created marker
        setMapLongClick(map)

    }

    /**
     * setCustomMapStyleUsingJSONFile()
     */
    private fun setMapStyle(map: GoogleMap) {

        try {

            // Set the map style from map_style.json file
            val success = map.setMapStyle(
                // Load the style JSON file
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(),
                    R.raw.map_style
                )
            )

            // If the map style failed to load, log an error message to the user.
            if (!success) {
                Log.e(TAG_LOGIN, "Style parsing failed.")
            }

        } catch (e: Resources.NotFoundException) {
            Log.e(TAG_LOGIN, "Can't find style. Error: ", e)
        }
    }

    /**
     * addNewMarkerOnLongClick() - Add a new marker when the user long clicks on the map
     */
    private fun setMapLongClick(map: GoogleMap) {

        // Add a new marker when the user long clicks on the map
        map.setOnMapLongClickListener { latLng ->

            map.clear()

            latitude = latLng.latitude
            longitude = latLng.longitude

            name = "Unknown"

            // Note: A Snippet is additional text that's displayed below the title.
            val snippet = String.format(

                // The default locale is used to get the language code.
                Locale.getDefault(),

                // The Format for the Geo Coordinates
                "Lat: %1$.5f, Long: %2$.5f",

                // The latitude and longitude
                latLng.latitude,
                latLng.longitude

            )

            // Add a marker
            map.addMarker(

                // Configurations for the new marker
                MarkerOptions()

                    // Set the position of the marker
                    .position(latLng)

                    // Set the title of the marker
                    .title(getString(R.string.dropped_pin))

                    // Set the snippet of the marker
                    .snippet(snippet)

                    // Set the icon of the marker
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

            )

            map.addCircle(
                CircleOptions()
                    .center(latLng)
                    .radius(150.0)
                    .strokeColor(Color.argb(255, 0, 0, 255))
                    .fillColor(Color.argb(60, 0, 0, 255)).strokeWidth(5F)
            )

        }

    }

    /**
     * displayInfoAboutNewMarker()
     */
    private fun setPoiClick(map: GoogleMap) {

        // Set a click event handler for the POI (point of interest) layer.
        map.setOnPoiClickListener { poi ->

            map.clear()

            latitude = poi.latLng.latitude
            longitude = poi.latLng.longitude
            name = poi.name

            // Create a new marker for the POI that the user selected.
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )

            map.addCircle(
                CircleOptions()
                    .center(poi.latLng)
                    .radius(150.0)
                    .strokeColor(Color.argb(255, 0, 0, 255))
                    .fillColor(Color.argb(60, 0, 0, 255)).strokeWidth(5F)
            )

            poiMarker?.showInfoWindow()
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

    /**
     * Request Permissions For Coarse & Fine Locations
     */
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

}
