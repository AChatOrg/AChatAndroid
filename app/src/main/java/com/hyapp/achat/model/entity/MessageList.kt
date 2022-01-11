package com.hyapp.achat.model.entity

import android.text.format.DateUtils
import java.util.*

class MessageList : LinkedList<Message>() {
    private fun setupBubble(message: ChatMessage): Boolean {
        var haveDateSeparatorPrev = false
        if (size == 1) {
            message.bubble = ChatMessage.BUBBLE_SINGLE
            haveDateSeparatorPrev = true
        } else {
            val prev = get(size - 1)
            if (prev is ChatMessage
                    && prev.transfer == Message.TRANSFER_SEND && message.time - prev.time < 60000) {
                message.bubble = ChatMessage.BUBBLE_END
                if (size >= 3) {
                    val prevPrev = get(size - 2)
                    if (prevPrev is ChatMessage
                            && prevPrev.transfer == Message.TRANSFER_SEND && prev.time - prevPrev.time < 60000) {
                        prev.bubble = ChatMessage.BUBBLE_MIDDLE
                    } else {
                        prev.bubble = ChatMessage.BUBBLE_START
                    }
                } else {
                    prev.bubble = ChatMessage.BUBBLE_START
                }
            } else {
                if (!DateUtils.isToday(prev.time)) {
                    haveDateSeparatorPrev = true
                }
                message.bubble = ChatMessage.BUBBLE_SINGLE
            }
        }
        return haveDateSeparatorPrev
    }

    override fun add(element: Message): Boolean {
        var haveDateSeparatorPrev = false
        if (element is ChatMessage) {
            haveDateSeparatorPrev = setupBubble(element)
        }
        if (haveDateSeparatorPrev) {
            val detailsMessage: Message = DetailsMessage(element.time)
            addLast(detailsMessage)
        }
        addLast(element)
        return true
    }
}