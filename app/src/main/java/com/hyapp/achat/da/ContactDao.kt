package com.hyapp.achat.da

import com.hyapp.achat.model.Contact

object ContactDao {

    @JvmStatic
    fun put(contact: Contact): Long {
        return ObjectBox.store.boxFor(Contact::class.java).put(contact)
    }

    @JvmStatic
    val all: List<Contact>
        get() = ObjectBox.store.boxFor(Contact::class.java).all

    @JvmStatic
    fun remove(contact: Contact): Boolean {
        return ObjectBox.store.boxFor(Contact::class.java).remove(contact.id)
    }
}