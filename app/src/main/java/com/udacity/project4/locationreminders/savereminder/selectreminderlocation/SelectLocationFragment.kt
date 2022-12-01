package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
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
import androidx.core.content.ContextCompat
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

    // Default Location: Daewoo Express Thokar Niaz Baig, Lahore - Pakistan
    private var selectedLocation: LatLng = LatLng(31.470095, 74.238973)

    //
    private var selectedLocationDescription: String? = null

    /**
     * Override: onCreateView()
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        // Inflate the layout: @layout/fragment_select_location.xml
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

        // Call this function after the user confirms on the selected location
        // Set the onClickListener for the save remainder location button
        binding.saveRemainderLocationButton.setOnClickListener {

//            Log.e("LatLng: ", "SAVE - selected Location: $selectedLocation")
//            Log.e("LatLng: ", "SAVE - selected Location Description: $selectedLocationDescription")

            // Set the selected location and description
            _viewModel.onLocationSelected(selectedLocation, selectedLocationDescription)

        }

        return binding.root
    }

    /**
     * onCreateOptionsMenu()
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        // Inflate the Map Options Menu, this adds items to the action bar if it is present.
        inflater.inflate(R.menu.map_options, menu)

    }

    /**
     * onOptionsItemSelected()
     */
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
     * onMapReady() - Called when the map is ready to be used.
     */
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onMapReady(googleMap: GoogleMap) {

        // Get the map
        map = googleMap

        // Zoom the current device location
        zoomToCurrentDeviceLocation()

        // Set the custom map style using the JSON style file
        setCustomMapStyleUsingJSONFile(map)

        // Add a new marker when the user long clicks on the map
        addNewMarkerOnLongClick(map)

        // Display info about the newly created marker
        displayInfoAboutNewMarker(map)

        // Verify or request location permissions
        verifyOrRequestLocationPermissions()

    }

    /**
     * zoomToCurrentDeviceLocation()
     */
    private fun zoomToCurrentDeviceLocation() {

        // TODO: Get the current Device Location and ZOOM to proper Level
        // reference: https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial#get-the-current-place

        //These coordinates represent the latitude and longitude of the Googleplex.
        val latitude = 31.470095
        val longitude = 74.238973
        val zoomLevel = 15f
        val homeLatLng = LatLng(latitude, longitude)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
        map.addMarker(MarkerOptions().position(homeLatLng))

    }

    /**
     * setCustomMapStyleUsingJSONFile()
     */
    private fun setCustomMapStyleUsingJSONFile(map: GoogleMap) {

        try {

            // Set the map style from map_style.json file
            val success = map.setMapStyle(
                // Load the style JSON file
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
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
    private fun addNewMarkerOnLongClick(map: GoogleMap) {

        // Add a new marker when the user long clicks on the map
        map.setOnMapLongClickListener { latLng ->

            // Note: A Snippet is additional text that's displayed below the title.
            val snippet = String.format(

                // The default locale is used to get the language code.
                Locale.getDefault(),

                // The Format for the Geo Coordinates
                "Lat: %1$.5f, Long: %2$.5f",

                // The latitude and longitude
                latLng.latitude, latLng.longitude

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


            // Set the selected location and description
            selectedLocation = latLng
            selectedLocationDescription = "Custom location"

            Log.e("LatLng: ", "ADD - Lat lag: $latLng")
            Log.e("LatLng: ", "ADD - selected Location: $selectedLocation")
            Log.e("LatLng: ", "ADD - selected Location Description: $selectedLocationDescription")

        }

    }

    /**
     * displayInfoAboutNewMarker()
     */
    private fun displayInfoAboutNewMarker(map: GoogleMap) {

        // Set a click event handler for the POI (point of interest) layer.
        map.setOnPoiClickListener { poi ->

            // Create a new marker for the POI that the user selected.
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )

            // Display the poi name in the marker's snippet.
            poiMarker?.showInfoWindow()

            // Set the selected location
            selectedLocation = poi.latLng

            // Set the selected location's title
            selectedLocationDescription = poiMarker?.title
        }
    }

    /**
     * verifyOrRequestLocationPermissions()
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun verifyOrRequestLocationPermissions() {

        if (isFinePermissionGranted()) {

            // map.isMyLocationEnabled = true
            Toast.makeText(context, "Permission for Fine Location is granted.", Toast.LENGTH_SHORT)
                .show()

        } else {

            // Permission to access the location is missing. Show rationale and request permission
            requestPermissionsForCoarseFineLocations.launch(

                // Ask the user to allow enable both the fine and coarse locations
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

            )

        }

    }

    /**
     * isFinePermissionGranted()
     */
    private fun isFinePermissionGranted(): Boolean {

        // Check if the permission for the precise/fine location is granted
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    }

    /**
     * Request Permissions For Coarse & Fine Locations
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private val requestPermissionsForCoarseFineLocations =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {

                // Permission: Approximate location (ACCESS_COARSE_LOCATION)
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    verifyOrRequestLocationPermissions()
                }

                // Permission: Precise location (ACCESS_FINE_LOCATION)
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    verifyOrRequestLocationPermissions()
                }

                else -> {

                    // Log: Permission denied
                    Log.i("Permission: ", "Denied")

                    // Failure Toast Message: Location permission was not granted.
                    Toast.makeText(
                        context,
                        "Location permission was not granted.",
                        Toast.LENGTH_LONG
                    ).show()

                }
            }
        }

}
