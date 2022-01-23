package com.hyapp.achat

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.multidex.MultiDexApplication
import com.aghajari.rlottie.AXrLottie
import com.aghajari.rlottie.AXrLottieImageView
import com.facebook.drawee.backends.pipeline.Fresco
import com.hyapp.achat.model.Preferences
import com.hyapp.achat.model.objectbox.ObjectBox
import com.hyapp.achat.view.component.emojiview.AXEmojiManager
import com.hyapp.achat.view.component.emojiview.iosprovider.AXIOSEmojiProvider
import com.hyapp.achat.view.component.emojiview.listener.StickerViewCreatorListener
import com.hyapp.achat.view.component.emojiview.sticker.StickerCategory

class App : MultiDexApplication() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @JvmStatic
        lateinit var context: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        Preferences.init(context)
        Fresco.initialize(context)
        ObjectBox.init(context)
        AXrLottie.init(context)
        AXEmojiManager.install(context, AXIOSEmojiProvider(context))
        AXEmojiManager.setStickerViewCreatorListener(object : StickerViewCreatorListener {
            override fun onCreateStickerView(
                context: Context,
                category: StickerCategory<*>?,
                isRecent: Boolean
            ): View {
                return AXrLottieImageView(context)
            }

            override fun onCreateCategoryView(context: Context): View {
                return AXrLottieImageView(context)
            }
        })
    }
}