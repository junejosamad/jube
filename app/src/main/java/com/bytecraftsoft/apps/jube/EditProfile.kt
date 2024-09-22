package com.bytecraftsoft.apps.jube

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import de.hdodenhof.circleimageview.CircleImageView

class EditProfile : AppCompatActivity() {
    private lateinit var name: TextInputEditText

    private lateinit var bio: TextInputEditText
    private lateinit var imageView: CircleImageView

    private val STORAGE_PERMISSION_CODE = 100
    private val PICK_MEDIA_REQUEST = 1

    private val storage = Firebase.storage
    private val storageRef = storage.reference

    private val database = FirebaseFirestore.getInstance()

    //private val auth = Firebase.auth
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        name = findViewById(R.id.name)
        bio = findViewById(R.id.bio)
        imageView = findViewById(R.id.circleImageView)
        Glide.with(this).load(userdata.profileURL).into(imageView)

        name.setText(userdata.username)
        bio.setText(userdata.bio)
    }

    fun save() {
        val getName = name.text.toString()
        val getBio = bio.text.toString()
        val currentUser = auth.currentUser

        //This object update profiles with selected field
        val update = hashMapOf<String, Any>(
            "name" to getName,
            "bio" to getBio
        )

        val dbRef = database.collection("users").document(currentUser?.uid ?: "")
        dbRef.update(update).addOnSuccessListener {
            currentUser?.updateProfile(
                userProfileChangeRequest {
                    displayName = getName
                }
            )
            Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, User_Profile::class.java))
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
        }

    }

    fun selectImg() {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_MEDIA_REQUEST && resultCode == Activity.RESULT_OK) {
            val selectedMediaUri = data?.data

            selectedMediaUri?.let {
                val mimeType = contentResolver.getType(it)
                if (mimeType?.startsWith("image") == true) {
                    handleImage(it)
                }
            }
        }
    }

    private fun handleImage(uri: Uri) {
        // Set the selected image URI to the ImageView
        //imageView.setImageURI(uri)

        // Create a reference for the image in Firebase Storage
        val imageRef =
            storageRef.child("images/${uri.lastPathSegment}") // Use lastPathSegment for a cleaner filename

        // Upload the file to Firebase Storage using the uri directly
        val uploadTask = imageRef.putFile(uri)



        // Listen for successful upload
        uploadTask.addOnSuccessListener {
            // Get the download URL of the uploaded file
            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                // Optionally display the uploaded image using its download URL

                // Use Glide to load the image into the ImageView
                Glide.with(this)
                    .load(downloadUri) // URL of the image
                    .into(imageView) // Your ImageView

                val imageUrl = downloadUri.toString()
                val currentUser = auth.currentUser
                val update = hashMapOf<String, Any>(
                    "profileURL" to imageUrl
                )
                val dbRef = database.collection("users").document(currentUser?.uid ?: "")
                dbRef.update(update).addOnSuccessListener {
                    userdata.profileURL = imageUrl
                    Toast.makeText(this, "Profile Image Updated", Toast.LENGTH_SHORT).show()

                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to update profile Image", Toast.LENGTH_SHORT).show()
                }
                Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            // Handle failure
            Toast.makeText(this, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
        }
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

    private fun selectMedia() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/* "  // To allow both images and videos
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*"))
        }
        startActivityForResult(intent, PICK_MEDIA_REQUEST)
    }
}
