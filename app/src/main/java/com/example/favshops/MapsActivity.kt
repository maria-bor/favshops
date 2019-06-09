package com.example.favshops

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mLocationRequest: LocationRequest? = null

    private var latitude = 0.0
    private var longitude = 0.0
    private lateinit var marker: Marker
    private lateinit var fabSaveLocation: FloatingActionButton

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fabSaveLocation = findViewById(R.id.fabSaveLocation) as FloatingActionButton
        fabSaveLocation.setOnClickListener {
            val intent = Intent(this@MapsActivity, MainActivity::class.java)
            intent.apply {
                putExtra("lat", latitude)
                putExtra("lon", longitude)
            }
            setResult(1, intent)
            finish()
        }

        latitude = intent.getDoubleExtra("LAT", 0.0)
        longitude = intent.getDoubleExtra("LON", 0.0)
    }

    override fun onStart() {
        super.onStart()
        if(latitude == 0.0 && longitude == 0.0) {
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        // initialize location request object
        mLocationRequest = LocationRequest.create()
        mLocationRequest!!.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            setNumUpdates(1)
        }

        // initialize location setting request builder object
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        val locationSettingsRequest = builder.build()

        // initialize location service object
        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient!!.checkLocationSettings(locationSettingsRequest)

        // call register location listener
        registerLocationListner()
    }

    private fun registerLocationListner() {
        // initialize location callback object
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                onLocationChanged(locationResult!!.getLastLocation())
            }
        }
        // add permission if android version is greater then 23
        if(Build.VERSION.SDK_INT >= 23 && checkPermission()) {
            LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper())
        }
    }

    private fun checkPermission() : Boolean {
        if (ContextCompat.checkSelfPermission(this ,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions()
            return false
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf("Manifest.permission.ACCESS_FINE_LOCATION"),1)
    }

    private fun onLocationChanged(location: Location) {
        latitude = location.latitude
        longitude = location.longitude

        val loc = LatLng(location.latitude, location.longitude)
        mMap.clear()
        marker = mMap.addMarker(MarkerOptions().position(loc).title("Current Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        marker = mMap.addMarker(MarkerOptions().position(LatLng(latitude, longitude)).title("Current Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 16.0f))

        mMap.setOnCameraMoveListener {
            marker.position = mMap.cameraPosition.target
        }

        mMap.setOnCameraIdleListener{
            latitude = mMap.cameraPosition.target.latitude
            longitude= mMap.cameraPosition.target.longitude
        }
    }
}