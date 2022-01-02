package com.hyapp.achat.model

import android.os.Bundle
import com.google.gson.annotations.Expose
import com.hyapp.achat.model.utils.PersonUtils
import io.objectbox.annotation.BaseEntity
import io.objectbox.annotation.Transient

@BaseEntity
open class People : Person {

    @Transient
    var key: Key? = null
        set(value) {
            field = value
            setupRank(field)
        }
    @Expose
    var avatars: Array<String?> = emptyArray()

    @Transient
    var rankStrRes: Int = PersonUtils.RANK_STR_GUEST

    @Transient
    var rankColor: Int = PersonUtils.RANK_COLOR_GUEST

    constructor(name: String = "",
                bio: String? = null,
                gender: Byte = GENDER_MALE,
                key: Key? = null,
                avatars: Array<String?> = emptyArray()
    ) : super(name, bio, gender) {
        this.key = key
        this.avatars = avatars
    }

    init {
        setupRank(key)
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

    fun setupRank(key: Key?) {
        key?.let {
            setupRank(it.rank)
        }
    }

    fun setupRank(rank: Byte) {
        val pair = PersonUtils.rankInt2rankStrResAndColor(rank)
        rankStrRes = pair.first
        rankColor = pair.second
    }

    companion object {
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
}