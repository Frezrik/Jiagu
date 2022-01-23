package com.frezrik.jiagu;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;

import com.frezrik.jiagu.util.ApplicationHook;
import com.frezrik.jiagu.util.AssetsUtil;

public class StubApp extends Application {
    /**
     * so的版本，格式为: v + 数字
     */
    public static final String VERSION = "v1";

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);

        System.load(AssetsUtil.copyJiagu(context));

        attach(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ApplicationHook.replaceApplicationContext(this);
    }

    @Override
    public String getPackageName() {
        return "JIAGU"; // 如果有ContentProvider，修改getPackageName后会重走createPackageContext
    }

    @Override
    public Context createPackageContext(String packageName, int flags) throws PackageManager.NameNotFoundException {
        return ApplicationHook.replaceContentProvider(this);
    }

    public native static void attach(StubApp base);
}
