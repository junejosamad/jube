package com.bytecraftsoft.apps.jube

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytecraftsoft.apps.jube.ui.login.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.messaging
import com.google.firebase.messaging.remoteMessage
import kotlinx.coroutines.tasks.await
import java.util.concurrent.atomic.AtomicInteger

lateinit var bottomNavigationView: BottomNavigationView
var userList = mutableListOf<Post>()
var userdata = UserData()
//var postsURL = mutableListOf<String>()
class home : BaseActivity() {

    private var db = Firebase.firestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageProcess
    //var userList = mutableListOf<Post>()


    @SuppressLint( "WrongViewCast", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.mainact)

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.menu.findItem(R.id.navigation_home).isChecked = true
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    // Handle Home button click

                    true
                }

                R.id.navigation_notifications -> {
                    // Handle Notifications button click
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    overridePendingTransition(0, 0)
                    true
                }

                R.id.navigation_profile -> {
                    // Handle Profile button click
                    Log.d("Home", "BottomNavigationView Profile button click")
                    startActivity(Intent(this, User_Profile::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    overridePendingTransition(0, 0)
                    true
                }

                else -> { false }
            }
        }


        Log.d("Home", "BottomNavigationView before reading URLS")
        userData()
        if(userList.isEmpty()) readImageUrls()
        //if(postsURL.isEmpty()) User_Profile().readUserImages()
        Log.d("Home", "BottomNavigationView after reading URLS")

        recyclerView = findViewById(R.id.recycler_view_posts)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ImageProcess(userList)
        recyclerView.adapter = adapter


    }


    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if ((currentUser == null) || !currentUser.isEmailVerified) {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun readImageUrls() {

        db.collection("images")
            .get()
            .addOnSuccessListener { result ->
                for (document in result.reversed()) {
                    // Convert Firestore document to User object
                    val post = document.toObject(Post::class.java)
                    userList.add(post)
                }
                // Set the adapter with the fetched data
                val adapter = ImageProcess(userList)
                recyclerView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
            }

    }

    fun userData(){
        //var userdata = UserData()
        db.collection("users").document(auth.currentUser?.uid ?: "").get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val bio = document.getString("bio")
                    userdata.bio = bio.toString()
                    val postCounter = document.getLong("postCounter")?.toInt() ?: 0

                    //postsNumber.text = "Posts\n$postCounter"
                    userdata.bio = bio.toString()
                    userdata.postCounter = postCounter
                    userdata.username = document.getString("name").toString()
                    userdata.uid = document.getString("uid").toString()
                    userdata.profileURL = document.getString("profileURL").toString()
                }

            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
            }
    }


}