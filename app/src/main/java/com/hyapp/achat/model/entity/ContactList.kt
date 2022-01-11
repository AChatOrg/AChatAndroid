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
        if (oldIndex != Resource.INDEX_NEW)
            removeAt(oldIndex)
        addFirst(contact)
        return oldIndex
    }
}