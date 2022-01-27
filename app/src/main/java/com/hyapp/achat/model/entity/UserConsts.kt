package com.hyapp.achat.model.entity

import android.util.Pair
import com.hyapp.achat.R

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

        const val RANK_COLOR_MANAGER = -0x506e00
        const val RANK_COLOR_ADMIN = -0xe17800
        const val RANK_COLOR_SENIOR = -0xc40000
        const val RANK_COLOR_ACTIVE = -0xf6c400
        const val RANK_COLOR_SPECIAL = -0xa3e200
        const val RANK_COLOR_MEMBER = -0xfff2c2
        const val RANK_COLOR_GUEST = -0x616162

        fun rankInt2rankStrResAndColor(rank: Byte): Pair<Int, Int> {
            when (rank) {
                RANK_GUEST -> return Pair(R.string.guest, RANK_COLOR_GUEST)
                RANK_MEMBER -> return Pair(R.string.member, RANK_COLOR_MEMBER)
                RANK_SPECIAL -> return Pair(R.string.special, RANK_COLOR_SPECIAL)
                RANK_ACTIVE -> return Pair(R.string.active, RANK_COLOR_ACTIVE)
                RANK_SENIOR -> return Pair(R.string.senior, RANK_COLOR_SENIOR)
                RANK_ADMIN -> return Pair(R.string.admin, RANK_COLOR_ADMIN)
                RANK_MANAGER -> return Pair(R.string.manager, RANK_COLOR_MANAGER)
            }
            return Pair(R.string.guest, RANK_COLOR_GUEST)
        }
    }
}