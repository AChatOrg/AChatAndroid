package com.hyapp.achat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDexApplication;

import com.aghajari.rlottie.AXrLottie;
import com.aghajari.rlottie.AXrLottieImageView;
import com.hyapp.achat.model.Preferences;
import com.hyapp.achat.view.component.emojiview.iosprovider.AXIOSEmojiProvider;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.hyapp.achat.model.objectbox.ObjectBox;
import com.hyapp.achat.view.component.emojiview.AXEmojiManager;
import com.hyapp.achat.view.component.emojiview.listener.StickerViewCreatorListener;
import com.hyapp.achat.view.component.emojiview.sticker.StickerCategory;

public class App extends MultiDexApplication {

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        Preferences.init(context);
        Fresco.initialize(context);
        ObjectBox.init(context);
        AXrLottie.init(context);
        AXEmojiManager.install(context, new AXIOSEmojiProvider(context));
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

    public static Context getContext() {
        return context;
    }
}
