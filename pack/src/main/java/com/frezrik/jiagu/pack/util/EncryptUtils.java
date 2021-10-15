package com.frezrik.jiagu.pack.util;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptUtils {

    /**
     * 密钥, 128位
     */
    public static final String DEFAULT_SECRET_KEY = "bajk3b4j3bvuoa3h";

    private static final String AES = "AES";
    /**
     * 初始向量IV, 初始向量IV的长度规定为128位16个字节, 初始向量的来源为随机生成.
     */
    private static final byte[] KEY_VI = "mers46ha35ga23hn".getBytes();

    /**
     * 加密解密算法/加密模式/填充方式
     */
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

    private static Cipher getEncryptCipher(int model) throws Exception {
        SecretKey key = new SecretKeySpec(DEFAULT_SECRET_KEY.getBytes(), AES);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(model, key, new IvParameterSpec(KEY_VI));
        return cipher;
    }

    /**
     * AES加密
     *
     * @param data
     * @param len 加密制定长度的数据
     * @return
     * @throws Exception
     */
    public static byte[] encrypt(byte[] data, int len) throws Exception {
        Cipher cipher = getEncryptCipher(Cipher.ENCRYPT_MODE);
        byte[] decrypt = cipher.doFinal(Arrays.copyOf(data, len));
        //Log.d("decrypt len:" + decrypt.length);
        byte[] temp = new byte[decrypt.length + data.length - len];
        System.arraycopy(decrypt, 0, temp, 0, decrypt.length);
        System.arraycopy(data, len, temp, decrypt.length, data.length - len);
        return temp;
    }

    /**
     * dex头(也就是前112位进行异或)
     * @param data
     * @return
     */
    public static byte[] encryptXor(byte[] data) {
        for (int i = 0; i < 112; i++) {
            data[i] ^= 0x66;
        }

        return data;
    }
}
