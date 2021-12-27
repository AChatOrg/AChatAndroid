package com.hyapp.achat.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Key(
        var uid: String = "",
        var rank: Byte = 0,
        var score: Int = 0,
        var loginTime: Long = 0
) : Parcelable