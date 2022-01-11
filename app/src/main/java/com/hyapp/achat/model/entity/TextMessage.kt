package com.hyapp.achat.model.entity

import android.util.Pair

class TextMessage(
        uid: String = "",
        transfer: Byte = TRANSFER_RECEIVE,
        time: Long = 0,
        sender: Contact = Contact(),
        receiverUid: String = "",

        var text: String = "",
        var extraTextSize: Int = 0

) : ChatMessage(uid, TYPE_TEXT, transfer, time, sender, receiverUid) {

    companion object {
        const val TEXT_SIZE_SP = 14
        const val EMOJI_SIZE_LARGEST_SP = 36
    }

    override fun same(message: Message): Boolean {
        return message is TextMessage
                && super.same(message)
                && text == message.text
                && extraTextSize == message.extraTextSize

    }

    fun setAndGetTextSizes(sp1: Int): Pair<Float, Int> {
        var textSize: Float = ((TEXT_SIZE_SP + extraTextSize) * sp1).toFloat()
        var emojiSize: Float = ((TEXT_SIZE_SP + 3 + extraTextSize) * sp1).toFloat()
        var hasText = false
        for (element in text) {
            if (element.code < 2000) {
                hasText = true
                break
            }
        }
        if (!hasText) {
            emojiSize = ((EMOJI_SIZE_LARGEST_SP + extraTextSize) * sp1).toFloat()
            for (i in 1 until text.length) {
                if (emojiSize <= (TEXT_SIZE_SP + 3 + extraTextSize) * sp1) {
                    emojiSize = ((TEXT_SIZE_SP + 3 + extraTextSize) * sp1).toFloat()
                    break
                }
                emojiSize -= sp1.toFloat()
            }
            textSize = emojiSize * 0.8f
        }
        return Pair(textSize, emojiSize.toInt())
    }
}