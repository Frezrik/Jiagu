package com.frezrik.jiagu;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.frezrik.jiagu.util.AssetsUtil;

public class StubApp extends Application {

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);

        Log.d("NDK_JIAGU", "StubApp attachBaseContext");

        System.load(AssetsUtil.copyJiagu(context));

        attach(this);
    }

    public native static void attach(StubApp base);
}
