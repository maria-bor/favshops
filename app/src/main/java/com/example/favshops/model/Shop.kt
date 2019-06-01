package com.example.favshops.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Shop(
    val name: String? = "",
    val type: String? = "",
    val radius: Int? = 0,
    val geo: Geo? = null,
    val key: String? = "",
    var hasPhoto: Boolean = false
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "type" to type,
            "radius" to radius,
            "geo" to geo
        )
    }
}