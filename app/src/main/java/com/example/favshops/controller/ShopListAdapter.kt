package com.example.favshops.controller

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.favshops.R
import com.example.favshops.model.MapShops
import com.example.favshops.model.Shop
import java.io.File

class ShopListAdapter(private val dataset: MapShops) : RecyclerView.Adapter<ShopListAdapter.ListViewHolder>() {


    class ListViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var imageShop: ImageView
        var nameShop: TextView
        var typeShop: TextView
        var radiusShop: TextView
        init {
            imageShop = item.findViewById(R.id.imageViewShop)
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
        val shop: Shop? = dataset.getShop(position)
        holder.nameShop.text = shop!!.name
        holder.typeShop.text = shop.type
        holder.radiusShop.text = shop.radius.toString()

        if (shop.hasPhoto) {
            val storageDir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val file = File(storageDir.absolutePath + "/shops/${shop.key}" + ".jpg")
            Log.d("---", "hasPhoto = true"+file.absoluteFile)
            if (file.exists()) {
                Log.d("---", "exists()")
                val bitmap: Bitmap = BitmapFactory.decodeFile(file.absolutePath)
                holder.imageShop.setImageBitmap(bitmap)
            }
        }
    }

    override fun getItemCount() = dataset.size()

    fun getCollection(): MapShops {
        return dataset
    }

    fun clearDataset() {
        dataset.getMapShops().clear()
    }
}