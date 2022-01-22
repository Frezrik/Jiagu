package com.frezrik.jiagu.pack;

import com.frezrik.jiagu.pack.core.AppManager;
import com.frezrik.jiagu.pack.core.Log;
import com.frezrik.jiagu.pack.util.AXmlUtil;
import com.frezrik.jiagu.pack.util.DexUtils;
import com.frezrik.jiagu.pack.util.EncryptUtils;
import com.frezrik.jiagu.pack.util.FileUtils;
import com.frezrik.jiagu.pack.util.SignUtils;
import com.frezrik.jiagu.pack.util.ZipUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class Main {
    //java -jar pack.jar -apk test.apk -key keystore/test.jks -kp test123 -alias test -ap test123
    private static String apk;
    private static String key;
    private static String keyPwd;
    private static String alias;
    private static String aliasPwd;

    private static void parse(String[] args) {
        int opti = 0;
        while (opti < args.length) {
            String opt = args[opti];
            if (opt == null || opt.length() <= 0 || opt.charAt(0) != '-') {
                break;
            }
            opti++;

            if ("-apk".equals(opt)) {
                if (opti < args.length) {
                    apk = args[opti];
                    opti++;
                } else {
                    Log.d("Error: -apk option requires apk argument");
                    return;
                }
            } else if ("-key".equals(opt)) {
                if (opti < args.length) {
                    key = args[opti];
                    opti++;
                } else {
                    Log.d("Error: -key option requires keystore argument");
                    return;
                }
            } else if ("-kp".equals(opt)) {
                if (opti < args.length) {
                    keyPwd = args[opti];
                    opti++;
                } else {
                    Log.d("Error: -kp option requires keystore password argument");
                    return;
                }
            } else if ("-alias".equals(opt)) {
                if (opti < args.length) {
                    alias = args[opti];
                    opti++;
                } else {
                    Log.d("Error: -alias option requires keystore alias argument");
                    return;
                }
            } else if ("-ap".equals(opt)) {
                if (opti < args.length) {
                    aliasPwd = args[opti];
                    opti++;
                } else {
                    Log.d("Error: -ap option requires keystore alias password argument");
                    return;
                }
            }
        }
    }

    public static void main(String[] args) {
        parse(args);

        // 待加固的APK
        File apkFile;
        if (apk == null) {
            apkFile = new File(AppManager.TEST_APK);
        } else {
            apkFile = new File(apk);
        }
        Log.d("正在加固：" + apkFile.getAbsolutePath());

        // *************1.解压APK*************
        // 先删除输出文件夹下的所有文件
        File outputDir = new File(AppManager.OUT_DIR);
        if (outputDir.exists()) {
            FileUtils.deleteAllInDir(new File(AppManager.TEMP_UNZIP));
        }
        // 创建apk的解压目录
        File apkUnzipDir = new File(AppManager.TEMP_UNZIP_APK);
        if (!apkUnzipDir.exists()) {
            apkUnzipDir.mkdirs();
        }
        // 解压APK
        ZipUtil.unZip(apkFile, apkUnzipDir);
        // 删除META-INF/CERT.RSA,META-INF/CERT.SF,META-INF/MANIFEST.MF
        FileUtils.delete("/META-INF/CERT.RSA");
        FileUtils.delete("/META-INF/CERT.SF");
        FileUtils.delete("/META-INF/MANIFEST.MF");

        //获取app的application名 修改app的AndroidManifest.xml的Application为dex壳的Application
        String applicationName = AXmlUtil.updateManifest(AppManager.TEMP_MANI);

        // *************** 2.壳classes.dex*********************/
        // 壳AAR
        File shellDex;
        File shellFile = new File(AppManager.TEST_AAR);
        if (shellFile.exists()) {
            // 判断文件是否存在
            if (!apkFile.exists() || !shellFile.exists()) {
                Log.d("apkFile or shellFile missing");
                return;
            }
            // 创建壳AAR的解压目录
            Log.d("解压壳AAR");
            File shellUnzipDir = new File(AppManager.TEMP_SHELL);
            if (!shellUnzipDir.exists()) {
                shellUnzipDir.mkdirs();
            }
            // 解压AAR
            ZipUtil.unZip(shellFile, shellUnzipDir);
            // 将jar转成dex
            Log.d("将jar转成dex");
            File shellJar = new File(shellUnzipDir, "classes.jar");
            shellDex = new File(shellUnzipDir, "classes.dex");
            try {
                DexUtils.dxCommand(shellJar, shellDex);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            shellDex = new File(AppManager.DEX_PATH);
        }

        // *************** 3.壳classes.dex与源dex加密合并成一个dex *********************/
        // 获取源dex文件
        File[] dexFiles = apkUnzipDir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".dex");
            }
        });
        Collections.sort(Arrays.asList(dexFiles), new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.getName().length() < o2.getName().length())
                    return -1;
                return o1.getName().compareTo(o2.getName());
            }
        });
        // 壳dex数据
        byte[] shelldex = new byte[0];
        try {
            shelldex = FileUtils.readFileBytes(shellDex);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 待加密的数据
        byte[] tempDex = new byte[1 + applicationName.length()];
        tempDex[0] = (byte) applicationName.length(); // 1字节application名长度
        System.arraycopy(applicationName.getBytes(), 0, tempDex, 1, tempDex[0]); // app的application名

        // 数据加密
        byte[] encryptData = null;
        for (int i = 0; i < dexFiles.length; i++) {
            File file = dexFiles[i];

            try {
                byte[] oridex = FileUtils.readFileBytes(file);
                Log.d("加密dex:" + file.getPath() + " " + oridex.length);

                if (i == 0) {
                    tempDex = Arrays.copyOf(tempDex, tempDex.length + 4 + oridex.length); // 扩容 1字节application名长度 + app的application名 + 4字节源dex大小 + 源dex
                    System.arraycopy(DexUtils.intToByte(oridex.length), 0, tempDex, tempDex.length - 4 - oridex.length, 4); // 4字节源dex大小
                    System.arraycopy(oridex, 0, tempDex, tempDex.length - oridex.length, oridex.length); // 源dex
                    encryptData = EncryptUtils.encrypt(tempDex, 512);
                } else {
                    encryptData = Arrays.copyOf(encryptData, encryptData.length + 4 + oridex.length); // 扩容
                    System.arraycopy(DexUtils.intToByte(oridex.length), 0, encryptData, encryptData.length - 4 - oridex.length, 4); // 4字节源dex大小
                    tempDex = EncryptUtils.encryptXor(oridex);
                    System.arraycopy(tempDex, 0, encryptData, encryptData.length - oridex.length, oridex.length);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            file.delete();
        }
        // 合并dex
        try {
            DexUtils.mergeDex(dexFiles[0].getAbsolutePath(), shelldex, encryptData);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // *************** 4.copy so *********************/
        // 创建assets目录
        File assetsDir = new File(apkUnzipDir, "assets");
        if (!assetsDir.exists()) {
            assetsDir.mkdirs();
        }

        String so_path;
        if (new File(AppManager.TEST_SO).exists()) {
            so_path = AppManager.TEST_SO;
        } else {
            so_path = AppManager.SO_PATH;
        }
        File so = new File(so_path + "armeabi-v7a/libjiagu.so");
        File so_64 = new File(so_path + "arm64-v8a/libjiagu.so");
        File so_x86 = new File(so_path + "x86/libjiagu.so");

        FileUtils.copy(so, new File(assetsDir, "libjiagu.so"));
        FileUtils.copy(so_64, new File(assetsDir, "libjiagu_64.so"));
        FileUtils.copy(so_x86, new File(assetsDir, "libjiagu_x86.so"));

        // *************** 5.打包APK *********************/
        Log.d("打包APK");
        File unsignedApk = new File("output/unsigned.apk");
        try {
            ZipUtil.zip(apkUnzipDir, unsignedApk);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!new File(AppManager.TEMP_SHELL).exists()) {
            FileUtils.delete(AppManager.TEMP_UNZIP);
        }

        // *************** 6.签名apk *********************/
        Log.d("签名APK");
        File signedApk = new File(AppManager.OUT_DIR + apkFile.getName().substring(0,
                apkFile.getName().length() - 4) + "_signed.apk");
        try {
            SignUtils.apkSignature(unsignedApk, signedApk, key, keyPwd, alias, aliasPwd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("Finished!!!");
    }

}
