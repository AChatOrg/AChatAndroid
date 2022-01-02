package com.hyapp.achat.ui.sticker;

import android.view.View;

import androidx.annotation.NonNull;

import com.aghajari.rlottie.AXrLottieImageView;
import com.hyapp.achat.ui.emojiview.sticker.Sticker;
import com.hyapp.achat.ui.emojiview.sticker.StickerCategory;
import com.hyapp.achat.ui.emojiview.sticker.StickerLoader;
import com.hyapp.achat.ui.emojiview.sticker.StickerProvider;

public class LottieStickerProvider implements StickerProvider {

    StickerCategory[] categories;

    public LottieStickerProvider() {
        categories = new StickerCategory[]{
                new LottieStickerCategory("HotCherry", 23),
                new LottieStickerCategory("KangarooFighter", 26),
                new LottieStickerCategory("ValentineCat", 15),
        };
    }

    @NonNull
    @Override
    public StickerCategory[] getCategories() {
        return categories;
    }

    @NonNull
    @Override
    public StickerLoader getLoader() {
        return new StickerLoader() {
            @Override
            public void onLoadSticker(View view, Sticker sticker) {
                if (view instanceof AXrLottieImageView && sticker instanceof LottieSticker) {
                    AXrLottieImageView lottieImageView = (AXrLottieImageView) view;
                    LottieSticker lottieSticker = (LottieSticker) sticker;
                    if (lottieSticker.drawable == null) {
                        lottieSticker.drawable = Utils.createFromSticker(view.getContext(), lottieSticker, 100);
                    }
                    lottieImageView.setLottieDrawable(lottieSticker.drawable);
                    lottieImageView.playAnimation();
                }
            }

            @Override
            public void onRecycleSticker(View view) {
                if (view instanceof AXrLottieImageView) {
                    AXrLottieImageView lottieImageView = (AXrLottieImageView) view;
                    lottieImageView.stopAnimation();
                }
            }

            @Override
            public void onLoadStickerCategory(View view, StickerCategory stickerCategory, boolean selected) {
                if (view instanceof AXrLottieImageView) {
                    AXrLottieImageView lottieImageView = (AXrLottieImageView) view;
                    LottieSticker lottieSticker = (LottieSticker) stickerCategory.getCategoryData();
                    if (lottieSticker.drawable == null) {
                        lottieSticker.drawable = Utils.createFromSticker(view.getContext(), lottieSticker, 50);
                    }
                    lottieImageView.setLottieDrawable(lottieSticker.drawable);
                    //lottieImageView.playAnimation();
                }
            }
        };
    }

    @Override
    public boolean isRecentEnabled() {
        return true;
    }
}
