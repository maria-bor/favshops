package com.example.favshops.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Geo(
    val lat: Double = 0.0,
    val lon: Double = 0.0
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "lat" to lat,
            "lon" to lon
        )
    }
}