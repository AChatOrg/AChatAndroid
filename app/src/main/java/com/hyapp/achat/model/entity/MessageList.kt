package com.hyapp.achat.model.entity

import android.text.format.DateUtils
import com.hyapp.achat.R
import com.hyapp.achat.viewmodel.utils.TimeUtils
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*

class MessageList : LinkedList<Message>() {

    private fun setupBubbleAddingLast(message: Message): Boolean {
        var haveDateSeparatorPrev = false
        if (size <= 1) {
            message.bubble = Message.BUBBLE_SINGLE
            haveDateSeparatorPrev = true
        } else {
            val prev = get(size - 1)
            if (prev.isChatMessage && prev.transfer == message.transfer && message.time - prev.time < 60000) {
                message.bubble = Message.BUBBLE_END
                if (size >= 3) {
                    val prevPrev = get(size - 2)
                    if (prevPrev.isChatMessage && prevPrev.transfer == message.transfer && prev.time - prevPrev.time < 60000) {
                        prev.bubble = Message.BUBBLE_MIDDLE
                    } else {
                        prev.bubble = Message.BUBBLE_START
                    }
                } else {
                    prev.bubble = Message.BUBBLE_START
                }
                set(size - 1, prev.copy(id = 0))
                //prevChanged
            } else {
                message.bubble = Message.BUBBLE_SINGLE
            }
            if (!DateUtils.isToday(prev.time)) {
                haveDateSeparatorPrev = true
                prev.bubble = Message.BUBBLE_END
                set(size - 1, prev.copy(id = 0))
                //prevChanged
            }
        }
        return haveDateSeparatorPrev
    }

    private fun setupBubbleAddingFirst(message: Message): Boolean {
        var haveDateSeparatorPrev = false
        if (size == 0) {
            message.bubble = Message.BUBBLE_SINGLE
        } else {
            val prev = get(0)
            if (prev.isChatMessage && prev.transfer == message.transfer && prev.time - message.time < 60000) {
                message.bubble = Message.BUBBLE_START
                if (size >= 2) {
                    val prevPrev = get(1)
                    if (prevPrev.isChatMessage && prevPrev.transfer == message.transfer && prevPrev.time - prev.time < 60000) {
                        prev.bubble = Message.BUBBLE_MIDDLE
                    } else {
                        prev.bubble = Message.BUBBLE_END
                    }
                } else {
                    prev.bubble = Message.BUBBLE_END
                }
                // prevChanged = true
            } else {
                message.bubble = Message.BUBBLE_SINGLE
            }
            //if two time is not for one day
            if (!TimeUtils.isSameDay(prev.time, message.time)) {
                haveDateSeparatorPrev = true
                prev.bubble = Message.BUBBLE_START
                //prevChanged = true
            }
        }
        return haveDateSeparatorPrev
    }

    fun addMessageLast(message: Message) {
        var haveDateSeparatorPrev = false
        if (message.isChatMessage) {
            haveDateSeparatorPrev = setupBubbleAddingLast(message)
        }
        if (haveDateSeparatorPrev) {
            val details = DateUtils.getRelativeTimeSpanString(
                message.time,
                System.currentTimeMillis(),
                DateUtils.DAY_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
            ).toString()
            val detailsMessage = Message(
                uid = UUID.randomUUID().toString(),
                type = Message.TYPE_DETAILS,
                time = message.time,
                text = details
            )
            addLast(detailsMessage)
        }
        addLast(message)
    }

    fun addMessageFirst(message: Message) {
        var haveDateSeparatorPrev = false
        if (message.isChatMessage) {
            haveDateSeparatorPrev = setupBubbleAddingFirst(message)
        }
        if (haveDateSeparatorPrev) {
            val details = DateUtils.getRelativeTimeSpanString(
                message.time,
                System.currentTimeMillis(),
                DateUtils.DAY_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
            ).toString()
            val detailsMessage = Message(
                uid = UUID.randomUUID().toString(),
                type = Message.TYPE_DETAILS,
                time = message.time,
                text = details
            )
            addFirst(detailsMessage)
        }
        addFirst(message)
    }

    fun updateMessageTimeAndDelivery(message: Message): Boolean {
        for (i in size - 1 downTo 0) {
            val msg = get(i)
            if (msg.uid == message.uid) {
                set(i, msg.copy(time = message.time, delivery = message.delivery))
                return true
            }
        }
        return false
    }

//    fun updateMessageDelivery(message: Message): Boolean {
//        for (i in size - 1 downTo 0) {
//            if (get(i).uid == message.uid) {
//                for (j in i downTo 0) {
//                    val m = get(j)
//                    if (m.isChatMessage && m.transfer == Message.TRANSFER_SEND && m.delivery != Message.DELIVERY_READ) {
//                        set(j, m.copy(delivery = message.delivery))
//                    } else break
//                }
//                return true
//            }
//        }
//        return false
//    }

    fun updateMessageDelivery(message: Message): Boolean {
        for (i in size - 1 downTo 0) {
            val m = get(i)
            if (m.uid == message.uid) {
                if (m.isChatMessage && m.transfer == Message.TRANSFER_SEND && m.delivery != Message.DELIVERY_READ) {
                    set(i, m.copy(delivery = message.delivery))
                }
                return true
            }
        }
        return false
    }
}