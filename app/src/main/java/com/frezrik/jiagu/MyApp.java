package com.frezrik.jiagu;

import android.app.Application;
import android.util.Log;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("NDK_JIAGU", "MyApp onCreate ==> " + getApplicationContext().getClass().getName());
    }
}
