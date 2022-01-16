package com.hyapp.achat.model.objectbox

import com.hyapp.achat.model.entity.Message
import com.hyapp.achat.model.entity.Message_
import io.objectbox.query.QueryBuilder

object MessageDao {

    @JvmStatic
    fun put(message: Message): Long {
        return ObjectBox.store.boxFor(Message::class.java).put(message)
    }

    @JvmStatic
    fun all(contactUi: String, offset: Long, limit: Long): List<Message> {
        return ObjectBox.store.boxFor(Message::class.java).query(
                Message_.receiverUid.equal(contactUi, QueryBuilder.StringOrder.CASE_SENSITIVE)
                        .or(Message_.senderUid.equal(contactUi, QueryBuilder.StringOrder.CASE_SENSITIVE)))
                .build()
                .find(offset, limit)
    }

    @JvmStatic
    fun count(): Long {
        return ObjectBox.store.boxFor(Message::class.java).count()
    }
}