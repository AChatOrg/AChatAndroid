package com.hyapp.achat.viewmodel.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings

object SecureUtils {

    @SuppressLint("HardwareIds")
    @JvmStatic
    fun androidId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}