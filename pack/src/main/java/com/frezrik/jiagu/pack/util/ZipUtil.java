package com.frezrik.jiagu.pack.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

	public static void unZip(File zip, File dir) {
		try {
			dir.delete();
			ZipFile zipFile = new ZipFile(zip);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry zipEntry = entries.nextElement();
				String name = zipEntry.getName();
				if (name.equals("META-INF/CERT.RSA") || name.equals("META-INF/CERT.SF")
						|| name.equals("META-INF/MANIFEST.MF")) {
					continue;
				}
				if (!zipEntry.isDirectory()) {
					File file = new File(dir, name);
					if (!file.getParentFile().exists())
						file.getParentFile().mkdirs();
					FileOutputStream fos = new FileOutputStream(file);
					InputStream is = zipFile.getInputStream(zipEntry);
					byte[] buffer = new byte[1024];
					int len;
					while ((len = is.read(buffer)) != -1) {
						fos.write(buffer, 0, len);
					}
					is.close();
					fos.close();
				}
			}
			zipFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 打包apk
	 * @param dir apk解压后的文件夹路径
	 * @param zip 输出打包后的apk
	 * @throws Exception
	 */
	public static void zip(File dir, File zip) throws Exception {
		zip.delete();
		CheckedOutputStream cos = new CheckedOutputStream(new FileOutputStream(zip), new CRC32());
		ZipOutputStream zos = new ZipOutputStream(cos);
		compress(dir, zos, "");
		zos.flush();
		zos.close();
	}

	public static void unZipFile(String zip, String src, String dest) {
		try {
			ZipFile zipFile = new ZipFile(zip);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry zipEntry = entries.nextElement();
				String name = zipEntry.getName();
				if (name.equals(src)) {
					FileOutputStream fos = new FileOutputStream(dest);
					InputStream is = zipFile.getInputStream(zipEntry);
					byte[] buffer = new byte[1024];
					int len;
					while ((len = is.read(buffer)) != -1) {
						fos.write(buffer, 0, len);
					}
					is.close();
					fos.close();
					break;
				}
			}
			zipFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void compress(File srcFile, ZipOutputStream zos, String basePath) throws Exception {
		if (srcFile.isDirectory()) {
			compressDir(srcFile, zos, basePath);
		} else {
			compressFile(srcFile, zos, basePath);
		}
	}

	private static void compressDir(File dir, ZipOutputStream zos, String basePath) throws Exception {
		File[] files = dir.listFiles();
		if (files.length < 1) {
			ZipEntry entry = new ZipEntry(basePath + dir.getName() + "/");
			zos.putNextEntry(entry);
			zos.closeEntry();
		}
		for (File file : files) {
			compress(file, zos, basePath + dir.getName() + "/");
		}
	}

	private static void compressFile(File file, ZipOutputStream zos, String dir) throws Exception {

		String dirName = dir + file.getName();

		String[] dirNameNew = dirName.split("/");

		StringBuffer buffer = new StringBuffer();

		if (dirNameNew.length > 1) {
			for (int i = 1; i < dirNameNew.length; i++) {
				buffer.append("/");
				buffer.append(dirNameNew[i]);

			}
		} else {
			buffer.append("/");
		}

		ZipEntry entry = new ZipEntry(buffer.toString().substring(1));
		if ("resources.arsc".equals(file.getName())) {
			entry.setMethod(ZipEntry.STORED);
			entry.setSize(file.length());
			entry.setCrc(calFileCRC32(file));
		}
		zos.putNextEntry(entry);
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		int count;
		byte data[] = new byte[1024];
		while ((count = bis.read(data, 0, 1024)) != -1) {
			zos.write(data, 0, count);
		}
		bis.close();
		zos.closeEntry();
	}
	
	private static long calFileCRC32(File file) throws IOException {
		FileInputStream fi = new FileInputStream(file);
		CheckedInputStream checksum = new CheckedInputStream(fi, new CRC32());
		while (checksum.read() != -1) { }
		long temp = checksum.getChecksum().getValue();
		fi.close();
		checksum.close();
		return temp;
	}
}
