package com.hyapp.achat;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDexApplication;

import com.aghajari.rlottie.AXrLottie;
import com.aghajari.rlottie.AXrLottieImageView;
import com.hyapp.achat.ui.emojiview.iosprovider.AXIOSEmojiProvider;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.hyapp.achat.da.ObjectBox;
import com.hyapp.achat.ui.emojiview.AXEmojiManager;
import com.hyapp.achat.ui.emojiview.listener.StickerViewCreatorListener;
import com.hyapp.achat.ui.emojiview.sticker.StickerCategory;

public class App extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
        ObjectBox.init(this);
        AXrLottie.init(this);
        AXEmojiManager.install(this, new AXIOSEmojiProvider(this));
        AXEmojiManager.setStickerViewCreatorListener(new StickerViewCreatorListener() {
            @Override
            public View onCreateStickerView(@NonNull Context context, @Nullable StickerCategory category, boolean isRecent) {
                return new AXrLottieImageView(context);
            }

            @Override
            public View onCreateCategoryView(@NonNull Context context) {
                return new AXrLottieImageView(context);
            }
        });
    }
}
