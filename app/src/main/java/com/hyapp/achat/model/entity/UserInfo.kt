package com.hyapp.achat.model.entity

data class UserInfo(

    var viewsCount: Long = 0,
    var likesCount: Long = 0,
    var friendsCount: Int = 0,

    var friendList: List<User> = mutableListOf(),
    var viewerList: List<User> = mutableListOf(),

    var likedByMe: Boolean = false
)