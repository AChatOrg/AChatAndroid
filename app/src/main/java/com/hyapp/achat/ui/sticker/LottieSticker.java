package com.hyapp.achat.ui.sticker;

import com.aghajari.rlottie.AXrLottieDrawable;
import com.hyapp.achat.ui.emojiview.sticker.Sticker;

public class LottieSticker extends Sticker<String> {

    public String name;
    public transient AXrLottieDrawable drawable;

    public LottieSticker(String data, String name) {
        super(data);
        this.name = name;
        drawable = null;
    }

}
