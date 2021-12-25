package com.hyapp.achat.kotlinlearning

open class User(var firstName: String?, var lastName: String? = null)

fun a(){
    var user = User("","")
    var user2 = User(firstName = "ali", lastName = "ahmadi")

}
