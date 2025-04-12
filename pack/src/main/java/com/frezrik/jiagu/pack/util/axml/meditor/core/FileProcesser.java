package com.frezrik.jiagu.pack.util.axml.meditor.core;

import com.frezrik.jiagu.pack.util.axml.meditor.property.ModificationProperty;
import com.frezrik.jiagu.pack.util.axml.meditor.utils.Log;
import com.frezrik.jiagu.pack.util.axml.meditor.utils.Utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author Windysha
 */
public class FileProcesser {

    public static void processApkFile(String srcApkPath, String dstApkPath, ModificationProperty property) {
        FileOutputStream outputStream = null;
        ZipOutputStream zipOutputStream = null;
        ZipFile zipFile = null;

        long time = System.currentTimeMillis();

        try {
            outputStream = new FileOutputStream(dstApkPath);
            zipOutputStream = new ZipOutputStream(outputStream);

            try {
                zipFile = new ZipFile(srcApkPath, Charset.forName("gbk"));
            } catch (Throwable e) {
                zipFile = new ZipFile(srcApkPath);
            }

            for (Enumeration entries = zipFile.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String zipEntryName = entry.getName();

//                Log.d(" zipEntryName = " + zipEntryName);

                // ignore signature files, we will resign it.
                if (zipEntryName.startsWith("META-INF")) {
                    continue;
                }

                InputStream zipInputStream = null;
                try {
                    zipInputStream = zipFile.getInputStream(entry);

                    ZipEntry zosEntry = new ZipEntry(entry.getName());
                    zosEntry.setComment(entry.getComment());
                    zosEntry.setExtra(entry.getExtra());

                    zipOutputStream.putNextEntry(zosEntry);
                    if ("AndroidManifest.xml".equals(zipEntryName)) {
                        // if it is manifest file, modify it.
                        new ManifestEditor(zipInputStream, zipOutputStream, property).processManifest();
                    } else if (!entry.isDirectory()) {
                        Utils.copyStream(zipInputStream, zipOutputStream);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    Utils.close(zipInputStream);
                }
                zipOutputStream.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utils.close(zipOutputStream);
            Utils.close(outputStream);
            Utils.close(zipFile);

            Log.i(" processApkFile time --> " + (System.currentTimeMillis() - time) + " ms");
        }
    }

    public static String processManifestFile(String srcManifestPath, String dstManifestPath, ModificationProperty property) {
        return new ManifestEditor(srcManifestPath, dstManifestPath, property).processManifest();
    }
}
