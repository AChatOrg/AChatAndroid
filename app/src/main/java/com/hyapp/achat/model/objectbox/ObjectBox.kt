package com.hyapp.achat.model.objectbox

import android.content.Context
import com.hyapp.achat.model.entity.MyObjectBox
import io.objectbox.BoxStore

object ObjectBox {

    @JvmStatic
    lateinit var store: BoxStore
        private set

    @JvmStatic
    fun init(context: Context) {
        store = MyObjectBox.builder()
            .androidContext(context.applicationContext)
            .build()
    }
}