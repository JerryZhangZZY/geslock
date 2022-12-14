package com.example.geslock.tools;

import android.util.Base64;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MyAES {
    public static final int SECRET_KEY_LENGTH = 16;
    private static final Charset CHARSET_UTF8 = StandardCharsets.UTF_8;
    private static final String DEFAULT_VALUE = "0";
    private static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

    public static File encryptFile(File sourceFile, String dir, String toFileName, String secretKey) {
        try {
            File encryptFile = new File(dir, toFileName);
            FileOutputStream outputStream = new FileOutputStream(encryptFile);
            Cipher cipher = initFileAESCipher(secretKey, Cipher.ENCRYPT_MODE);
            CipherInputStream cipherInputStream = new CipherInputStream(new FileInputStream(sourceFile), cipher);
            byte[] buffer = new byte[1024 * 2];
            int len;
            while ((len = cipherInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
                outputStream.flush();
            }
            cipherInputStream.close();
            closeStream(outputStream);
            return encryptFile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File decryptFile(File sourceFile, String dir, String toFileName, String secretKey) {
        try {
            File decryptFile = new File(dir, toFileName);
            Cipher cipher = initFileAESCipher(secretKey, Cipher.DECRYPT_MODE);
            FileInputStream inputStream = new FileInputStream(sourceFile);
            CipherOutputStream cipherOutputStream = new CipherOutputStream(new FileOutputStream(decryptFile), cipher);
            byte[] buffer = new byte[1024 * 2];
            int len;
            while ((len = inputStream.read(buffer)) >= 0) {
                cipherOutputStream.write(buffer, 0, len);
                cipherOutputStream.flush();
            }
            cipherOutputStream.close();
            closeStream(inputStream);
            return decryptFile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encrypt(String data, String secretKey) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(secretKey));
            byte[] encryptByte = cipher.doFinal(data.getBytes(CHARSET_UTF8));
            return base64Encode(encryptByte);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String base64Data, String secretKey) {
        try {
            byte[] data = base64Decode(base64Data);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(secretKey));
            byte[] result = cipher.doFinal(data);
            return new String(result, CHARSET_UTF8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SecretKeySpec getSecretKey(String secretKey) {
        secretKey = toMakeKey(secretKey, SECRET_KEY_LENGTH, DEFAULT_VALUE);
        return new SecretKeySpec(secretKey.getBytes(CHARSET_UTF8), "AES");
    }

    private static String toMakeKey(String secretKey, int length, String text) {
        int strLen = secretKey.length();
        if (strLen < length) {
            StringBuilder builder = new StringBuilder();
            builder.append(secretKey);
            for (int i = 0; i < length - strLen; i++) {
                builder.append(text);
            }
            secretKey = builder.toString();
        }
        return secretKey;
    }

    public static byte[] base64Decode(String data) {
        return Base64.decode(data, Base64.NO_WRAP);
    }

    public static String base64Encode(byte[] data) {
        return Base64.encodeToString(data, Base64.NO_WRAP);
    }

    private static Cipher initFileAESCipher(String secretKey, int cipherMode) {
        try {
            SecretKeySpec secretKeySpec = getSecretKey(secretKey);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(cipherMode, secretKeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
            return cipher;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void closeStream(Closeable closeable) {
        try {
            if (closeable != null) closeable.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
