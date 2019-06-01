package com.example.favshops.model

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.favshops.MainActivity
import com.example.favshops.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

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
        var mapShops = MainActivity.mapShops

        mapShops.getMapShops().forEach {
            val lat = it.value.geo!!.lat
            val lon = it.value.geo!!.lon
            val radius = it.value.radius
            val location = LatLng(lat, lon)
            mMap.addCircle(CircleOptions().center(location).radius(radius!!.toDouble())
                .fillColor(Color.argb(90, 221, 237, 245)).strokeColor(Color.parseColor("#DDEDF5")))
            mMap.addMarker(MarkerOptions().position(location).title(it.value.name))
        }

        var locZoom = LatLng(mapShops.getMapShops().get(0)!!.geo!!.lat, mapShops.getMapShops().get(0)!!.geo!!.lon)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locZoom, 10.0f))
    }
}