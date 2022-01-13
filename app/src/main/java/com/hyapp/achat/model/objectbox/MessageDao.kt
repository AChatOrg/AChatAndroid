package com.hyapp.achat.model.objectbox

import com.hyapp.achat.model.entity.Contact_
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
        val box = ObjectBox.store.boxFor(Message::class.java)
        val builder = box.query().link(Message_.sender).equal(Contact_.uid, contactUi, QueryBuilder.StringOrder.CASE_SENSITIVE)
        val builder2 = box.query().equal(Message_.receiverUid, contactUi, QueryBuilder.StringOrder.CASE_SENSITIVE)

        return builder2.or()
                .link(Message_.sender).equal(Contact_.uid, contactUi, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .build().find(offset, limit)
    }
}