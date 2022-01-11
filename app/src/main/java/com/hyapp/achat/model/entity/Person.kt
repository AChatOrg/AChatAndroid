package com.hyapp.achat.model.entity

import android.os.Bundle
import io.objectbox.annotation.BaseEntity

@BaseEntity
abstract class Person(var name: String = "", var bio: String? = null, var gender: Byte = GENDER_MALE) {

    companion object {
        const val EXTRA_NAME = "name"
        const val EXTRA_BIO = "bio"
        const val EXTRA_GENDER = "gender"

        const val GENDER_MALE: Byte = 1
        const val GENDER_FEMALE: Byte = 2
        const val GENDER_MIXED: Byte = 3
    }


    constructor(bundle: Bundle) : this(
            bundle.getString(EXTRA_NAME) ?: "",
            bundle.getString(EXTRA_BIO),
            bundle.getByte(EXTRA_GENDER)
    )

    open val bundle: Bundle
        get() {
            return Bundle().apply {
                putString(EXTRA_NAME, name)
                putString(EXTRA_BIO, bio)
                putByte(EXTRA_GENDER, gender)
            }
        }

    open fun same(person: Person): Boolean {
        return name == person.name
                && bio == person.bio
                && gender == person.gender
    }
}