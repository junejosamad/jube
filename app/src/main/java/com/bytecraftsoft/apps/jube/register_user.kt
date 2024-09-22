package com.bytecraftsoft.apps.jube

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bytecraftsoft.apps.jube.ui.login.LoginActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.firestore

class register_user : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_register_user)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val enrollment = findViewById<EditText>(R.id.register_enrollment)
        val name = findViewById<EditText>(R.id.register_name)
        val password = findViewById<EditText>(R.id.register_password)
        val registerBtn = findViewById<Button>(R.id.registerBtn)



        registerBtn.setOnClickListener {
            val enrollmentText = enrollment.text.toString().trim()
            val nameText = name.text.toString().trim()
            val passwordText = password.text.toString().trim()

            if (enrollmentText.isEmpty() || nameText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            registerUser(enrollmentText, nameText, passwordText)
        }
    }

    private fun registerUser(enrollmentText: String, nameText: String, passwordText: String) {
        val email = "$enrollmentText@student.bahria.edu.pk"
        auth.createUserWithEmailAndPassword(email, passwordText)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    val dbUsers = hashMapOf(
                        "uid" to user?.uid,
                        "enrollment" to enrollmentText,
                        "name" to nameText
                    )
                    db.collection("users").document(user?.uid ?: "")
                        .set(dbUsers)
                    user?.let {
                        updateUserProfile(it, nameText)
                        sendEmailVerification(it)
                    }
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateUserProfile(user: FirebaseUser, nameText: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(nameText)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User profile updated.")
                }
            }
    }

    private fun sendEmailVerification(user: FirebaseUser) {

        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email verification sent.")
                    Toast.makeText(this, "Verification email sent to ${user.email}", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Log.e(TAG, "sendEmailVerification failed.", task.exception)
                    Toast.makeText(this, "Failed to send verification email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
