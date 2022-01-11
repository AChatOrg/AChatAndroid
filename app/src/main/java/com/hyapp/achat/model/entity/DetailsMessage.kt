package com.hyapp.achat.model.entity

import android.text.format.DateUtils

class DetailsMessage : Message {

    var details: String = ""

    constructor(time: Long, details: String) : super(type = TYPE_DETAILS, time = time) {
        this.details = details
    }

    constructor(time: Long) : super(type = TYPE_DETAILS, time = time) {
        setDetails(time)
    }

    fun setDetails(timeMillis: Long) {
        details = DateUtils.getRelativeTimeSpanString(
                timeMillis, System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    }
}