package com.frezrik.jiagu.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUtil {

    /**
     * 直接从apk中读取dex数据
     *
     * @param zip
     * @return
     */
    public static byte[] getDexData(String zip) {
        ZipFile zf = null;
        ZipEntry entry;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            zf = new ZipFile(zip);
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                if (entry.getName().indexOf("/") > 0) {
                    continue;
                }
                if (!entry.isDirectory()) {
                    if ("classes.dex".equals(entry.getName())) {
                        InputStream is = zf.getInputStream(entry);
                        int len = 0;
                        byte[] bytes = new byte[1024 * 8];
                        while ((len = is.read(bytes)) != -1) {
                            byteArrayOutputStream.write(bytes, 0, len);
                        }
                        is.close();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (zf != null) {
                try {
                    zf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return byteArrayOutputStream.toByteArray();
    }
}
