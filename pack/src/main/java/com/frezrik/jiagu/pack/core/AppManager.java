package com.frezrik.jiagu.pack.core;

import java.io.File;

public class AppManager {
    /**
     * 壳Application名
     */
    public static final String PROXY_APPLICATION_NAME = "com.frezrik.jiagu.StubApp";

    /**
     * 获取运行时目录
     */
    public static final String RUNTIME_PATH = ProjectManager.getRuntimeDir().getAbsolutePath() + File.separator;

    /**
     * 待加固应用
     */
    public static final String IN_DIR = RUNTIME_PATH + "input/";

    /**
     * 加固应用输出
     */
    public static final String OUT_DIR = RUNTIME_PATH + "output/";

    /**
     * 执行依赖文件路径
     */
    public static final String BIN_PATH = RUNTIME_PATH + "bin/";

    /**
     * 壳DEX路径
     */
    public static final String DEX_PATH = BIN_PATH + "classes.dex";

    /************ 临时文件 *************/
    // 解压路径
    public static final String TEMP_UNZIP = OUT_DIR + "unzip/";
    // apk解压路径
    public static final String TEMP_UNZIP_APK = TEMP_UNZIP + "apk/";
    // AndroidManifest.xml
    public static final String TEMP_MANI = TEMP_UNZIP_APK + "AndroidManifest.xml";
    // 壳Dex处理路径
    public static final String TEMP_SHELL = TEMP_UNZIP + "shell";
    /************ 临时文件 *************/





    /************ debug配置 *************/
    // 调试应用路径
    public static final String TEST_APK = "app/build/outputs/apk/release/app-release.apk";
    // 调试jiagu.aar
    public static final String TEST_AAR = "jiagu/build/outputs/aar/jiagu-release.aar";
    // 调试so文件夹
    public static final String TEST_SO = TEMP_UNZIP + "shell/jni/";
    /************ debug配置 *************/

}
