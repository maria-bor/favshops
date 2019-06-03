package com.example.favshops

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLngBounds
import java.lang.IllegalStateException

class ShopLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop_location)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val mapShops = MainActivity.mapShops

        if (mapShops.getMapShops().size > 0) {
            val builder = LatLngBounds.Builder()

            mapShops.getMapShops().forEach {
                val lat = it.value.geo!!.lat
                val lon = it.value.geo!!.lon
                if (lat != 0.0 && lon != 0.0) {
                    val radius = it.value.radius
                    val location = LatLng(lat, lon)
                    mMap.addCircle(
                        CircleOptions().center(location).radius(radius!!.toDouble())
                            .fillColor(Color.argb(90, 221, 237, 245)).strokeColor(Color.parseColor("#DDEDF5"))
                    )
                    val marker = mMap.addMarker(MarkerOptions().position(location).title(it.value.name))
                    builder.include(marker.getPosition());
                }
            }

            try {
                val bounds = builder.build()
                val padding = 200
                val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                googleMap.animateCamera(cu);
            } catch (e: IllegalStateException) {
                Log.d("---", "Exception:"+e.message)
            }
        }
    }
}