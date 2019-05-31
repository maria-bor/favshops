package com.example.favshops.controller

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.favshops.R
import com.example.favshops.model.ListShops
import com.example.favshops.model.Shop

class ShopListAdapter(private val dataset: ListShops) : RecyclerView.Adapter<ShopListAdapter.ListViewHolder>() {

    class ListViewHolder(item: View) : RecyclerView.ViewHolder(item) {
//        var imageShop: ImageView
        var nameShop: TextView
        var typeShop: TextView
        var radiusShop: TextView
        init {
//            imageShop = item.findViewById(R.id.imageViewShop)
            nameShop = item.findViewById(R.id.textNameShop)
            typeShop = item.findViewById(R.id.textTypeShop)
            radiusShop = item.findViewById(R.id.textRadiusShop)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val li = LayoutInflater.from(parent.context)
        val view = li.inflate(R.layout.row_shop_item, parent, false)

        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val shop: Shop = dataset.getShop(position)
        holder.nameShop.text = shop.name
        holder.typeShop.text = shop.type
        holder.radiusShop.text = shop.radius.toString()
    }

    override fun getItemCount() = dataset.size()


    fun getCollection(): ListShops {
        return dataset
    }

    fun clearDataset() {
        dataset.getListShops().clear()
    }
}