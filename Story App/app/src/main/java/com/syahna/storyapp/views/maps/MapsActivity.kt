package com.syahna.storyapp.views.maps

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.syahna.storyapp.R
import com.syahna.storyapp.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.MapStyleOptions
import com.syahna.storyapp.data.pref.UserPreference
import com.syahna.storyapp.data.repositories.UserRepository
import com.syahna.storyapp.di.Injection
import com.syahna.storyapp.remote.retrofit.ApiConfig
import com.syahna.storyapp.views.ViewModelFactory
import com.syahna.storyapp.views.main.MainActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var userPreference: UserPreference
    private lateinit var binding: ActivityMapsBinding
    private lateinit var viewModelMaps: MapsViewModel
    private lateinit var userRepository: UserRepository
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val FINE_PERMISSION_CODE = 1
    private var userLocation: LatLng? = null

    companion object {
        private const val TAG = "MapsActivity"
        private val DEFAULT_LOCATION = LatLng(-2.548926, 118.014863)
        private const val DEFAULT_ZOOM = 5f // Zoom level untuk Indonesia
    }

    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreference = UserPreference.getInstance(this.dataStore)
        userRepository = Injection.provideRepository(this, ApiConfig.getApiService("token"))
        viewModelMaps = ViewModelProvider(
            this,
            ViewModelFactory(
                userRepository,
                Injection.provideStoriesRepository(this),
                Injection.provideLocationRepository(this)
            )
        )[MapsViewModel::class.java]

        checkUserSession()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as? SupportMapFragment
        if (mapFragment != null) {
            mapFragment.getMapAsync(this)
        } else {
            Log.e(TAG, "MapFragment not found!")
        }
    }

    private fun checkUserSession() {
        Log.d(TAG, "Checking user session")
        lifecycleScope.launch {
            try {
                val userModel = userPreference.getSession().first()
                if (userModel.token.isNotEmpty()) {
                    observeViewModel()
                    viewModelMaps.getLocation()
                } else {
                    Log.d(TAG, "User session not found. Redirecting to login.")
                    showToast("Session expired. Redirecting to login.")
                    startActivity(Intent(this@MapsActivity, MainActivity::class.java))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking session: ${e.message}")
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true

        mMap.setOnMapLoadedCallback {
            moveToDefaultLocation()
            getMyLocation()
        }

        getMyLocation()
        setMapStyle()
        observeViewModel()

        mMap.setOnCameraIdleListener {
            userLocation?.let {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
            }
        }
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ${exception.message}")
        }
    }

    private fun getMyLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                FINE_PERMISSION_CODE
            )
            return
        }

        mMap.isMyLocationEnabled = true
        Log.d(TAG, "My Location enabled")

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val userLocation = LatLng(location.latitude, location.longitude)
                Log.d(TAG, "User location: ${location.latitude}, ${location.longitude}")

                mMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(userLocation, 15f)
                )
            } else {
                Log.e(TAG, "Location is null. Defaulting to Indonesia: $DEFAULT_LOCATION")
                mMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM)
                )
                showToast("Unable to fetch current location. Showing default location: Indonesia.")
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Failed to get location: ${e.message}")
            mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM)
            )
            showToast("Failed to fetch location. Showing default location: Indonesia.")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == FINE_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getMyLocation()
        } else {
            showToast("Permission denied. Cannot fetch location.")
        }
    }

    private fun observeViewModel() {
        viewModelMaps.mapList.observe(this) { listStory ->
            if (listStory.isNullOrEmpty()) {
                Log.w(TAG, "No data found in mapList. Displaying default location.")
                moveToDefaultLocation()
                return@observe
            }

            val boundsBuilder = LatLngBounds.Builder()
            var hasMarker = false

            listStory.forEach { data ->
                val lat = data.lat ?: 0.0
                val lon = data.lon ?: 0.0

                if (lat != 0.0 && lon != 0.0) {
                    val latLng = LatLng(lat, lon)
                    mMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(data.name)
                            .snippet(data.description)
                    )
                    boundsBuilder.include(latLng)
                    hasMarker = true
                } else {
                    Log.e(TAG, "Invalid coordinates for story: ${data.name}")
                }
            }

            if (hasMarker) {
                mMap.setOnMapLoadedCallback {
                    try {
                        val bounds: LatLngBounds = boundsBuilder.build()
                        mMap.animateCamera(
                            CameraUpdateFactory.newLatLngBounds(
                                bounds,
                                resources.displayMetrics.widthPixels,
                                resources.displayMetrics.heightPixels,
                                300 // Padding
                            )
                        )
                    } catch (e: IllegalStateException) {
                        Log.e(TAG, "Error setting camera bounds: ${e.message}")
                    }
                }
            } else {
                Log.w(TAG, "No valid markers to display.")
            }
        }
    }

    private fun moveToDefaultLocation() {
        Log.d(TAG, "Moving camera to default location: $DEFAULT_LOCATION")
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}