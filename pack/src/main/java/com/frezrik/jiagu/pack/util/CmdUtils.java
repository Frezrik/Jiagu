package com.frezrik.jiagu.pack.util;

import com.frezrik.jiagu.pack.core.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class CmdUtils {

    /**
     * 执行cmd命令
     *
     * @param tag 日志TAG
     * @param cmd
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public static void exec(String tag, String cmd) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(cmd);
        Log.d("start " + tag);
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw e;
        }
        if (process.exitValue() != 0) {
            InputStream inputStream = process.getErrorStream();
            int len;
            byte[] buffer = new byte[2048];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ((len = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            Log.d(new String(bos.toByteArray(), StandardCharsets.UTF_8));
            throw new RuntimeException(tag + " execute fail");
        }
        Log.d("finish " + tag);
        process.destroy();
    }
}
