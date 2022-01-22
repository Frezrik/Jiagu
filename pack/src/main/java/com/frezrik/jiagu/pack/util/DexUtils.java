package com.frezrik.jiagu.pack.util;

import com.frezrik.jiagu.pack.core.AppManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Adler32;

public class DexUtils {

    private DexUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static void dxCommand(File jar, File dex) throws IOException, InterruptedException {
        String cmd = AppManager.CMD_RUNNER + AppManager.BIN_PATH + "dx --dex --output=" + dex.getAbsolutePath() + " " + jar.getAbsolutePath();
        CmdUtils.exec("dx", cmd);
    }

    /**
     * int转byte[]
     *
     * @param number
     *
     * @return
     */
    public static byte[] intToByte(int number) {
        byte[] b = new byte[4];
        for (int i = 3; i >= 0; i--) {
            b[i] = (byte) (number & 0xFF);
            number >>= 8;
        }
        return b;
    }

    /**
     * 合并 加密数据到指定路径
     *
     * @param output
     * @param shelldex
     * @param encryptDex 需要加密的数据
     */
    public static void mergeDex(String output, byte[] shelldex, byte[] encryptDex) {
        byte[] temp = new byte[shelldex.length + encryptDex.length + 4];
        System.arraycopy(shelldex, 0, temp, 0, shelldex.length); // 壳dex
        System.arraycopy(encryptDex, 0, temp, shelldex.length, encryptDex.length); // 加密数据
        System.arraycopy(DexUtils.intToByte(shelldex.length), 0, temp,
                shelldex.length + encryptDex.length, 4); // 4字节壳dex大小

        FileOutputStream fos = null;
        try {
            fixDex(temp);
            fos = new FileOutputStream(output);
            fos.write(temp);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理dex
     *
     * @param newdex
     *
     * @throws NoSuchAlgorithmException
     */
    private static void fixDex(byte[] newdex) throws NoSuchAlgorithmException {
        //修改DEX file size文件头
        fixFileSizeHeader(newdex);
        //修改DEX SHA1 文件头
        fixSHA1Header(newdex);
        //修改DEX CheckSum文件头
        fixCheckSumHeader(newdex);
    }

    /**
     * 修改dex头，CheckSum 校验码
     *
     * @param dexBytes
     */
    private static void fixCheckSumHeader(byte[] dexBytes) {
        Adler32 adler = new Adler32();
        adler.update(dexBytes, 12, dexBytes.length - 12);//从12到文件末尾计算校验码
        long value = adler.getValue();
        int va = (int) value;
        byte[] newcs = intToByte(va);
        byte[] recs = new byte[4];
        for (int i = 0; i < 4; i++) {
            recs[i] = newcs[newcs.length - 1 - i];
        }
        System.arraycopy(recs, 0, dexBytes, 8, 4);//效验码赋值（8-11）
    }

    /**
     * 修改dex头 sha1值
     *
     * @param dexBytes
     *
     * @throws NoSuchAlgorithmException
     */
    private static void fixSHA1Header(byte[] dexBytes)
            throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(dexBytes, 32, dexBytes.length - 32);//从32位到结束计算sha--1
        byte[] newdt = md.digest();
        System.arraycopy(newdt, 0, dexBytes, 12, 20);//修改sha-1值（12-31）
    }

    /**
     * 修改dex头 file_size值
     *
     * @param dexBytes
     */
    private static void fixFileSizeHeader(byte[] dexBytes) {
        //新文件长度
        byte[] newfs = intToByte(dexBytes.length);
        byte[] refs = new byte[4];
        //高位在前，低位在前掉个个
        for (int i = 0; i < 4; i++) {
            refs[i] = newfs[newfs.length - 1 - i];
        }
        System.arraycopy(refs, 0, dexBytes, 32, 4);//修改（32-35）
    }

}
