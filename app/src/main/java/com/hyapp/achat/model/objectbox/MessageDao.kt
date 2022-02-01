package com.hyapp.achat.model.objectbox

import com.hyapp.achat.model.entity.Contact_
import com.hyapp.achat.model.entity.Message
import com.hyapp.achat.model.entity.Message_
import io.objectbox.query.Query
import io.objectbox.query.QueryBuilder

object MessageDao {

    private val getQuery: Query<Message> by lazy {
        ObjectBox.store.boxFor(Message::class.java)
            .query(
                Message_.account.equal("", QueryBuilder.StringOrder.CASE_SENSITIVE)
                    .and(Message_.uid.equal("", QueryBuilder.StringOrder.CASE_SENSITIVE))
            )
            .build()
    }

    @JvmStatic
    fun get(account: String, uid: String): Message? {
        return getQuery
            .setParameter(Message_.account, account)
            .setParameter(Message_.uid, uid)
            .findUnique()
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
    fun all(
        account: String,
        currUserUid: String,
        contactUi: String,
        offset: Long,
        limit: Long
    ): List<Message> {
        return ObjectBox.store.boxFor(Message::class.java).query(
            Message_.account.equal(account, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .and(
                    Message_.receiverUid.equal(contactUi, QueryBuilder.StringOrder.CASE_SENSITIVE)
                        .or(
                            (Message_.receiverUid.equal(
                                currUserUid,
                                QueryBuilder.StringOrder.CASE_SENSITIVE
                            )
                                .and(
                                    Message_.senderUid.equal(
                                        contactUi,
                                        QueryBuilder.StringOrder.CASE_SENSITIVE
                                    )
                                ))
                        )
                )
        )
            .build()
            .find(offset, limit)
    }

    fun allRoom(account: String, roomUid: String, offset: Long, limit: Long): List<Message> {
        return ObjectBox.store.boxFor(Message::class.java).query(
            Message_.account.equal(account, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .and(Message_.receiverUid.equal(roomUid, QueryBuilder.StringOrder.CASE_SENSITIVE))
        )
            .build()
            .find(offset, limit)
    }

    @JvmStatic
    fun allReceivedUnReads(account: String, contactUi: String): List<Message> {
        val isAccount = Message_.account.equal(account, QueryBuilder.StringOrder.CASE_SENSITIVE)
        val isContactSender =
            Message_.senderUid.equal(contactUi, QueryBuilder.StringOrder.CASE_SENSITIVE)
        val isUnread = (Message_.delivery.notEqual(Message.DELIVERY_SENT.toInt())
            .and(Message_.delivery.notEqual(Message.DELIVERY_READ.toInt())))
        return ObjectBox.store.boxFor(Message::class.java)
            .query(isAccount.and(isContactSender.and(isUnread)))
            .build()
            .find()
    }

    @JvmStatic
    fun allReceivedUnReadsRoom(
        account: String,
        currUserUid: String,
        roomUid: String
    ): List<Message> {
        val isAccount = Message_.account.equal(account, QueryBuilder.StringOrder.CASE_SENSITIVE)
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
            .query(isAccount.and(isFromRoom.and(isUnread)))
            .build()
            .find()
    }

    @JvmStatic
    fun markAllSentAsReadUntil(account: String, lastMessage: Message) {
        val box = ObjectBox.store.boxFor(Message::class.java)
        val list = box
            .query(
                Message_.account.equal(account, QueryBuilder.StringOrder.CASE_SENSITIVE)
                    .and(
                        Message_.receiverUid.equal(
                            lastMessage.receiverUid,
                            QueryBuilder.StringOrder.CASE_SENSITIVE
                        ).and(
                            Message_.time.lessOrEqual(lastMessage.time)
                                .and(
                                    Message_.delivery.notEqual(Message.DELIVERY_READ.toInt())
                                )
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
    fun markAllReceivedAsReadUntil(account: String, lastMessage: Message) {
        val box = ObjectBox.store.boxFor(Message::class.java)
        val list = box
            .query(
                Message_.account.equal(account, QueryBuilder.StringOrder.CASE_SENSITIVE)
                    .and(
                        Message_.senderUid.equal(
                            lastMessage.senderUid,
                            QueryBuilder.StringOrder.CASE_SENSITIVE
                        ).and(
                            Message_.time.lessOrEqual(lastMessage.time)
                                .and(
                                    Message_.delivery.notEqual(Message.DELIVERY_READ.toInt())
                                )
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
    fun waitings(account: String, currUserUid: String): List<Message> {
        return ObjectBox.store.boxFor(Message::class.java).query(
            Message_.account.equal(account, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .and(
                    Message_.senderUid.equal(currUserUid, QueryBuilder.StringOrder.CASE_SENSITIVE)
                        .and(Message_.delivery.equal(Message.DELIVERY_WAITING.toInt()))
                )
        )
            .build()
            .find()
    }

    @JvmStatic
    fun allSentUnReads(account: String, currUserUid: String): List<Message> {
        return ObjectBox.store.boxFor(Message::class.java).query(
            Message_.account.equal(account, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .and(
                    Message_.receiverUid.equal(currUserUid, QueryBuilder.StringOrder.CASE_SENSITIVE)
                        .and(Message_.delivery.equal(Message.DELIVERY_SENT.toInt()))
                )
        )
            .build()
            .find()
    }

    fun removeALl(account: String) {
        ObjectBox.store.boxFor(Message::class.java).query()
            .equal(Message_.account, account, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build()
            .remove()
    }
}