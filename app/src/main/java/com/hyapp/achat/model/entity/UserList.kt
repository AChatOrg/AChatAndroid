package com.hyapp.achat.model.entity

import java.util.Comparator

class UserList : SortedList<User>(Comparator { u1, u2 -> User.compare(u1, u2) }) {

    companion object {
        const val INDEX_NOT_FOUND = -1
    }

    fun remove(uid: String): Int {
        var i = 0
        val iterator = iterator()
        while (iterator.hasNext()) {
            if (iterator.next().uid == uid) {
                iterator.remove()
                return i
            }
            i++
        }
        return INDEX_NOT_FOUND
    }
}