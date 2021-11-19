package com.frezrik.jiagu.util;

import android.content.Context;
import android.os.Build;

import com.frezrik.jiagu.StubApp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class AssetsUtil {
    public static String copyJiagu(Context context) {
        String absolutePath = Objects.requireNonNull(context.getFilesDir().getParentFile()).getAbsolutePath();
        File jiaguDir = new File(absolutePath, ".jiagu");
        if (!jiaguDir.exists()) {
            jiaguDir.mkdir();
        }
        String destSo = absolutePath + "/.jiagu/libjiagu" + StubApp.VERSION +".so";

        boolean is64 = Build.CPU_ABI.contains("64") || Build.CPU_ABI2.contains("64");
        String soName = is64 ? "libjiagu_64.so" : "libjiagu.so";

        if ("x86".equals(Build.CPU_ABI)) {
            soName = "libjiagu_x86.so";
        }

        writeFile(context, soName, destSo);

        return destSo;
    }

    private static void writeFile(Context context, String in, String out) {
        if (new File(out).exists())
            return;

        File jiaguDir = new File(out).getParentFile();
        if (jiaguDir != null) {
            File[] files = jiaguDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }

        InputStream is = null;
        OutputStream os = null;
        try {
            is = context.getAssets().open(in);
            os = new FileOutputStream(out);
            byte[] buffer = new byte[1024 * 8];
            int read;
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
