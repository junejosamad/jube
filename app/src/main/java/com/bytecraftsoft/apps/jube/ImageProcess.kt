package com.bytecraftsoft.apps.jube

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ImageProcess(private val userList: List<Post>) : RecyclerView.Adapter<ImageProcess.UserViewHolder>() {


    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.postOwnerName)
        val userDescription: TextView = itemView.findViewById(R.id.postDesc)
        val profileImage: ImageView = itemView.findViewById(R.id.post)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.posts, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.userName.text = user.owner
        holder.userDescription.text = user.desc

        // Use Glide or Picasso to load the image
        Glide.with(holder.itemView.context)
            .load(user.url)
            .into(holder.profileImage)
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}

class ProfileImageProcess(private val postList: MutableList<String>) :
    RecyclerView.Adapter<ProfileImageProcess.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        val profileImage: ImageView = itemView.findViewById(R.id.post)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        Log.d("ProfileImageProcess", "onCreateViewHolder called")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_uploads_posts, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        Log.d("ProfileImageProcess", "onBindViewHolder called for position: $position")
        val user = postList[position]

        // Use Glide or Picasso to load the image
        Glide.with(holder.itemView.context)
            .load(user)
            .into(holder.profileImage)
    }

    override fun getItemCount(): Int {
        Log.d("ProfileImageProcess", "getItemCount called with size: ${postList.size}")
        return postList.size
    }
}



