package com.frezrik.jiagu.pack.util;

import com.frezrik.jiagu.pack.core.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

	public static List<String> unZip(File zip, File dir) {
		List<String> rawPathList = new ArrayList<>();
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
					if (zipEntry.getCompressedSize() == zipEntry.getSize()) {
						String rawPath = file.getAbsolutePath();
						rawPathList.add(rawPath.substring(rawPath.indexOf("unzip")));
					}
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

		return rawPathList;
	}

	/**
	 * 打包apk
	 * @param dir apk解压后的文件夹路径
	 * @param zip 输出打包后的apk
	 * @param rawPathList 不压缩的文件
	 * @throws Exception
	 */
	public static void zip(File dir, File zip, List<String> rawPathList) throws Exception {
		zip.delete();
		CheckedOutputStream cos = new CheckedOutputStream(new FileOutputStream(zip), new CRC32());
		ZipOutputStream zos = new ZipOutputStream(cos);
		compress(dir, zos, "", rawPathList);
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

	private static void compress(File srcFile, ZipOutputStream zos, String basePath,
								 List<String> rawPathList) throws Exception {
		if (srcFile.isDirectory()) {
			compressDir(srcFile, zos, basePath, rawPathList);
		} else {
			compressFile(srcFile, zos, basePath, rawPathList);
		}
	}

	private static void compressDir(File dir, ZipOutputStream zos, String basePath,
									List<String> rawPathList) throws Exception {
		File[] files = dir.listFiles();
		if (files.length < 1) {
			ZipEntry entry = new ZipEntry(basePath + dir.getName() + "/");
			zos.putNextEntry(entry);
			zos.closeEntry();
		}
		for (File file : files) {
			compress(file, zos, basePath + dir.getName() + "/", rawPathList);
		}
	}

	private static void compressFile(File file, ZipOutputStream zos, String dir,
									 List<String> rawPathList) throws Exception {

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
		String rawPath = file.getAbsolutePath();
		if (rawPathList.contains(rawPath.substring(rawPath.indexOf("unzip")))) { // 打包时不能压缩
			Log.d("raw:   " + rawPath);

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
