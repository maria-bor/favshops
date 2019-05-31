package com.example.favshops.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

//data class Shop(val image: Byte, val name: String, val type: String, val radius: Double)
@IgnoreExtraProperties
data class Shop(
    val name: String? = "",
    val type: String? = "",
    val radius: Int? = 0
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "type" to type,
            "radius" to radius
        )
    }
}