package com.hyapp.achat.model

import io.objectbox.annotation.BaseEntity
import io.objectbox.annotation.Transient

@BaseEntity
open class People : Person {

    @Transient
    var key: Key? = null
    var avatars: Array<String?>? = null

    constructor()

    constructor(name: String, bio: String?, gender: Byte, avatars: Array<String?>?) : super(name, bio, gender) {
        this.avatars = avatars
    }

    constructor(name: String, bio: String?, gender: Byte, key: Key?, avatars: Array<String?>?) : super(name, bio, gender) {
        this.key = key
        this.avatars = avatars
    }

    companion object {

        const val RANK_GUEST: Byte = 0
        const val RANK_MEMBER: Byte = 1
        const val RANK_SPECIAL: Byte = 2
        const val RANK_ACTIVE: Byte = 3
        const val RANK_SENIOR: Byte = 4
        const val RANK_ADMIN: Byte = 5
        const val RANK_MANAGER: Byte = 6

        @JvmStatic
        fun compare(o1: People, o2: People): Int {
            if (o1.key!!.uid == o2.key!!.uid) return 0
            if (o1.key!!.rank < o1.key!!.rank) return 1
            if (o1.key!!.rank > o1.key!!.rank) return -1
            if (o1.key!!.score < o1.key!!.score) return 1
            if (o1.key!!.score > o1.key!!.score) return -1
            if (o1.key!!.loginTime < o1.key!!.loginTime) return -1
            return if (o1.key!!.loginTime > o1.key!!.loginTime) return 1 else 0
        }
    }
}