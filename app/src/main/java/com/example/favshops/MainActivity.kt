package com.example.favshops

import android.app.AlertDialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Geocoder
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.MenuItem
import android.support.v4.widget.DrawerLayout
import android.support.design.widget.NavigationView
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.favshops.controller.ShopListAdapter
import com.example.favshops.model.Geo
import com.example.favshops.model.MapShops
import com.example.favshops.model.Shop
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterShop: ShopListAdapter

    private lateinit var uidUser: String

    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private val storageDir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    private val shopsDirectory = File(storageDir.absolutePath+"/shops/")

    private var file: File? = null
    private lateinit var keyIndexMap: MutableMap<String?, Int>
    private var lat: Double = 0.0
    private var lon: Double = 0.0
    private lateinit var address: TextView

    companion object {
        const val REQUIRED = "Required"
        const val LOCATION_REQUEST = 0
        val mapShops = MapShops(mutableMapOf())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        uidUser = FirebaseAuth.getInstance().currentUser!!.uid

        database = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance().reference

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { _ ->
            showShopDialog("Add new shop")
        }

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        // Read from the database
        database.child("users").child("$uidUser").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                adapterShop.clearDataset()
                keyIndexMap.clear()
                recyclerView.adapter = adapterShop // tak trzeba bo recycler view zostaje stare zdj jak go nie ma

                recyclerView.adapter?.notifyDataSetChanged()
                for (ds in dataSnapshot.children) {
                    val value = ds.getValue(Shop::class.java)
                    val name = value!!.name
                    val type = value.type
                    val radius = value.radius
                    val geo: Geo? = value.geo
                    val key = ds.key
                    val shop = Shop(name, type, radius, geo, key)
                    val index = adapterShop.getCollection().addShop(shop)
                    Log.d("---", "Key is: " + ds.key + ",idx:"+index)
                    keyIndexMap.put(key, index)
                    getImageFile(key)
                    recyclerView.adapter?.notifyItemInserted(index)
                }
                Log.d("---", "Value is: ")
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("---", "Failed to read value.", error.toException())
            }
        })

        keyIndexMap = mutableMapOf()
        adapterShop = ShopListAdapter(mapShops)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        recyclerView.adapter = adapterShop
        recyclerView.addOnItemTouchListener(RecyclerItemClickListener(this,
            recyclerView, object : RecyclerItemClickListener.OnItemClickListener {

            override fun onItemClick(view: View, position: Int) {
                showShopDialog("Edit shop", mapShops.getShop(position), position)
            }
            override fun onItemLongClick(view: View?, position: Int) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity);
                val inflater: LayoutInflater = LayoutInflater.from(this@MainActivity)
                val v: View = inflater.inflate(R.layout.delete_shop_dialog, null)

                builder.setView(v)
                builder.setPositiveButton("Yes", null)
                builder.setNegativeButton("Cancel", ({ dialog: DialogInterface, _: Int ->
                    dialog.cancel()
                }))
                val deleteDialog = builder.setCancelable(false).create()
                deleteDialog.show()
                deleteDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    mapShops.getMapShops()[position]?.apply {
                        val imageToDelete = storage.child("images/${this.key}")

                        if(this.hasPhoto) {
                            imageToDelete.delete()
                                .addOnSuccessListener {
                                    Log.d("---", "delete from storage")
                                    val sdFile = File(shopsDirectory.absolutePath+"/${this.key}.jpg")
                                    if (sdFile.exists()) {
                                        sdFile.delete()
                                        Log.d("---", "delete from sd")
                                    }
                                    removeFromDB(this.key)
                                }
                                .addOnFailureListener {
                                    Log.d("---", "fail delete from storage")
                                }
                        } else {
                            removeFromDB(this.key)
                        }
                    }
                    deleteDialog.dismiss()
                }
            }
        }))
    }

    private fun removeFromDB(keyShop: String?) {
        val query = database.child("users/$uidUser/$keyShop")
        query.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this@MainActivity, "Success to delete from database.", Toast.LENGTH_LONG).show()
                Log.d("---", "delete from database")
            }
            .addOnFailureListener {
                Toast.makeText(this@MainActivity, "Fail to delete from database: "+it.message, Toast.LENGTH_LONG).show()
                Log.d("---", "fail delete from database")
            }
    }

    private fun showShopDialog(titleDialog: String, shopVal: Shop? = null, position: Int = 0) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity);
        val inflater: LayoutInflater = LayoutInflater.from(this@MainActivity)
        val v: View = inflater.inflate(R.layout.add_shop_dialog, null)

        val nameShop: EditText = v.findViewById(R.id.editTextName) as EditText
        val typeShop: EditText = v.findViewById(R.id.editTextType) as EditText
        val radiusShop: EditText = v.findViewById(R.id.editTextRadius) as EditText
        val imageShop: ImageView = v.findViewById(R.id.imageViewMakePhoto) as ImageView
        address = v.findViewById(R.id.textViewAddress) as TextView

        shopVal?.apply {
            nameShop.text.insert(0, name)
            typeShop.text.insert(0, type)
            radiusShop.text.insert(0, radius.toString())
            if(hasPhoto) {
                val path = shopsDirectory.absolutePath + "/$key.jpg"
                file = File(path)
                if (file!!.exists()) {
                    val bitmap: Bitmap = BitmapFactory.decodeFile(file!!.absolutePath)
                    imageShop.setImageBitmap(bitmap)
                }
            }
            lat = geo!!.lat
            lon = geo.lon
            val geo = Geocoder(baseContext, Locale.getDefault())
            val addressToDisplay = geo.getFromLocation(lat, lon, 1)
            if (addressToDisplay.size > 0) {
                addressToDisplay[0].countryName
                address.text =
                    addressToDisplay[0].thoroughfare + " " + addressToDisplay[0].locality + ", " + addressToDisplay[0].countryName
                address.textSize = 14f
            }
            Log.d("---edit", address.toString())
        }

        val fabPhoto: FloatingActionButton = v.findViewById(R.id.fabPhoto)
        fabPhoto.setOnClickListener {
            val intentPhoto = Intent(this@MainActivity, CameraActivity::class.java)
            startActivity(intentPhoto)
        }

        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("---", "BroadcastReceiver")
                val path = intent?.getStringExtra("currentPhotoPath")
                file = File(path)
                if (file!!.exists()) {
                    var bitmap: Bitmap = BitmapFactory.decodeFile(file!!.absolutePath)
                    bitmap = rotateImage(file!!.absolutePath, bitmap)
                    imageShop.setImageBitmap(bitmap)
                    saveImageToExternalStorage(bitmap)
                }
                Log.d("---", "BroadcastReceiver"+path)
            }
        }, IntentFilter("com.example.favshops.PHOTO"))

        val fabLocal: FloatingActionButton = v.findViewById(R.id.fabMaps)
        fabLocal.setOnClickListener {
            val intentLocation
                    = Intent(this@MainActivity, MapsActivity::class.java)
            if(lat != 0.0 && lon != 0.0) {
                intentLocation.putExtra("LAT", lat)
                intentLocation.putExtra("LON", lon)
            }
            startActivityForResult(intentLocation, LOCATION_REQUEST)
        }

        builder.setView(v)
            .setTitle(titleDialog)

        builder.setPositiveButton("Ok", null)
        builder.setNegativeButton("Cancel", ({ dialog: DialogInterface, _: Int ->
            dialog.cancel()
            if(shopVal == null) {
                if(file != null && file!!.exists()) file!!.delete()
            }
        }))
        val showDialog = builder.setCancelable(false).create()
        showDialog.show()
        showDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val nameShopSend: String = nameShop.text.toString()
            val typeShopSend: String = typeShop.text.toString()
            val radiusShopSend: String = radiusShop.text.toString()

            if (validShopInfo(nameShop, typeShop, radiusShop)) {
                val key = writeNewShop(uidUser, nameShopSend, typeShopSend, radiusShopSend.toInt(),
                    if(shopVal != null) shopVal.key else null )
                file?.apply {
                    putImageFile(this, key)
                    file = null
                    setHasPhoto(key)
                    recyclerView.adapter?.notifyItemChanged(position)
                }
                showDialog.dismiss()
                lat = 0.0
                lon = 0.0
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("---", "ONRESUME")

        if(!shopsDirectory.exists()) {
            shopsDirectory.mkdirs()
        }
    }

    private fun putImageFile(file: File, keyShop: String?) {
        if(keyShop == null) {
            return
        }
        val fileUri: Uri = Uri.fromFile(file)
        val storageRef: StorageReference = storage.child("images/$keyShop")
        storageRef.putFile(fileUri)
            .addOnSuccessListener( OnSuccessListener {
            })
            .addOnFailureListener( OnFailureListener {
            })
    }

    private fun getImageFile(keyShop: String?) {
        val localFile = File(storageDir.absolutePath+"/shops/${keyShop}.jpg")
        if (localFile.exists()) {
            setHasPhoto(keyShop)
            Log.d("---", "setHasPhotoSD, keyShop: " +keyShop)

            return
        }
        val storageRef: StorageReference = storage.child("images/${keyShop}")
        storageRef.getFile(localFile)
            .addOnSuccessListener( OnSuccessListener {
                setHasPhoto(keyShop).also {
                    it?.apply {
                        adapterShop.notifyItemChanged(this)
                        Log.d("---", "addOnSuccessListener, idx: " +this)
                    }
                }
            }).addOnFailureListener( OnFailureListener() {
                Log.d("---", "addOnFailureListener")
            });
    }

    private fun setHasPhoto(keyShop: String?) : Int? {
        val idx = keyIndexMap[keyShop]
        idx?.apply {
            adapterShop.getCollection().getMapShops()[this]?.apply {
                hasPhoto = true
                Log.d("---", "hasPhoto = true"+idx)
            }
        }
        return idx
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == LOCATION_REQUEST && resultCode == 1) {
            data?.let {
                lat = it.getDoubleExtra("lat", 0.0)
                lon = it.getDoubleExtra("lon", 0.0)

                val geo = Geocoder(baseContext, Locale.getDefault())
                val addressToDisplay = geo.getFromLocation(lat, lon, 1)
                if (addressToDisplay.size > 0) {
                    address.text =
                        addressToDisplay[0].thoroughfare + " " + addressToDisplay[0].featureName +
                                ", " + addressToDisplay[0].postalCode + " " + addressToDisplay[0].locality +
                                ", " + addressToDisplay[0].countryName
                    address.textSize = 14f
                }
                Log.d("---", address.toString())
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("---", "onActivityResult")
    }

    private fun validShopInfo(nameShopSend: EditText, typeShopSend: EditText, radiusShopSend: EditText): Boolean {
        var valid = true
        if (TextUtils.isEmpty(nameShopSend.text.toString())) {
            nameShopSend.error = REQUIRED
            valid = false
        } else {
            nameShopSend.error = null
        }

        if (TextUtils.isEmpty(typeShopSend.text.toString())) {
            typeShopSend.error = REQUIRED
            valid = false
        } else {
            typeShopSend.error = null
        }

        if (TextUtils.isEmpty(radiusShopSend.text.toString())) {
            radiusShopSend.error = REQUIRED
            valid = false
        } else if (radiusShopSend.text.toString().toIntOrNull() == null) {
            radiusShopSend.error = "Put a number"
            valid = false
        } else {
            radiusShopSend.error = null
        }

        return valid
    }

    private fun writeNewShop(uid: String?, nameShop: String, typeShop: String, radiusShop: Int, keyShop: String?): String? {
        var key: String? = null
        if(keyShop != null) {
            key = keyShop
        } else {
            key = database.child("users").child("shops").push().key
        }

        if(key == null) {
            Log.d("---KEY IS NULL", "TRUE")
            Log.d("---KEY IS NULL", "TRUE")
            return null
        }
        val shop = Shop(nameShop, typeShop, radiusShop, Geo(lat, lon))
        val shopValues = shop.toMap()
        val childUpdates = HashMap<String, Any>()
        childUpdates["/users/$uid/$key"] = shopValues
        database.updateChildren(childUpdates)

        val renameFile = File(storageDir.absolutePath + "/shops/$key.jpg")
        if(file != null && renameFile.absolutePath != file!!.absolutePath) {
            if (file!!.renameTo(renameFile)) {
                file = renameFile
            }
            else {
                Log.d("---", "CANNOT RENAME\n" + file!!.absoluteFile+"\npath:"+file!!.absolutePath)
            }
        }

        return key
    }

    @Throws(IOException::class)
    private fun rotateImage(imagePath: String, source: Bitmap): Bitmap {
        val ei = ExifInterface(imagePath)
        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> return rotateImageByAngle(source, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> return rotateImageByAngle(source, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> return rotateImageByAngle(source, 270f)
        }
        return source
    }

    private fun rotateImageByAngle(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun saveImageToExternalStorage(bitmap:Bitmap) {
        try {
            // Get the file output stream
            val stream: OutputStream = FileOutputStream(file)

            // Compress the bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream)

            // Flush the output stream
            stream.flush()

            // Close the output stream
            stream.close()
        } catch (e: IOException){ // Catch the exception
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
//        else {
//            super.onBackPressed()
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_location -> {
                val intent: Intent = Intent(this@MainActivity, ShopLocationActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_logout -> {
                logout()
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)

        return false
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }
}