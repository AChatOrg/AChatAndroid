package com.hyapp.achat.model.objectbox

import com.hyapp.achat.model.entity.Message
import com.hyapp.achat.model.entity.Message_
import io.objectbox.query.Query
import io.objectbox.query.QueryBuilder

object MessageDao {

    private val getQuery: Query<Message> by lazy {
        ObjectBox.store.boxFor(Message::class.java)
            .query()
            .equal(Message_.uid, "", QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build()
    }

    @JvmStatic
    fun put(message: Message): Long {
        return ObjectBox.store.boxFor(Message::class.java).put(message)
    }

    @JvmStatic
    fun all(contactUi: String, offset: Long, limit: Long): List<Message> {
        return ObjectBox.store.boxFor(Message::class.java).query(
            Message_.receiverUid.equal(contactUi, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .or(Message_.senderUid.equal(contactUi, QueryBuilder.StringOrder.CASE_SENSITIVE))
        )
            .build()
            .find(offset, limit)
    }

    @JvmStatic
    fun update(message: Message) {
        val msg = getQuery.setParameter(Message_.uid, message.uid).findUnique()
        if (msg != null) {
            ObjectBox.store.boxFor(Message::class.java).put(message.apply { id = msg.id })
        }
    }

    @JvmStatic
    fun waitings(senderUi: String): List<Message> {
        return ObjectBox.store.boxFor(Message::class.java).query(
            Message_.senderUid.equal(senderUi, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .and(Message_.delivery.equal(Message.DELIVERY_WAITING.toInt()))
        )
            .build()
            .find()
    }
}