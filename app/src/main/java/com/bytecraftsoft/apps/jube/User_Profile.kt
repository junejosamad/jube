package com.bytecraftsoft.apps.jube

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.Manifest
import android.app.Fragment
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bytecraftsoft.apps.jube.ui.login.LoginActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import kotlin.properties.Delegates

var postsURL = mutableListOf<String>()
class User_Profile : BaseActivity() {

    private val STORAGE_PERMISSION_CODE = 100
    private val PICK_MEDIA_REQUEST = 1
    private lateinit var imageView: ImageView
    private val storage = Firebase.storage
    private val storageRef = storage.reference
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private var postCounter: Int = 0

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter:ProfileImageProcess
    private lateinit var postsNumber: TextView
    private lateinit var profileBio: TextView
    private lateinit var profileImg: CircleImageView

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home)

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.navigation_profile
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    // Handle Home button click
                    startActivity(Intent(this, home::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    overridePendingTransition(0, 0)
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
                    true
                }

                else -> { false }
            }
        }

        //fragment = findViewById(R.id.fragmentContainerView)

        val profileName = findViewById<TextView>(R.id.profileName)
        profileImg = findViewById(R.id.profileIMG)
        Glide.with(this).load(userdata.profileURL).into(profileImg)
        profileName.text = userdata.username
        profileBio = findViewById(R.id.profileBio)
        profileBio.text = userdata.bio
        postsNumber = findViewById(R.id.postCounter)
        postsNumber.text = "Posts\n${userdata.postCounter}"

        //It refresh the page and run userdata function which belongs to home class
        //User data update data from firebase and save in to data class
        val refresh: SwipeRefreshLayout = findViewById(R.id.refresh_layout_profile)
        refresh.setOnRefreshListener {
            Toast.makeText(this, "Refresed", Toast.LENGTH_SHORT).show()
            try {
                home().userData()
                refresh.isRefreshing = false
                startActivity(Intent(this, User_Profile::class.java))
                finish()
                overridePendingTransition(0, 0)
            } catch (e: Exception) {
                Log.e("Profile Activity", "Error refreshing: ${e.message}")
            }
            //refresh.isRefreshing = false
        }


        Log.d("Profile Activity", "Activity Opened")
        if(postsURL.isEmpty()) readUserImages()
        recyclerView = findViewById(R.id.recycler_view_posts)
        adapter = ProfileImageProcess(postsURL)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        Log.d("Profile Activity", "Images Read")


        //postsNumber.text = "Posts\n$postCounter"
        Log.d("Profile Activity", "Posts Number: $postCounter Updated")


        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/
        /*val username = findViewById<TextView>(R.id.display_username)
        val selectImg = findViewById<TextView>(R.id.select_img)
        val user = auth.currentUser
        imageView = findViewById(R.id.imageView)


        user?.let {
            val name = user.displayName
            username.text = name
        }
        val logout = findViewById<TextView>(R.id.logout)
        logout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        selectImg.setOnClickListener{

            checkPermissionsAndSelectMedia()
        }*/

    }

    fun selectMedia() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/* video/*"  // To allow both images and videos
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        }
        startActivityForResult(intent, PICK_MEDIA_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_MEDIA_REQUEST && resultCode == Activity.RESULT_OK) {
            val selectedMediaUri = data?.data

            selectedMediaUri?.let {
                val mimeType = contentResolver.getType(it)
                if (mimeType?.startsWith("image") == true) {
                    handleImage(it)
                } else if (mimeType?.startsWith("video") == true) {
                    handleVideo(it)
                }
            }
        }
    }

    private fun handleImage(uri: Uri) {

        //LayoutInflater.from(this).inflate(R.layout.selected_media, null)
        // Set the selected image URI to the ImageView
        //imageView.setImageURI(uri)

        // Create a reference for the image in Firebase Storage
        val imageRef = storageRef.child("images/${uri.lastPathSegment}") // Use lastPathSegment for a cleaner filename

        // Upload the file to Firebase Storage using the uri directly
        val uploadTask = imageRef.putFile(uri)

        // Listen for successful upload
        uploadTask.addOnSuccessListener {
            // Get the download URL of the uploaded file
            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                // Optionally display the uploaded image using its download URL

                // Use Glide to load the image into the ImageView
                /*Glide.with(this)
                    .load(downloadUri) // URL of the image
                    .into()*/ // Your ImageView

                val imageUrl = downloadUri.toString()
                val user = auth.currentUser
                val images = hashMapOf(
                    "url" to imageUrl,
                    "title" to uri.lastPathSegment,
                    "owner" to userdata.username,
                )

                db.collection("users").document(user?.uid ?: "").collection("images").add(images).addOnSuccessListener {
                    db.collection("images").add(images).addOnSuccessListener {
                        Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show()
                    }
                }


            }
        }.addOnFailureListener {
            // Handle failure
            Toast.makeText(this, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun handleVideo(uri: Uri) {
        // Example: videoView.setVideoURI(uri)

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectMedia()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun checkPermissionsAndSelectMedia() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Android 11 and above: No need for storage permissions for media picker
                selectMedia()
            }
            Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> {
                // Android 10: Only request READ_EXTERNAL_STORAGE
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
                } else {
                    selectMedia()
                }
            }
            else -> {
                // Android 9 and below: Request READ and WRITE permissions
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this, arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
                } else {
                    selectMedia()
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun readUserImages(){
        //postCounter = 0
        db.collection("users").document(auth.currentUser?.uid ?: "").collection("images")
            .get()
            .addOnSuccessListener { result ->
                for (document in result.reversed() ) {
                    Log.d("Firestore", "${document.id} => ${document.data}")
                    val imageUrl = document.getString("url")
                    if (imageUrl != null) {
                        postsURL.add(imageUrl)


                    }
                }
                postCounter = result.size()
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)

            }
    }

    fun editProfile(view: android.view.View){
        startActivity(Intent(this, EditProfile::class.java))
    }

    fun userData(){
        db.collection("users").document(auth.currentUser?.uid ?: "").get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val bio = document.getString("bio")
                    //val profileBio = findViewById<TextView>(R.id.profileBio)
                    profileBio.text = bio
                    Log.d("Firestore", "Bio: $bio")
                    postCounter = document.getLong("postCounter")?.toInt() ?: 0
                    Log.d("Post Counter", "$postCounter")
                    postsNumber.text = "Posts\n$postCounter"
                }

            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
            }
    }

    fun restart(){
        startActivity(Intent(this, User_Profile::class.java))
        finish()
    }

    fun postBtn(view: android.view.View){
        checkPermissionsAndSelectMedia()

    }



}