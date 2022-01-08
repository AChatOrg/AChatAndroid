package com.hyapp.achat.model.entity

class Login(var operation: String = "", name: String = "", bio: String = "", gender: Byte = GENDER_MALE
) : Person(name, bio, gender)