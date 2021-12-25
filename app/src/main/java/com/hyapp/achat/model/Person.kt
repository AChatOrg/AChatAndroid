package com.hyapp.achat.model

import io.objectbox.annotation.BaseEntity

@BaseEntity
open class Person(
        var name: String = "",
        var bio: String? = null,
        var gender: Byte = GENDER_MALE
) {

    companion object {
        const val GENDER_MALE: Byte = 1
        const val GENDER_FEMALE: Byte = 2
        const val GENDER_MIXED: Byte = 3
    }
}