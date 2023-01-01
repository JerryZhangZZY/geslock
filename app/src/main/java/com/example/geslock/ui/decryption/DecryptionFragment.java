//package com.example.geslock.ui.decryption;
//
//import android.Manifest;
//import android.app.Activity;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Environment;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.geslock.ui.home.MyAdapter;
//import com.example.geslock.R;
//import com.example.geslock.tools.MyAES;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//public class DecryptionFragment extends Fragment {
//
//    private EditText etPlain;
//    private EditText etCipher;
//    private EditText etData;
//    private EditText etResult;
//
//    private Button btnEncrypt;
//    private Button btnDecrypt;
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_decryption, container, false);
//    }
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
//        Activity activity = getActivity();
//
//        assert activity != null;
//        etPlain = activity.findViewById(R.id.etPlain);
//        etCipher = activity.findViewById(R.id.etCipher);
//        etData = activity.findViewById(R.id.etData);
//        etResult = activity.findViewById(R.id.etResult);
//        btnEncrypt = activity.findViewById(R.id.btnEncrypt);
//        btnDecrypt = activity.findViewById(R.id.btnDecrypt);
//
//        btnEncrypt.setOnClickListener(view -> {
//            String plain = String.valueOf(etPlain.getText());
//            String key = String.valueOf(etCipher.getText());
//            String data = MyAES.encrypt(plain, key);
//            etData.setText(data);
//        });
//
//        btnDecrypt.setOnClickListener(view -> {
//            String data = String.valueOf(etData.getText());
//            String key = String.valueOf(etCipher.getText());
//            String result = MyAES.decrypt(data, key);
//            etResult.setText(result);
//        });
//
//
//
//
//
//
//
//
//        Button button = activity.findViewById(R.id.btnFile);
//        button.setOnClickListener(view -> {
//            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//            intent.setType("*/*");//筛选器
//            intent.addCategory(Intent.CATEGORY_OPENABLE);
//            startActivityForResult(Intent.createChooser(intent,"选择一个文件"),1);
//        });
//
//        Button button2 = activity.findViewById(R.id.btnFile2);
//        button2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                File file = new File(activity.getExternalCacheDir() + "/test.txt");
//                MyAES.encryptFile(file, activity.getExternalCacheDir().toString(), "encrypted.gl", "123");
//                file = new File(activity.getExternalCacheDir() + "/encrypted.gl");
//                MyAES.decryptFile(file, activity.getExternalCacheDir().toString(), "decrypted.txt", "124");
//            }
//        });
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        if (resultCode == Activity.RESULT_OK) {
//            try {
//                //保存读取到的内容
//                StringBuilder result = new StringBuilder();
//                //获取URI
//                Uri uri = data.getData();
//                //获取输入流
//                InputStream is = getContext().getContentResolver().openInputStream(uri);
//                //创建用于字符输入流中读取文本的bufferReader对象
//                BufferedReader br = new BufferedReader(new InputStreamReader(is));
//                String line;
//                while ((line = br.readLine()) != null) {
//                    //将读取到的内容放入结果字符串
//                    result.append(line);
//                }
//                //文件中的内容
//                Log.i("File", result.toString());
//                String content = result.toString();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//}