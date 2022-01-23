package com.frezrik.jiagu;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class TestService extends Service {
    public TestService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("JIAGU_TEST", "onCreate[Service] ==> " + getApplicationContext().getClass().getName());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}