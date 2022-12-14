package com.example.geslock.ui.decryption;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.geslock.R;
import com.example.geslock.tools.MyAES;

public class DecryptionFragment extends Fragment {

    private EditText etPlain;
    private EditText etCipher;
    private EditText etData;
    private EditText etResult;

    private Button btnEncrypt;
    private Button btnDecrypt;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_decryption, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();

        assert activity != null;
        etPlain = activity.findViewById(R.id.etPlain);
        etCipher = activity.findViewById(R.id.etCipher);
        etData = activity.findViewById(R.id.etData);
        etResult = activity.findViewById(R.id.etResult);
        btnEncrypt = activity.findViewById(R.id.btnEncrypt);
        btnDecrypt = activity.findViewById(R.id.btnDecrypt);

        btnEncrypt.setOnClickListener(view -> {
            String plain = String.valueOf(etPlain.getText());
            String key = String.valueOf(etCipher.getText());
            String data = MyAES.encrypt(plain, key);
            etData.setText(data);
        });

        btnDecrypt.setOnClickListener(view -> {
            String data = String.valueOf(etData.getText());
            String key = String.valueOf(etCipher.getText());
            String result = MyAES.decrypt(data, key);
            etResult.setText(result);
        });
    }
}