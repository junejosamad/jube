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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage

class selectedPost : Fragment() {

    companion object {
        fun newInstance() = selectedPost()
    }

    //private val viewModel: SelectedPostViewModel by viewModels()
    private val STORAGE_PERMISSION_CODE = 100
    private val PICK_MEDIA_REQUEST = 1
    private val storage = Firebase.storage
    private val storageRef = storage.reference
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private var postCounter: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_selected_post, container, false)
        val title = view.findViewById<EditText>(R.id.selected_post_title)
        val desc = view.findViewById<EditText>(R.id.selected_post_desc)
        val post = view.findViewById<Button>(R.id.selected_post_btn)
        post.setOnClickListener {

        }

        return inflater.inflate(R.layout.fragment_selected_post, container, false)
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
                val mimeType = context?.contentResolver?.getType(it)
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
                        Toast.makeText(context, "Upload successful", Toast.LENGTH_SHORT).show()
                    }
                }


            }
        }.addOnFailureListener {
            // Handle failure
            Toast.makeText(context, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
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
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
                } else {
                    selectMedia()
                }
            }
            else -> {
                // Android 9 and below: Request READ and WRITE permissions
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(
                        context as Activity, arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
                } else {
                    selectMedia()
                }
            }
        }
    }


}