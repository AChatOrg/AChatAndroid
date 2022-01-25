package com.hyapp.achat.model.entity

open class Event(
    var status: Status = Status.SUCCESS,
    var msg: String = MSG_ERROR,
    var action: Byte = -1
) {

    enum class Status {
        SUCCESS, ERROR, LOADING
    }

    companion object {
        const val MSG_EMPTY = "empty"
        const val MSG_EXIST = "exist"
        const val MSG_NET = "net"
        const val MSG_ERROR = "error"
        const val MSG_GENDER = "gender"

    }
}