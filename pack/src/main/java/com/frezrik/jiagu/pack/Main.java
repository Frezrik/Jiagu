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

        // ????????????APK
        File apkFile;
        if (apk == null) {
            apkFile = new File(AppManager.TEST_APK);
        } else {
            apkFile = new File(apk);
        }
        Log.d("???????????????" + apkFile.getAbsolutePath());

        // *************1.??????APK*************
        // ??????????????????????????????????????????
        File outputDir = new File(AppManager.OUT_DIR);
        if (outputDir.exists()) {
            FileUtils.deleteAllInDir(new File(AppManager.TEMP_UNZIP));
        }
        // ??????apk???????????????
        File apkUnzipDir = new File(AppManager.TEMP_UNZIP_APK);
        if (!apkUnzipDir.exists()) {
            apkUnzipDir.mkdirs();
        }
        // ??????APK
        ZipUtil.unZip(apkFile, apkUnzipDir);
        // ??????META-INF/CERT.RSA,META-INF/CERT.SF,META-INF/MANIFEST.MF
        FileUtils.delete("/META-INF/CERT.RSA");
        FileUtils.delete("/META-INF/CERT.SF");
        FileUtils.delete("/META-INF/MANIFEST.MF");

        //??????app???application??? ??????app???AndroidManifest.xml???Application???dex??????Application
        String applicationName = AXmlUtil.updateManifest(AppManager.TEMP_MANI);

        // *************** 2.???classes.dex*********************/
        // ???AAR
        File shellDex;
        File shellFile = new File(AppManager.TEST_AAR);
        if (shellFile.exists()) {
            // ????????????????????????
            if (!apkFile.exists() || !shellFile.exists()) {
                Log.d("apkFile or shellFile missing");
                return;
            }
            // ?????????AAR???????????????
            Log.d("?????????AAR");
            File shellUnzipDir = new File(AppManager.TEMP_SHELL);
            if (!shellUnzipDir.exists()) {
                shellUnzipDir.mkdirs();
            }
            // ??????AAR
            ZipUtil.unZip(shellFile, shellUnzipDir);
            // ???jar??????dex
            Log.d("???jar??????dex");
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

        // *************** 3.???classes.dex??????dex?????????????????????dex *********************/
        // ?????????dex??????
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
        // ???dex??????
        byte[] shelldex = new byte[0];
        try {
            shelldex = FileUtils.readFileBytes(shellDex);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // ??????????????????
        byte[] tempDex = new byte[1 + applicationName.length()];
        tempDex[0] = (byte) applicationName.length(); // 1??????application?????????
        System.arraycopy(applicationName.getBytes(), 0, tempDex, 1, tempDex[0]); // app???application???

        // ????????????
        byte[] encryptData = null;
        for (int i = 0; i < dexFiles.length; i++) {
            File file = dexFiles[i];

            try {
                byte[] oridex = FileUtils.readFileBytes(file);
                Log.d("??????dex:" + file.getPath() + " " + oridex.length);

                if (i == 0) {
                    tempDex = Arrays.copyOf(tempDex, tempDex.length + 4 + oridex.length); // ?????? 1??????application????????? + app???application??? + 4?????????dex?????? + ???dex
                    System.arraycopy(DexUtils.intToByte(oridex.length), 0, tempDex, tempDex.length - 4 - oridex.length, 4); // 4?????????dex??????
                    System.arraycopy(oridex, 0, tempDex, tempDex.length - oridex.length, oridex.length); // ???dex
                    encryptData = EncryptUtils.encrypt(tempDex, 512);
                } else {
                    encryptData = Arrays.copyOf(encryptData, encryptData.length + 4 + oridex.length); // ??????
                    System.arraycopy(DexUtils.intToByte(oridex.length), 0, encryptData, encryptData.length - 4 - oridex.length, 4); // 4?????????dex??????
                    tempDex = EncryptUtils.encryptXor(oridex);
                    System.arraycopy(tempDex, 0, encryptData, encryptData.length - oridex.length, oridex.length);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            file.delete();
        }
        // ??????dex
        try {
            DexUtils.mergeDex(dexFiles[0].getAbsolutePath(), shelldex, encryptData);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // *************** 4.copy so *********************/
        // ??????assets??????
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

        // *************** 5.??????APK *********************/
        Log.d("??????APK");
        File unsignedApk = new File("output/unsigned.apk");
        try {
            ZipUtil.zip(apkUnzipDir, unsignedApk);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!new File(AppManager.TEMP_SHELL).exists()) {
            FileUtils.delete(AppManager.TEMP_UNZIP);
        }

        // *************** 6.??????apk *********************/
        Log.d("??????APK");
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
