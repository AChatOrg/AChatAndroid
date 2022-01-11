package com.hyapp.achat.model.entity

import android.os.Bundle
import io.objectbox.annotation.BaseEntity
import io.objectbox.annotation.Transient

@BaseEntity
open class People : Person {

    companion object {
        const val RANK_GUEST: Byte = 0
        const val RANK_MEMBER: Byte = 1
        const val RANK_SPECIAL: Byte = 2
        const val RANK_ACTIVE: Byte = 3
        const val RANK_SENIOR: Byte = 4
        const val RANK_ADMIN: Byte = 5
        const val RANK_MANAGER: Byte = 6

        const val EXTRA_KEY = "key"
        const val EXTRA_AVATARS = "avatars"

        @JvmStatic
        fun compare(o1: People, o2: People): Int {
            if (o1.key!!.uid == o2.key!!.uid) return 0
            if (o1.key!!.rank < o2.key!!.rank) return 1
            if (o1.key!!.rank > o2.key!!.rank) return -1
            if (o1.key!!.score < o2.key!!.score) return 1
            if (o1.key!!.score > o2.key!!.score) return -1
            if (o1.key!!.loginTime < o2.key!!.loginTime) return -1
            return if (o1.key!!.loginTime > o2.key!!.loginTime) return 1 else 0
        }
    }

    @Transient
    var key: Key? = null

    var avatars: Array<String?> = emptyArray()

    constructor(name: String = "", bio: String? = null, gender: Byte = GENDER_MALE,
                key: Key? = null, avatars: Array<String?> = emptyArray()
    ) : super(name, bio, gender) {
        this.key = key
        this.avatars = avatars
    }

    constructor(bundle: Bundle) : super(bundle) {
        key = bundle.getParcelable(EXTRA_KEY) ?: Key()
        avatars = bundle.getStringArray(EXTRA_AVATARS) ?: emptyArray()
    }

    override val bundle: Bundle
        get() {
            return super.bundle.apply {
                putParcelable(EXTRA_KEY, key)
                putStringArray(EXTRA_AVATARS, avatars)
            }
        }

    override fun same(person: Person): Boolean {
        return person is People
                && ((avatars.isEmpty() && person.avatars.isEmpty()) || (avatars.isNotEmpty() && person.avatars.isNotEmpty() && avatars[0] == person.avatars[0]))
                && key?.rank == person.key?.rank
                && super.same(person)
    }
}