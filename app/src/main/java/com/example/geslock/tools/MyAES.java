package com.example.geslock.tools;

import android.app.Activity;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MyAES {
    public static final int SECRET_KEY_LENGTH = 16;
    private static final Charset CHARSET_UTF8 = StandardCharsets.UTF_8;
    private static final String DEFAULT_VALUE = "0";
    private static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
    // CHECK length must not exceed 16
    private static final byte[] CHECK = "[CHECK]".getBytes();

    public static void encryptFile(Uri srcUri, String destPath, String key, Activity activity) {
        if (new File(destPath).exists()) {
            return;
        }
        try {
            Cipher cipher = initFileAESCipher(key, Cipher.ENCRYPT_MODE);
            InputStream dataStream = activity.getContentResolver().openInputStream(srcUri);
            InputStream checkStream = new ByteArrayInputStream(CHECK);
            SequenceInputStream sequenceInputStream = new SequenceInputStream(checkStream, dataStream);
            CipherInputStream cipherInputStream = new CipherInputStream(sequenceInputStream, cipher);
            FileOutputStream outputStream = new FileOutputStream(destPath);
            byte[] buffer = new byte[2048];
            int len;
            while ((len = cipherInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
            cipherInputStream.close();
            closeStream(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean decryptFile(File sourceFile, String destPath, String key) {
        File file = new File(destPath);
        if (file.exists()) {
            if (!file.delete()) {
                return false;
            }
        }
        try {
            FileInputStream inputStream = new FileInputStream(sourceFile);
            Cipher cipher = initFileAESCipher(key, Cipher.DECRYPT_MODE);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(byteArrayOutputStream , cipher);
            FileOutputStream fileOutputStream = new FileOutputStream(destPath);
            int len;
            assert cipher != null;
            byte[] firstBlockBuffer = new byte[cipher.getBlockSize()];
            byte[] buffer = new byte[2048];
            boolean checked = false;

            // decrypted the first block which the check included
            len = inputStream.read(firstBlockBuffer);
            if (len >= 0) {
                cipherOutputStream.write(firstBlockBuffer, 0, len);
            } else {
                return false;
            }

            // continue decryption with conventional buffer size
            while ((len = inputStream.read(buffer)) >= 0) {
                cipherOutputStream.write(buffer, 0, len);
                // check password correctness
                if (!checkPassword(checked, byteArrayOutputStream)) {
                    return false;
                } else {
                    checked = true;
                }
                // write file from byte array stream and clear that byte array stream
                byteArrayOutputStream.writeTo(fileOutputStream);
                byteArrayOutputStream.reset();
            }
            // finish cipher stream
            cipherOutputStream.flush();
            cipherOutputStream.close();
            // handle remained data
            if (!checkPassword(checked, byteArrayOutputStream)) {
                return false;
            }
            byteArrayOutputStream.writeTo(fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            closeStream(inputStream);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean checkPassword(boolean checked, ByteArrayOutputStream byteArrayOutputStream) {
        if (!checked) {
            byte[] firstBlock = byteArrayOutputStream.toByteArray();
            int checkLength = CHECK.length;
            for (int i = 0; i < checkLength; i++) {
                if (firstBlock[i] != CHECK[i]) {
                    // wrong password
                    return false;
                }
            }
            // remove the check from byte array stream
            byteArrayOutputStream.reset();
            byteArrayOutputStream.write(firstBlock, checkLength, firstBlock.length - checkLength);
        }
        return true;
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
