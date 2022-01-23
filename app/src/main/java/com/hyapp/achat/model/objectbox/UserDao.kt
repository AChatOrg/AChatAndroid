package com.hyapp.achat.model.objectbox

import com.hyapp.achat.model.entity.User

object UserDao {

    @JvmStatic
    fun get(id: Long): User? {
        return ObjectBox.store.boxFor(User::class.java)[id]
    }

    @JvmStatic
    fun put(user: User): Long {
        return ObjectBox.store.boxFor(User::class.java).put(user)
    }

    fun removeALl() {
        ObjectBox.store.boxFor(User::class.java).removeAll()
    }
}