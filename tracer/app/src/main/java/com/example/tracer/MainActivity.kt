package com.example.tracer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso


class MainActivity : AppCompatActivity() {
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var navController: NavController
    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationView: NavigationView
    lateinit var nav_drawer_username: TextView
    lateinit var nav_drawer_profile_picture: ImageView
    val historyViewModel: HistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firebaseAuth = FirebaseAuth.getInstance()

        if (!isLoggedIn())
        {
            login()
        }


        // Variables
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        navigationView = findViewById(R.id.nav_view)

        val headerView = navigationView.getHeaderView(0)
        nav_drawer_username = headerView.findViewById(R.id.nav_drawer_username)
        nav_drawer_profile_picture = headerView.findViewById(R.id.nav_drawer_profile_picture)

        val currentUser = firebaseAuth.currentUser
        currentUser?.let { user ->
            nav_drawer_username.text = user.email
            val db = FirebaseDatabase.getInstance().reference
            val userRef = db.child("users").child(user.uid)

            userRef.child("profileImageUrl").get().addOnSuccessListener { dataSnapshot ->
                val profileImageUrl = dataSnapshot.value as? String
                profileImageUrl?.let {
                    Picasso.get()
                        .load(it)
                        .placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .into(nav_drawer_profile_picture);

                }
            }
        }

        // Navigation
        val appBarConfig = AppBarConfiguration(navController.graph, drawerLayout)
        toolbar.setupWithNavController(navController, appBarConfig)
        navigationView.setupWithNavController(navController)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {


        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    private fun isLoggedIn(): Boolean {
        val currentUser = firebaseAuth.currentUser
        return currentUser != null
    }


    private fun login() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}