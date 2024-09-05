package com.bytecraftsoft.apps.jube

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bytecraftsoft.apps.jube.ui.login.LoginActivity

class home : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val video = findViewById<Button>(R.id.videos)
        val profile = findViewById<Button>(R.id.profile)
        val home = findViewById<Button>(R.id.mainView)

        video.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        profile.setOnClickListener {
            startActivity(Intent(this, User_Profile::class.java))
        }

    }
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if((currentUser == null) || !currentUser.isEmailVerified){
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

}