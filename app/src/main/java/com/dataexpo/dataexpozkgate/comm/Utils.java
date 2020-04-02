package com.dataexpo.dataexpozkgate.comm;

import android.text.TextUtils;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Utils {
    public static final int INPUT_SUCCESS = 0;
    public static final int INPUT_HAVE_NET_ADDRESS = 1;
    public static final int INPUT_ONLY_NUM = 2;
    public static final int INPUT_CHECK_NET_ADDRESS = 3;
    public static final int INPUT_NULL = 4;
    public static final int INPUT_NO_CHECK = 99;

    private static byte[] AESIV = ByteBuffer.allocate(16).array();
    private static String algorithm = "AES";
    private static String transformation = "AES/CBC/PKCS5PADDING";
    private static byte[] AESKEY;

    static {
        try {
            AESKEY = "345f266b93ee2072".getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 时间格式转化
     * @param timeStamp
     * @param pattern
     * @return
     */
    public static String formatTime(long timeStamp, String pattern) {
        Date date = new Date(timeStamp);
        DateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(date);
    }

    public static int checkInput(String input) {
        return checkInput(input, INPUT_CHECK_NET_ADDRESS);
    }

    public static int checkInput(String input, int target) {
        if (TextUtils.isEmpty(input)) {
            return INPUT_NULL;
        }

        if (input.contains("http")) {
            return INPUT_HAVE_NET_ADDRESS;
        }

        if (INPUT_ONLY_NUM == target) {

        }
        //TODO: check int or order code
        return INPUT_SUCCESS;
    }

    /**
     * aes-128-cbc  加密
     * @param value
     * @return
     */
    public static String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(AESIV);
            SecretKeySpec skeySpec = new SecretKeySpec(AESKEY, algorithm);

            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            //System.out.println("encrypted string: " + Base64.encodeToString(encrypted, Base64.DEFAULT));

            return Base64.encodeToString(encrypted, Base64.NO_WRAP);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * aes-128-cbc  解密
     * @param encrypted
     * @return
     */
    public static String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(AESIV);
            SecretKeySpec skeySpec = new SecretKeySpec(AESKEY, algorithm);

            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.decode(encrypted.getBytes(), Base64.NO_WRAP));

            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
