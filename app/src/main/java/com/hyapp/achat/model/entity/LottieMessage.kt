package com.hyapp.achat.model.entity

import com.aghajari.rlottie.AXrLottieDrawable

class LottieMessage(
        uid: String = "",
        transfer: Byte = TRANSFER_RECEIVE,
        time: Long = 0,
        sender: Contact = Contact(),
        receiverUid: String = "",

        val drawable: AXrLottieDrawable? = null

) : ChatMessage(uid, TYPE_LOTTIE, transfer, time, sender, receiverUid)