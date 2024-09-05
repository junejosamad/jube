package com.bytecraftsoft.apps.jube

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bytecraftsoft.apps.jube.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseUser

class email_verify : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_email_verify)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val checkVerificationButton = findViewById<Button>(R.id.checkVerificationButton)
        checkVerificationButton.setOnClickListener {
            val user = auth.currentUser
            user?.sendEmailVerification()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Email Sent", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }else{
                    Toast.makeText(this, "Failed to send link: $task", Toast.LENGTH_SHORT).show()
            }
        }

        if(user?.isEmailVerified == true){
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

    }

}

//    private fun sendEmailVerification(user: FirebaseUser) {
//        user.sendEmailVerification()
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    Log.d(TAG, "Email verification sent.")
//                    Toast.makeText(this, "Verification email sent to ${user.email}", Toast.LENGTH_SHORT).show()
//                } else {
//                    Log.e(TAG, "sendEmailVerification failed.", task.exception)
//                    Toast.makeText(this, "Failed to send verification email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
//                }
//            }
//    }
}
