package com.bytecraftsoft.apps.jube

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        Log.d("BaseActivity", "BottomNavigationView: $bottomNavigationView")
        if (bottomNavigationView == null) {
            Log.d("BaseActivity", "BottomNavigationView is null")
        } else {

            bottomNavigationView?.setOnNavigationItemSelectedListener { item ->
                // Handle item selection
                when (item.itemId) {
                    R.id.navigation_home -> {
                        startActivity(Intent(this, com.bytecraftsoft.apps.jube.home::class.java))
                        Log.d("BaseActivity", "BottomNavigationView Home: $bottomNavigationView")
                        true
                    }

                    R.id.navigation_notifications -> {
                        startActivity(Intent(this, MainActivity::class.java))
                        Log.d("BaseActivity", "BottomNavigationView Watch: $bottomNavigationView")
                        true
                    }

                    R.id.navigation_profile -> {
                        startActivity(
                            Intent(
                                this,
                                com.bytecraftsoft.apps.jube.User_Profile::class.java
                            )
                        )
                        Log.d("BaseActivity", "BottomNavigationView Profile: $bottomNavigationView")
                        true
                    }

                    else -> false
                }
                Log.d("BaseActivity", "BottomNavigationView: $bottomNavigationView")
                true
            }
        }
        Log.d("BaseActivity", "BottomNavigationView After SetOnNavigationItemSelectedListener: $bottomNavigationView")
    }
}
