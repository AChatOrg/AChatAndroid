package com.hyapp.achat.model;

import com.aghajari.rlottie.AXrLottieDrawable;

public class LottieMessage extends ChatMessage {

    private AXrLottieDrawable drawable;

    public LottieMessage(byte transferType, long timeMillis, String uid, Contact sender, String receiverId, AXrLottieDrawable drawable) {
        super(TYPE_LOTTIE, transferType, timeMillis, uid, sender, receiverId);
        this.drawable = drawable;
    }

    public AXrLottieDrawable getDrawable() {
        return drawable;
    }

    public void setDrawable(AXrLottieDrawable drawable) {
        this.drawable = drawable;
    }
}
