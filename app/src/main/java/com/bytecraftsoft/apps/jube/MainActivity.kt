package com.bytecraftsoft.apps.jube

import VideoAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytecraftsoft.apps.jube.ui.login.LoginActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

var auth: FirebaseAuth = Firebase.auth
class MainActivity : AppCompatActivity() {
    private var db = Firebase.firestore
    private lateinit var recyclerView: RecyclerView
    private var videoUrls: MutableList<String> = mutableListOf()
    private lateinit var adapter: VideoAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        readVideoUrls()
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = VideoAdapter(videoUrls, recyclerView)
        recyclerView.adapter = adapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                (recyclerView.adapter as? VideoAdapter)?.checkCurrentVideoPlaying()
                snapToFullScreen(dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    (recyclerView.adapter as? VideoAdapter)?.checkCurrentVideoPlaying()
                    snapToFullScreen(0) // Pass 0 to handle cases when scrolling stops
                }
            }

            private fun snapToFullScreen(dy: Int) {
                val layoutManager = recyclerView.layoutManager as? androidx.recyclerview.widget.LinearLayoutManager
                layoutManager?.let {
                    val firstVisibleItemPosition = it.findFirstVisibleItemPosition()
                    val lastVisibleItemPosition = it.findLastVisibleItemPosition()

                    for (i in firstVisibleItemPosition..lastVisibleItemPosition) {
                        val holder = recyclerView.findViewHolderForAdapterPosition(i) as? VideoAdapter.VideoViewHolder
                        holder?.let { viewHolder ->
                            val visibleHeight = getVisibleHeight(viewHolder.itemView, recyclerView.context)
                            val itemHeight = viewHolder.itemView.height

                            if (dy < 0 && visibleHeight in 1 until itemHeight && viewHolder.itemView.top < 0) {
                                recyclerView.smoothScrollBy(0, viewHolder.itemView.top)
                                //break
                            }

                            if (dy > 0 && visibleHeight in 1 until itemHeight && viewHolder.itemView.bottom > itemHeight) {
                                recyclerView.smoothScrollBy(0, viewHolder.itemView.top)
                                //break
                            }

//                            if (visibleHeight == itemHeight) {
//                                playVideoAt(i)
//                                //break
//                            }
                        }
                    }
                }
            }

            private fun getVisibleHeight(view: View, context: Context): Int {
                val itemRect = Rect()
                view.getGlobalVisibleRect(itemRect)
                return itemRect.bottom - itemRect.top
            }
        })




    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Toast.makeText(this, "User is: $currentUser", Toast.LENGTH_SHORT).show()
            reload()
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun reload() {
        // Implement your reload logic here
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun readVideoUrls() {
        db.collection("videos")
            .get()
            .addOnSuccessListener { result ->
                videoUrls.clear()
                for (document in result) {
                    val videoUrl = document.getString("url")
                    if (videoUrl != null) {
                        videoUrls.add(videoUrl)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.d("MainActivity", "Error getting documents: ", exception)
            }
    }



}
