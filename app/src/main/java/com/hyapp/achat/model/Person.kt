package com.hyapp.achat.model

import android.os.Bundle
import com.hyapp.achat.model.utils.PersonUtils
import io.objectbox.annotation.BaseEntity

@BaseEntity
abstract class Person {

    var name: String = ""
    var bio: String? = null
    var gender: Byte = GENDER_MALE
        set(value) {
            field = value
            setupGenderCircleRes(field)
        }

    @Transient
    var genderCircleRes: Int = PersonUtils.GENDER_PEOPLE_CIRCLE_MALE_BG_RES

    init {
        setupGenderCircleRes(gender)
    }

    constructor(name: String = "", bio: String? = null, gender: Byte = GENDER_MALE) {
        this.name = name
        this.bio = bio
        this.gender = gender
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

    fun setupGenderCircleRes(gender: Byte) {
        when (gender) {
            GENDER_MALE -> genderCircleRes = PersonUtils.GENDER_PEOPLE_CIRCLE_MALE_BG_RES
            GENDER_FEMALE -> genderCircleRes = PersonUtils.GENDER_PEOPLE_CIRCLE_FEMALE_BG_RES
            GENDER_MIXED -> genderCircleRes = PersonUtils.GENDER_PEOPLE_CIRCLE_MIXED_BG_RES
        }
    }

    companion object {
        const val EXTRA_NAME = "name"
        const val EXTRA_BIO = "bio"
        const val EXTRA_GENDER = "gender"

        const val GENDER_MALE: Byte = 1
        const val GENDER_FEMALE: Byte = 2
        const val GENDER_MIXED: Byte = 3
    }
}