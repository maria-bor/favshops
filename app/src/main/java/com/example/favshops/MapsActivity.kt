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
    private val UPDATE_INTERVAL = (10 * 1000).toLong()  /* 10 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */

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
            var intent = Intent(this@MapsActivity, MainActivity::class.java)
            intent.apply {
                putExtra("lat", latitude)
                putExtra("lon", longitude)
            }
            setResult(MainActivity.LOCATION_REQUEST, intent)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        // initialize location request object
        mLocationRequest = LocationRequest.create()
        mLocationRequest!!.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = UPDATE_INTERVAL
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
        // 4. add permission if android version is greater then 23
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
        Log.d("---", "onLocationChanged")

        // create message for toast with updated latitude and longitudefa
        var msg = "Updated Location: " + location.latitude  + " , " +location.longitude

        latitude = location.latitude
        longitude = location.longitude
        // show toast message with updated location
        //Toast.makeText(this,msg, Toast.LENGTH_LONG).show()
        val location = LatLng(location.latitude, location.longitude)
        mMap.clear()
        marker = mMap.addMarker(MarkerOptions().position(location).title("Current Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12.0f))
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
        mMap = googleMap
        Log.d("---", "onMapReady")

        // Add a marker and move the camera
//        val pjwstk = LatLng(52.2239023, 20.9917673)
//        mMap.addMarker(MarkerOptions().position(pjwstk).title("Marker in PJWST"))

//        mMap.moveCamera(CameraUpdateFactory.newLatLng(pjwstk))
        marker = mMap.addMarker(MarkerOptions().position(LatLng(latitude, longitude)).title("Current Location"))

        mMap.setOnCameraMoveListener {
            marker.position = mMap.cameraPosition.target
            Log.d("---", "setOnCameraMoveListener")
        }

        mMap.setOnCameraIdleListener{
            latitude = mMap.cameraPosition.target.latitude
            longitude= mMap.cameraPosition.target.longitude
            Log.d("---", "setOnCameraIdleListener"+ latitude + " " + longitude)
        }
    }
}