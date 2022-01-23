package com.frezrik.jiagu;

import android.content.Intent;
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
        tv.setText(stringFromJNI());

        mIntent = new Intent("com.frezrik.jiagu.testService");
        mIntent.setPackage(getPackageName());
        startService(mIntent);
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