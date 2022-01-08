package com.hyapp.achat.view.component.sticker;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.hyapp.achat.view.component.emojiview.sticker.Sticker;
import com.hyapp.achat.view.component.emojiview.sticker.StickerCategory;


public class LottieStickerCategory implements StickerCategory<LottieSticker> {

    private LottieSticker[] stickers;

    public LottieStickerCategory(String name, int stickerCount) {
        stickers = new LottieSticker[stickerCount];
        for (int i = 0; i < stickerCount; i++) {
            stickers[i] = new LottieSticker(name + "/sticker" + (i + 1) + ".json", name + ".sticker" + (i + 1));
        }
    }

    @NonNull
    @Override
    public Sticker[] getStickers() {
        return stickers;
    }

    @Override
    public LottieSticker getCategoryData() {
        return new LottieSticker(stickers[0].getData(), stickers[0].name);
    }

    @Override
    public boolean useCustomView() {
        return false;
    }

    @Override
    public View getView(ViewGroup viewGroup) {
        return null;
    }

    @Override
    public void bindView(View view) {
    }

    @Override
    public View getEmptyView(ViewGroup viewGroup) {
        return null;
    }
}
