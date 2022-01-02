package com.hyapp.achat.ui.sticker;

import android.content.Context;

import com.aghajari.rlottie.AXrLottieDrawable;


public class Utils {

    public static AXrLottieDrawable createFromSticker(Context context, LottieSticker sticker, int size) {
        return AXrLottieDrawable.fromAssets(context, sticker.getData())
                .setCacheName(sticker.name)
                .setSize(size, size)
                .setCacheEnabled(true)
                .setFpsLimit(false)
                .setAutoRepeat(true)
                .build();
    }

}
