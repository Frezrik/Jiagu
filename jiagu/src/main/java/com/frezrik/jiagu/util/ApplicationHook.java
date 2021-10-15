package com.frezrik.jiagu.util;

import android.app.Application;
import android.content.ContentProvider;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class ApplicationHook {

    /**
     * hook 替换原有的代理Application
     *
     * @param application
     */
    public static Application hook(Application application, String delegateApplicationName) {
        if (TextUtils.isEmpty(delegateApplicationName) || "com.frezrik.jiagu.StubApp".equals(delegateApplicationName)) {
            return application;
        }
        compatible28();
        Application delegateApplication = null;
        try {
            // 先获取到ContextImpl对象
            Context contextImpl = application.getBaseContext();
            // 创建插件中真实的Application且，执行生命周期
            ClassLoader classLoader = application.getClassLoader();
            Class<?> applicationClass = classLoader.loadClass(delegateApplicationName);
            delegateApplication = (Application) applicationClass.newInstance();

            Reflect.invokeMethod(Application.class, delegateApplication,
                    new Object[]{contextImpl}, "attach",
                    Context.class);

            // 替换ContextImpl的代理Application
            Reflect.invokeMethod(contextImpl.getClass(), contextImpl,
                    new Object[]{delegateApplication}, "setOuterContext",
                    Context.class);

            // 替换LoadedApk的代理Application
            Object loadedApk = Reflect.getFieldValue(contextImpl.getClass(), contextImpl,
                    "mPackageInfo");
            Reflect.setFieldValue("android.app.LoadedApk", loadedApk, "mApplication",
                    delegateApplication);

            // 替换ActivityThread的代理Application
            Object mMainThread = Reflect.getFieldValue(contextImpl.getClass(), contextImpl,
                    "mMainThread");
            Reflect.setFieldValue("android.app.ActivityThread", mMainThread, "mInitialApplication",
                    delegateApplication);
            ArrayList<Application> mAllApplications =
                    (ArrayList<Application>) Reflect.getFieldValue("android.app.ActivityThread",
                            mMainThread, "mAllApplications");
            mAllApplications.remove(application);
            mAllApplications.add(delegateApplication);

            // 替换LoadedApk中的mApplicationInfo中name
            ApplicationInfo applicationInfo =
                    (ApplicationInfo) Reflect.getFieldValue("android.app.LoadedApk",
                            loadedApk
                            , "mApplicationInfo");
            applicationInfo.className = delegateApplicationName;

            delegateApplication.onCreate();
            replaceContentProvider(mMainThread, delegateApplication);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("NDK_JIAGU", "hook application finish");
        return delegateApplication == null ? application : delegateApplication;
    }

    private static void compatible28() {
        if (Build.VERSION.SDK_INT == 28) {
            try {
                Class.forName("android.content.pm.PackageParser$Package").getDeclaredConstructor(String.class).setAccessible(true);
            } catch (Throwable th) {
            }
            try {
                Class<?> cls = Class.forName("android.app.ActivityThread");
                Method declaredMethod = cls.getDeclaredMethod("currentActivityThread",
                        new Class[0]);
                declaredMethod.setAccessible(true);
                Object invoke = declaredMethod.invoke(null, new Object[0]);
                Field declaredField = cls.getDeclaredField("mHiddenApiWarningShown");
                declaredField.setAccessible(true);
                declaredField.setBoolean(invoke, true);
            } catch (Throwable th2) {
            }
        }
    }


    /**
     * 修改已经存在ContentProvider中application
     *
     * @param activityThread
     * @param delegateApplication
     */
    private static void replaceContentProvider(Object activityThread,
                                               Application delegateApplication) {
        try {
            Map<Object, Object> mProviderMap =
                    (Map<Object, Object>) Reflect.getFieldValue(activityThread.getClass(),
                            activityThread, "mProviderMap");
            Set<Map.Entry<Object, Object>> entrySet = mProviderMap.entrySet();
            for (Map.Entry<Object, Object> entry : entrySet) {
                // 取出ContentProvider
                ContentProvider contentProvider =
                        (ContentProvider) Reflect.getFieldValue(entry.getValue().getClass(),
                                entry.getValue(), "mLocalProvider");

                if (contentProvider != null) {
                    // 修改ContentProvider中的context
                    Reflect.setFieldValue("android.content.ContentProvider", contentProvider,
                            "mContext", delegateApplication);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
