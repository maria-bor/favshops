package com.example.favshops.model

class ListShops(private var listShops: ArrayList<Shop>) {
    fun addShop(shop: Shop) {
        listShops.add(shop)
    }

    fun getShop(id: Int): Shop {
        return listShops[id]
    }

    fun size(): Int {
        return listShops.size
    }

    fun getListShops(): ArrayList<Shop> {
        return listShops
    }
}