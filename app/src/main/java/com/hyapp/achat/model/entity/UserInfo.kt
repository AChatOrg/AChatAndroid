package com.hyapp.achat.model.entity

data class UserInfo(

    var viewsCount: String = "0",
    var likesCount: String = "0",
    var friendsCount: String = "0",

    var friendList: List<User> = mutableListOf(),
    var viewerList: List<User> = mutableListOf()
)