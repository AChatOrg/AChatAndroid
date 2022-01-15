package com.hyapp.achat.model.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
        var name: String = "",
        var bio: String = "",
        var gender: Byte = GENDER_MALE,
        var avatars: MutableList<String> = mutableListOf(),
        var onlineTime: Long = TIME_ONLINE,

        var uid: String = "",
        var rank: Byte = RANK_GUEST,
        var score: Int = 0,
        var loginTime: Long = 0

) : UserConsts(), Parcelable {

    companion object {
        @JvmStatic
        fun compare(u1: User, u2: User): Int {
            if (u1.uid == u2.uid) return 0
            if (u1.rank < u2.rank) return 1
            if (u1.rank > u2.rank) return -1
            if (u1.score < u2.score) return 1
            if (u1.score > u2.score) return -1
            if (u1.loginTime < u2.loginTime) return -1
            return if (u1.loginTime > u2.loginTime) return 1 else 0
        }
    }
}
