package com.hyapp.achat.model.entity

import java.util.Comparator

class PeopleList : SortedList<People>(Comparator { p1, p2 -> People.compare(p1, p2) }) {

    companion object {
        const val INDEX_NOT_FOUND = -1
    }

    fun remove(uid: String): Int {
        var i = 0
        val iterator = iterator()
        while (iterator.hasNext()) {
            if (iterator.next().key?.uid == uid) {
                iterator.remove()
                return i
            }
            i++
        }
        return INDEX_NOT_FOUND
    }
}