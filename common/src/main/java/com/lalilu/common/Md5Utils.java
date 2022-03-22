package com.lalilu.common;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Utils {

    public static String getMd5(String text) throws NoSuchAlgorithmException {
        //获取文件的byte信息
        byte[] textBytes = text.getBytes();

        return getMd5(textBytes);
    }

    public static String getMd5(byte[] bytes) throws NoSuchAlgorithmException {
        // 拿到一个MD5转换器
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] digest = md5.digest(bytes);
        //转换为16进制
        return new BigInteger(1, digest).toString(16);
    }
}
