package com.hyapp.achat.model.entity

import android.text.format.DateUtils
import java.util.*

class MessageList : LinkedList<Message>() {

    private var addedCount: Byte = 0
    private var prevChanged = false

    private fun setupBubbleAddingLast(message: Message): Boolean {
        var haveDateSeparatorPrev = false
        if (size <= 1) {
            message.bubble = Message.BUBBLE_SINGLE
            haveDateSeparatorPrev = true
        } else {
            val prev = get(size - 1)
            if (prev.isChatMessage
                    && prev.transfer == message.transfer && message.time - prev.time < 60000) {
                message.bubble = Message.BUBBLE_END
                if (size >= 3) {
                    val prevPrev = get(size - 2)
                    if (prevPrev.isChatMessage
                            && prevPrev.transfer == message.transfer && prev.time - prevPrev.time < 60000) {
                        prev.bubble = Message.BUBBLE_MIDDLE
                    } else {
                        prev.bubble = Message.BUBBLE_START
                    }
                } else {
                    prev.bubble = Message.BUBBLE_START
                }
                prevChanged = true
            } else {
                if (!DateUtils.isToday(prev.time)) {
                    haveDateSeparatorPrev = true
                }
                message.bubble = Message.BUBBLE_SINGLE
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
            if (prev.isChatMessage
                    && prev.transfer == message.transfer && prev.time - message.time < 60000) {
                message.bubble = Message.BUBBLE_START
                if (size >= 2) {
                    val prevPrev = get(1)
                    if (prevPrev.isChatMessage
                            && prevPrev.transfer == message.transfer && prevPrev.time - prev.time < 60000) {
                        prev.bubble = Message.BUBBLE_MIDDLE
                    } else {
                        prev.bubble = Message.BUBBLE_END
                    }
                } else {
                    prev.bubble = Message.BUBBLE_END
                }
                prevChanged = true
            } else {
                if (!DateUtils.isToday(prev.time)) {
                    haveDateSeparatorPrev = true
                }
                message.bubble = Message.BUBBLE_SINGLE
            }
        }
        return haveDateSeparatorPrev
    }

    fun addMessageLast(message: Message): Pair<Boolean, Byte> {
        var haveDateSeparatorPrev = false
        if (message.isChatMessage) {
            haveDateSeparatorPrev = setupBubbleAddingLast(message)
        }
        if (haveDateSeparatorPrev) {
            val details = DateUtils.getRelativeTimeSpanString(message.time, System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString()
            val detailsMessage = Message(type = Message.TYPE_DETAILS, time = message.time, text = details)
            addLast(detailsMessage)
            addedCount++
        }
        addLast(message)
        addedCount++
        val pair = Pair(prevChanged, addedCount)
        prevChanged = false
        addedCount = 0
        return pair
    }

    fun addMessageFirst(message: Message): Pair<Boolean, Byte> {
        var haveDateSeparatorPrev = false
        if (message.isChatMessage) {
            haveDateSeparatorPrev = setupBubbleAddingFirst(message)
        }
        addFirst(message)
        addedCount++
        if (haveDateSeparatorPrev) {
            val details = DateUtils.getRelativeTimeSpanString(message.time, System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString()
            val detailsMessage = Message(type = Message.TYPE_DETAILS, time = message.time, text = details)
            addFirst(detailsMessage)
            addedCount++
        }
        val pair = Pair(prevChanged, addedCount)
        prevChanged = false
        addedCount = 0
        return pair
    }
}