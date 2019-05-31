package com.example.favshops.model

class MapShops(private var mapShops: MutableMap<Int, Shop>) {
    fun addShop(shop: Shop): Int {
        mapShops.put(mapShops.size, shop)
        return mapShops.size - 1
    }

    fun getShop(id: Int): Shop? {
        return mapShops[id]
    }

    fun size(): Int {
        return mapShops.size
    }

    fun getMapShops(): MutableMap<Int, Shop> {
        return mapShops
    }
}