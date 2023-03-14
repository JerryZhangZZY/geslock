package com.example.geslock.tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Base64;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.FileProvider;

import com.example.geslock.R;
import com.example.geslock.ui.home.RockerDialog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

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
    private static final int BUFFER_LENGTH = 2048;
    // set CHECK string
    // CHECK length must not exceed 16
    private static final byte[] CHECK = "[CHECK]".getBytes();

    /**
     * Encrypt a file from an uri and export to the given path.
     *
     * @param srcUri   the file uri to be encrypted
     * @param destPath file exporting path
     * @param key      AES secret key
     * @param activity activity
     */
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
            byte[] buffer = new byte[BUFFER_LENGTH];
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

    /**
     * An async task that tries to encrypt and open a file
     */
    public static class DecryptTask extends AsyncTask<Void, Integer, File> {
        @SuppressLint("StaticFieldLeak")
        private final Activity activity;
        private final File sourceFile;
        private final String destPath;
        private final String key;
        private ProgressDialog progressDialog;
        private final RockerDialog decryptionDialog;
        private boolean done = false;

        /**
         * @param activity activity
         * @param sourceFile the file to be decrypted
         * @param destPath file exporting path
         * @param key AES secret key
         * @param decryptionDialog decryption dialog
         */
        public DecryptTask(Activity activity, File sourceFile, String destPath, String key, RockerDialog decryptionDialog) {
            this.activity = activity;
            this.sourceFile = sourceFile;
            this.destPath = destPath;
            this.key = key;
            this.decryptionDialog = decryptionDialog;
        }

        /**
         * @param activity activity
         * @param sourceFile the file to be decrypted
         * @param destPath file exporting path
         * @param key AES secret key
         */
        public DecryptTask(Activity activity, File sourceFile, String destPath, String key) {
            this.activity = activity;
            this.sourceFile = sourceFile;
            this.destPath = destPath;
            this.key = key;
            this.decryptionDialog = null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(activity);
            progressDialog.setProgressDrawable(AppCompatResources.getDrawable(activity, R.drawable.round_progressbar));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.setTitle(activity.getString(R.string.progress_decryption));
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(1000);
            progressDialog.setProgressNumberFormat(null);
            progressDialog.setProgress(0);
            progressDialog.getWindow().setBackgroundDrawableResource(R.drawable.general_dialog_background);
            try {
                progressDialog.setIcon(activity.getPackageManager().getApplicationIcon("com.example.geslock"));
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
            // hide progress bar until 200ms
            new Handler().postDelayed(() -> {
                if (!done) {
                    progressDialog.show();
                }
            }, 200);
        }

        @Override
        protected File doInBackground(Void... voids) {
            File file = new File(destPath);
            if (!deleteFile(file)) return null;
            try {
                FileInputStream inputStream = new FileInputStream(sourceFile);
                Cipher cipher = initFileAESCipher(key, Cipher.DECRYPT_MODE);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                CipherOutputStream cipherOutputStream = new CipherOutputStream(byteArrayOutputStream, cipher);
                FileOutputStream fileOutputStream = new FileOutputStream(destPath);
                int len;
                assert cipher != null;
                byte[] firstBlockBuffer = new byte[cipher.getBlockSize()];
                byte[] buffer = new byte[BUFFER_LENGTH];
                boolean checked = false;

                // decrypted the first block which the check included
                len = inputStream.read(firstBlockBuffer);
                if (len >= 0) {
                    cipherOutputStream.write(firstBlockBuffer, 0, len);
                } else {
                    return null;
                }

                long fileSize = sourceFile.length();
                long bytesRead = cipher.getBlockSize();
                int progress;

                // continue decryption with conventional buffer size
                while ((len = inputStream.read(buffer)) >= 0) {
                    cipherOutputStream.write(buffer, 0, len);
                    // check password correctness
                    if (!checkPassword(checked, byteArrayOutputStream)) {
                        if (!deleteFile(file)) return null;
                        return null;
                    } else {
                        checked = true;
                    }
                    // write file from byte array stream and clear that byte array stream
                    byteArrayOutputStream.writeTo(fileOutputStream);
                    byteArrayOutputStream.reset();

                    // calculate progress
                    bytesRead += BUFFER_LENGTH;
                    progress = (int) (bytesRead * 1000 / fileSize);
                    publishProgress(progress);
                }
                // finish cipher stream
                cipherOutputStream.flush();
                cipherOutputStream.close();
                // handle remained data
                if (!checkPassword(checked, byteArrayOutputStream)) {
                    if (!deleteFile(file)) return null;
                    return null;
                }
                byteArrayOutputStream.writeTo(fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
                closeStream(inputStream);
                return file;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(File plainFile) {
            super.onPostExecute(plainFile);
            done = true;
            progressDialog.dismiss();
            if (plainFile == null) {
                if (decryptionDialog != null) {
                    decryptionDialog.handleWrongPassword();
                } else {
                    MyToastMaker.make("Error", activity);
                }
            } else {
                // share the decrypted file with a third-party app
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri;
                uri = FileProvider.getUriForFile(activity, "com.example.geslock.fileprovider", plainFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(uri);
                try {
                    activity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    intent.setType("*/*");
                    activity.startActivity(intent);
                }
                if (decryptionDialog != null) {
                    decryptionDialog.dismiss();
                }
            }
        }
    }

    public static String encryptString(String data, String secretKey) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(secretKey));
            byte[] encryptByte = cipher.doFinal(data.getBytes(CHARSET_UTF8));
            return base64Encode(encryptByte);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    public static String decryptString(String base64Data, String secretKey) {
        try {
            byte[] data = base64Decode(base64Data);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(secretKey));
            byte[] result = cipher.doFinal(data);
            return new String(result, CHARSET_UTF8);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    public static String base64Encode(byte[] data) {
        // replace '/' with '-' to prevent invalid folder name
        return Base64.encodeToString(data, Base64.NO_WRAP).replace('/', '-');
    }

    public static byte[] base64Decode(String data) {
        // recover '/' from '-'
        return Base64.decode(data.replace('-', '/'), Base64.NO_WRAP);
    }

    private static void handleException(Exception e) {
        e.printStackTrace();
    }

    /**
     * Delete the given file.
     *
     * @param file the file to be deleted
     * @return result
     */
    public static boolean deleteFile(File file) {
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }

    /**
     * Verify the CHECK in the first aes block to determine if the password is correct.
     *
     * @param checked               check status
     * @param byteArrayOutputStream a byte array output stream contains the first decrypted AES block
     * @return result
     */
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

    /**
     * Extract a secret kry from a string.
     *
     * @param secretKey secret key in the form of string
     * @return AES secret key in the form of SecretKeySpec
     */
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
