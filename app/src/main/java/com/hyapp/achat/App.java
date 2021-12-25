package com.hyapp.achat;

import androidx.multidex.MultiDexApplication;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.hyapp.achat.da.ObjectBox;

public class App extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
        ObjectBox.init(this);
    }
}
