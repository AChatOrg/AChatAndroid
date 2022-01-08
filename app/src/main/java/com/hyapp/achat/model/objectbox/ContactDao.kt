package com.hyapp.achat.model.objectbox

import com.hyapp.achat.model.entity.Contact
import com.hyapp.achat.model.entity.Contact_
import io.objectbox.query.Query
import io.objectbox.query.QueryBuilder

object ContactDao {

    private val getQuery: Query<Contact> by lazy {
        ObjectBox.store.boxFor(Contact::class.java)
                .query()
                .equal(Contact_.uid, "", QueryBuilder.StringOrder.CASE_SENSITIVE)
                .build()
    }

    @JvmStatic
    fun get(uid: String): Contact? {
        return getQuery.setParameter(Contact_.uid, uid).findUnique()
    }

    @JvmStatic
    fun put(contact: Contact): Long {
        return ObjectBox.store.boxFor(Contact::class.java).put(contact)
    }

    @JvmStatic
    val all: List<Contact>
        get() = ObjectBox.store.boxFor(Contact::class.java).query()
                .order(Contact_.messageTime, QueryBuilder.DESCENDING)
                .build().find()

    @JvmStatic
    fun remove(contact: Contact): Boolean {
        return ObjectBox.store.boxFor(Contact::class.java).remove(contact.id)
    }
}