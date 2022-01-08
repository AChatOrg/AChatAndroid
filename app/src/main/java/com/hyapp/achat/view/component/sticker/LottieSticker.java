package com.hyapp.achat.view.component.sticker;

import com.aghajari.rlottie.AXrLottieDrawable;
import com.hyapp.achat.view.component.emojiview.sticker.Sticker;

public class LottieSticker extends Sticker<String> {

    public String name;
    public transient AXrLottieDrawable drawable;

    public LottieSticker(String data, String name) {
        super(data);
        this.name = name;
        drawable = null;
    }

}
