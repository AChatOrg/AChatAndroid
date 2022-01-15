package com.hyapp.achat.model.entity

open class UserConsts {
    companion object {
        const val GENDER_MALE: Byte = 1
        const val GENDER_FEMALE: Byte = 2
        const val GENDER_MIXED: Byte = 3

        const val TIME_ONLINE: Long = 0

        const val RANK_GUEST: Byte = 0
        const val RANK_MEMBER: Byte = 1
        const val RANK_SPECIAL: Byte = 2
        const val RANK_ACTIVE: Byte = 3
        const val RANK_SENIOR: Byte = 4
        const val RANK_ADMIN: Byte = 5
        const val RANK_MANAGER: Byte = 6
    }
}