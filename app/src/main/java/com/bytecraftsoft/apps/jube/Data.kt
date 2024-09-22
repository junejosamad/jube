package com.bytecraftsoft.apps.jube

data class Post(
    val url: String = "",
    val title: String = "",
    val owner: String = "",
    val desc: String = ""
)

data class UserData(
    var username: String = "",
    var uid: String = "",
    var bio: String = "",
    var postCounter: Int = 0,
    var followers: List<String> = emptyList(),
    var following: List<String> = emptyList(),
    var images: List<String> = emptyList(),
    var followersCount: Int = 0,
    var followingCount: Int = 0,
    var profileURL:String = ""

)
