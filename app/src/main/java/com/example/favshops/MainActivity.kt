package com.example.favshops

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
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
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.EditText
import com.example.favshops.controller.ShopListAdapter
import com.example.favshops.model.ListShops
import com.example.favshops.model.Shop
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterShop: ShopListAdapter

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { _ ->
            val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity);
            val inflater: LayoutInflater = LayoutInflater.from(this@MainActivity)

            val v: View = inflater.inflate(R.layout.add_shop_dialog, null)
            val nameShop: EditText = v.findViewById(R.id.editTextName) as EditText
            val typeShop: EditText = v.findViewById(R.id.editTextType) as EditText
            val radiusShop: EditText = v.findViewById(R.id.editTextRadius) as EditText
            val fabPhoto: FloatingActionButton = v.findViewById(R.id.fabPhoto)
            val fabLocal: FloatingActionButton = v.findViewById(R.id.fabMaps)
            fabPhoto.setOnClickListener {
                var intentPhoto: Intent = Intent(this@MainActivity, CameraActivity::class.java)
                startActivity(intentPhoto)
            }
            fabLocal.setOnClickListener {
                var intentLocation: Intent = Intent(this@MainActivity, MapsActivity::class.java)
                startActivity(intentLocation)
            }
            builder.setView(v)
            builder.setPositiveButton("Ok", ({ _: DialogInterface, _: Int ->
                val nameShopSend: String = nameShop.text.toString()
                val typeShopSend: String = typeShop.text.toString()
                val radiusShopSend: Double = radiusShop.text.toString().toDouble()

                var uidUser = FirebaseAuth.getInstance().currentUser?.uid
                writeNewShop(uidUser, nameShopSend, typeShopSend, radiusShopSend)
            }))
            builder.setNegativeButton("Cancel", ({ _: DialogInterface, _: Int ->
            }))
            builder.setCancelable(false).create().show()
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
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (ds in dataSnapshot.children) {
                    val value = ds.getValue(Shop::class.java)
                    val name = value!!.name
                    val type = value!!.type
                    val radius = value!!.radius
                    val shop: Shop = Shop(name, type, radius)
                    adapterShop.getCollection().addShop(shop)
                    recyclerView.adapter = adapterShop
                }
//                val value = dataSnapshot.getValue(String::class.java)
                Log.d("aaaaaaaaaaa", "Value is: ")
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Hello", "Failed to read value.", error.toException())
            }
        })

        adapterShop = ShopListAdapter(ListShops(ArrayList()))
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        recyclerView.adapter = adapterShop
    }

    private fun writeNewShop(uid: String?, nameShop: String, typeShop: String, radiusShop: Double) {
        val key = database.child("users").child("shops").push().key
        if(key == null) {
            Log.d("KEY IS NULL", "TRUE")
            return
        }
        val shop = Shop(nameShop, typeShop, radiusShop)
        val shopValues = shop.toMap()
        val childUpdates = HashMap<String, Any>()
        childUpdates["/users/$uid/$key"] = shopValues
        database.updateChildren(childUpdates)
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
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
//            R.id.nav_home -> {
//                // Handle the camera action
//            }
//            R.id.nav_gallery -> {
//
//            }
//            R.id.nav_slideshow -> {
//
//            }
//            R.id.nav_tools -> {
//
//            }
//            R.id.nav_share -> {
//
//            }
//            R.id.nav_send -> {
//
//            }
            R.id.nav_location -> {
                var intent: Intent = Intent(this@MainActivity, MapsActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_logout -> {
                logout()
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        var intent: Intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }
}