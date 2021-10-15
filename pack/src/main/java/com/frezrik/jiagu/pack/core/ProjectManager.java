package com.frezrik.jiagu.pack.core;

import java.io.File;

public class ProjectManager {
    /**
     * 获取根路径，如果released返回的是该jar的File对象，否则是工程的根目录
     * @return
     */
    private static File getRoot(){
        return new File(AppManager.class.getProtectionDomain().getCodeSource().getLocation().getFile());
    }

    /**
     * 获取运行时目录
     * @return
     */
    public static File getRuntimeDir(){
        if (getRoot().getPath().contains("pack\\build\\classes\\java\\main"))
            return getRoot().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
        return getRoot().getParentFile();
    }

    /**
     * 获取运行时目录
     * @return
     */
    public static String getApktool(){
        return new File(getRuntimeDir(), "packenv/apktool_2.6.0.jar").getAbsolutePath();
    }
}
