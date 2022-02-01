package com.hyapp.achat.model.objectbox

import com.hyapp.achat.model.entity.User
import com.hyapp.achat.model.entity.User_
import io.objectbox.query.QueryBuilder

object UserDao {

    @JvmStatic
    fun get(id: Long): User? {
        return ObjectBox.store.boxFor(User::class.java)[id]
    }

    @JvmStatic
    fun put(user: User): Long {
        return ObjectBox.store.boxFor(User::class.java).put(user)
    }

    fun removeALl(account: String) {
        ObjectBox.store.boxFor(User::class.java)
            .query()
            .equal(User_.uid, account, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build()
            .remove()
    }
}