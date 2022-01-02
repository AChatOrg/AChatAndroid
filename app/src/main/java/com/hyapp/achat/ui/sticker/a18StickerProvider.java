package com.hyapp.achat.ui.sticker;

import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.graphics.drawable.DrawableCompat;

import com.hyapp.achat.ui.emojiview.AXEmojiManager;
import com.hyapp.achat.ui.emojiview.sticker.Sticker;
import com.hyapp.achat.ui.emojiview.sticker.StickerCategory;
import com.hyapp.achat.ui.emojiview.sticker.StickerLoader;
import com.hyapp.achat.ui.emojiview.sticker.StickerProvider;

public class a18StickerProvider implements StickerProvider {
    @NonNull
    @Override
    public StickerCategory[] getCategories() {
        return new StickerCategory[]{
                new a18Stickers(),
                new a18Stickers(),
                new a18Stickers()
        };
    }

    @NonNull
    @Override
    public StickerLoader getLoader() {
        return new StickerLoader() {
            @Override
            public void onLoadSticker(View view, Sticker sticker) {
                if (sticker.isInstance(Integer.class)) {
                    ((AppCompatImageView) view).setImageResource((Integer) sticker.getData());
                }

            }

            @Override
            public void onLoadStickerCategory(View view, StickerCategory stickerCategory, boolean selected) {
                try {
                    if (false/*stickerCategory instanceof ShopStickers*/) {
                        Drawable dr0 = AppCompatResources.getDrawable(view.getContext(), (Integer) stickerCategory.getCategoryData());
                        Drawable dr = dr0.getConstantState().newDrawable();
                        if (selected) {
                            DrawableCompat.setTint(DrawableCompat.wrap(dr), AXEmojiManager.getStickerViewTheme().getSelectedColor());
                        } else {
                            DrawableCompat.setTint(DrawableCompat.wrap(dr), AXEmojiManager.getStickerViewTheme().getDefaultColor());
                        }
                        ((AppCompatImageView) view).setImageDrawable(dr);
                    } else {
                        ((AppCompatImageView) view).setImageResource(Integer.parseInt(stickerCategory.getCategoryData().toString()));
                    }
                } catch (Exception ignore) {
                }
            }
        };
    }

    @Override
    public boolean isRecentEnabled() {
        return true;
    }
}