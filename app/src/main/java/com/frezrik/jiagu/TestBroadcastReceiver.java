package com.frezrik.jiagu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TestBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("JIAGU_TEST", "onCreate[BroadcastReceiver] ==> " + context.getApplicationContext().getClass().getName());
    }
}