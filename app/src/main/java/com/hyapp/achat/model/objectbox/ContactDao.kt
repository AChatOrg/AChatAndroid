package com.hyapp.achat.model.objectbox

import com.hyapp.achat.model.entity.Contact
import com.hyapp.achat.model.entity.Contact_
import io.objectbox.query.Query
import io.objectbox.query.QueryBuilder

object ContactDao {

    private val getQuery: Query<Contact> by lazy {
        ObjectBox.store.boxFor(Contact::class.java)
            .query(
                Contact_.account.equal("", QueryBuilder.StringOrder.CASE_SENSITIVE)
                    .and(Contact_.uid.equal("", QueryBuilder.StringOrder.CASE_SENSITIVE))
            )
            .build()
    }

    @JvmStatic
    fun get(account: String, uid: String): Contact? {
        return getQuery
            .setParameter(Contact_.account, account)
            .setParameter(Contact_.uid, uid)
            .findUnique()
    }

    @JvmStatic
    fun put(contact: Contact): Long {
        return ObjectBox.store.boxFor(Contact::class.java).put(contact)
    }

    @JvmStatic
    fun all(account: String): List<Contact> {
        return ObjectBox.store.boxFor(Contact::class.java).query()
            .equal(Contact_.account, account, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .order(Contact_.messageTime, QueryBuilder.DESCENDING)
            .build().find()
    }

    @JvmStatic
    fun remove(contact: Contact): Boolean {
        return ObjectBox.store.boxFor(Contact::class.java).remove(contact.id)
    }


    fun removeALl(account: String) {
        ObjectBox.store.boxFor(Contact::class.java).query()
            .equal(Contact_.account, account, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build()
            .remove()
    }
}