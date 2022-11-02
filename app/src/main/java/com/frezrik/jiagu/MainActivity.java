package com.frezrik.jiagu;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("JIAGU_TEST", "onCreate[Activity] ==> " + getApplicationContext().getClass().getName());

        TextView tv = findViewById(R.id.sample_text);
        tv.setText(stringFromJNI()
                + "\nBuild.CPU_ABI: " + Build.CPU_ABI
                + "\nBuild.CPU_ABI2: " + Build.CPU_ABI2
        );

        mIntent = new Intent("com.frezrik.jiagu.testService");
        mIntent.setPackage(getPackageName());
        startService(mIntent);

        MediaPlayer.create(this, R.raw.beep).start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(mIntent);
    }

    public native String stringFromJNI();

    static {
        System.loadLibrary("native-lib");
    }
}