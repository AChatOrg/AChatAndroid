package com.hyapp.achat.model.entity

import java.util.LinkedList

class ContactList : LinkedList<Contact> {

    constructor() : super()

    constructor(collection: Collection<Contact>) : super(collection)

    fun putFirst(contact: Contact): Int {
        var oldIndex = Resource.INDEX_NEW
        for (i in 0 until size) {
            if (get(i).uid == contact.uid) {
                oldIndex = i
                break
            }
        }
        when {
            oldIndex == Resource.INDEX_NEW -> {
                addFirst(contact)
            }
            oldIndex != 0 -> {
                removeAt(oldIndex)
                addFirst(contact)
            }
            else -> {
                set(0, contact)
            }
        }
        return oldIndex
    }

    fun update(contact: Contact): Boolean {
        for (i in 0 until size) {
            if (get(i).uid == contact.uid) {
                set(i, contact)
                return true
            }
        }
        return false
    }

    fun refreshOnlineTimes(): Boolean {
        var updated = false
        for (i in 0 until size) {
            val contact = get(i)
            if (contact.onlineTime != Contact.TIME_ONLINE) {
                set(i, contact.copy(onlineTime = contact.onlineTime + 1))
                updated = true
            }
        }
        return updated
    }
}