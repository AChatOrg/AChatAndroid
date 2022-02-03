package com.hyapp.achat.model.entity

import android.os.Parcelable
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.Unique
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class User(
    var name: String = "",
    var bio: String = "",
    var gender: Byte = GENDER_MALE,
    var avatars: List<String> = mutableListOf(),
    var onlineTime: Long = TIME_ONLINE,

    @Index
    @Unique
    var uid: String = "",
    var rank: Byte = RANK_GUEST,
    var score: Int = 0,
    var loginTime: Long = 0,

    @Id(assignable = true)
    var id: Long = 0,

    var username: String = "unknown"

) : UserConsts(), Parcelable {

    companion object {

        const val CURRENT_USER_ID: Long = 1

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

    val isGuest
        get() = rank == RANK_GUEST

    val isMale
        get() = gender == GENDER_MALE

    val firstAvatar
        get() = if (avatars.isNotEmpty()) avatars[0] else ""
}
