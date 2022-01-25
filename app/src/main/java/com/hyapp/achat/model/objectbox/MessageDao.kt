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
    fun get(uid: String): Message? {
        return getQuery.setParameter(Message_.uid, uid).findUnique()
    }

    @JvmStatic
    fun put(message: Message): Long {
        return ObjectBox.store.boxFor(Message::class.java).put(message)
    }

    @JvmStatic
    fun put(messageList: List<Message>) {
        ObjectBox.store.boxFor(Message::class.java).put(messageList)
    }

    @JvmStatic
    fun all(currUserUid: String, contactUi: String, offset: Long, limit: Long): List<Message> {
        return ObjectBox.store.boxFor(Message::class.java).query(
            Message_.receiverUid.equal(contactUi, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .or(Message_.receiverUid.equal(currUserUid, QueryBuilder.StringOrder.CASE_SENSITIVE))
        )
            .build()
            .find(offset, limit)
    }

    fun allRoom(roomUid: String, offset: Long, limit: Long): List<Message> {
        return ObjectBox.store.boxFor(Message::class.java).query()
            .equal(Message_.receiverUid, roomUid, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build()
            .find(offset, limit)
    }

    @JvmStatic
    fun allReceivedUnReads(contactUi: String): List<Message> {
        val isContactSender =
            Message_.senderUid.equal(contactUi, QueryBuilder.StringOrder.CASE_SENSITIVE)
        val isUnread = (Message_.delivery.notEqual(Message.DELIVERY_SENT.toInt())
            .and(Message_.delivery.notEqual(Message.DELIVERY_READ.toInt())))
        return ObjectBox.store.boxFor(Message::class.java)
            .query(isContactSender.and(isUnread))
            .build()
            .find()
    }

    @JvmStatic
    fun allReceivedUnReadsRoom(currUserUid: String, roomUid: String): List<Message> {
        val isFromRoom =
            (Message_.receiverUid.equal(roomUid, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .and(
                    Message_.senderUid.notEqual(
                        currUserUid,
                        QueryBuilder.StringOrder.CASE_SENSITIVE
                    )
                ))
        val isUnread = (Message_.delivery.notEqual(Message.DELIVERY_SENT.toInt())
            .and(Message_.delivery.notEqual(Message.DELIVERY_READ.toInt())))
        return ObjectBox.store.boxFor(Message::class.java)
            .query(isFromRoom.and(isUnread))
            .build()
            .find()
    }

    @JvmStatic
    fun markAllSentAsReadUntil(lastMessage: Message) {
        val box = ObjectBox.store.boxFor(Message::class.java)
        val list = box
            .query(
                Message_.receiverUid.equal(
                    lastMessage.receiverUid,
                    QueryBuilder.StringOrder.CASE_SENSITIVE
                ).and(
                    Message_.time.lessOrEqual(lastMessage.time)
                        .and(
                            Message_.delivery.notEqual(Message.DELIVERY_READ.toInt())
                        )
                )
            ).build()
            .find()
        for (message in list) {
            message.delivery = Message.DELIVERY_READ
        }
        box.put(list)
    }

    @JvmStatic
    fun markAllReceivedAsReadUntil(lastMessage: Message) {
        val box = ObjectBox.store.boxFor(Message::class.java)
        val list = box
            .query(
                Message_.senderUid.equal(
                    lastMessage.senderUid,
                    QueryBuilder.StringOrder.CASE_SENSITIVE
                ).and(
                    Message_.time.lessOrEqual(lastMessage.time)
                        .and(
                            Message_.delivery.notEqual(Message.DELIVERY_READ.toInt())
                        )
                )
            ).build()
            .find()
        for (message in list) {
            message.delivery = Message.DELIVERY_READ
        }
        box.put(list)
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

    @JvmStatic
    fun allSentUnReads(receiverUi: String): List<Message> {
        return ObjectBox.store.boxFor(Message::class.java).query(
            Message_.receiverUid.equal(receiverUi, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .and(Message_.delivery.equal(Message.DELIVERY_SENT.toInt()))
        )
            .build()
            .find()
    }

    fun removeALl() {
        ObjectBox.store.boxFor(Message::class.java).removeAll()
    }
}