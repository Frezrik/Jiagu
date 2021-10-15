package com.frezrik.jiagu.pack.util;

import com.frezrik.jiagu.pack.core.AppManager;

import java.io.File;
import java.io.IOException;

public class SignUtils {

    private SignUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * V1签名
     */
    private static String signature(File unsignedApk, String keyStore, String keyPwd,
                                    String alias, String alisaPwd)
            throws InterruptedException, IOException {
        String path = unsignedApk.getAbsolutePath();
        String v1Name = path.substring(0, path.indexOf(".apk")) + "_v1.apk";
        String cmd[] = {"cmd.exe", "/C ", "jarsigner", "-sigalg", "SHA1withRSA", "-digestalg",
                "SHA1", "-keystore",
                keyStore, "-storepass", keyPwd, "-keypass", alisaPwd, "-signedjar", v1Name,
                unsignedApk.getAbsolutePath(), alias};

        CmdUtils.exec("v1 sign", cmd);

        FileUtils.delete(path);

        return v1Name;
    }

    // zipalign -p 4 input\app-release-unsigned.apk input\app-release-unsigned.apk
    private static String apkZipalign(String v1Apk) throws IOException, InterruptedException {
        String zipalignName = v1Apk.substring(0, v1Apk.indexOf(".apk")) + "_align.apk";
        String cmd[] = {"cmd.exe", "/C ", AppManager.RUNTIME_PATH + "bin/zipalign", "-p", "4", v1Apk, zipalignName};

        CmdUtils.exec("zipalign", cmd);

        FileUtils.delete(v1Apk);

        return zipalignName;
    }

    //apksigner.jar sign  --ks key.jks --ks-key-alias releasekey  --ks-pass pass:pp123456
    // --key-pass pass:pp123456  --out output.apk  input.apk
    public static void apkSignature(File unsignedApk, File signedApk, String keyStore,
                                    String keyPwd, String alias, String alisaPwd) throws IOException, InterruptedException {
        String v1Name = signature(unsignedApk, keyStore, keyPwd, alias, alisaPwd);
        String zipalignName = apkZipalign(v1Name);
        String[] cmd = {"cmd.exe", "/C ", AppManager.RUNTIME_PATH + "bin/apksigner", "sign", "--ks", keyStore, "--ks-pass",
                "pass:" + keyPwd,
                "--ks-key-alias", alias, "--key-pass", "pass:" + alisaPwd,
                "--out", signedApk.getAbsolutePath(), zipalignName};

        CmdUtils.exec("v2 sign", cmd);

        FileUtils.delete(zipalignName);
        FileUtils.delete(signedApk.getAbsolutePath() + ".idsig");
    }
}
