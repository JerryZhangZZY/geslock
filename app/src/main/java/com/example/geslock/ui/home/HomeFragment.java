package com.example.geslock.ui.home;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geslock.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    RecyclerView recyclerFileList;
    Button btnBack;
    File currentParent;
    File[] currentFiles;

    String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    MyAdapter myAdapter;
    List<File> myList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        assert activity != null;

        recyclerFileList = activity.findViewById(R.id.recyclerFileList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        recyclerFileList.setLayoutManager(linearLayoutManager);
//        recyclerFileList.setItemAnimator(new DefaultItemAnimator());

        btnBack = activity.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> {
            handleBack();
        });


        myAdapter = new MyAdapter(activity, myList);
        myAdapter.setOnItemClickListener((view, position) -> {
            if (currentFiles[position].isFile()) return;
            File[] tmp = currentFiles[position].listFiles();
            if (tmp == null || tmp.length == 0) {
                Toast.makeText(activity, "no files", Toast.LENGTH_SHORT).show();
            } else {
                currentParent = currentFiles[position];
                currentFiles = tmp;

                myList.clear();
                //数组与list的转换
                Collections.addAll(myList, currentFiles);
                myAdapter.notifyDataSetChanged();
            }
        });
        //给RecyclerView设置adapter
        recyclerFileList.setAdapter(myAdapter);

        requestPermissions(permissions, 1);

    }

    @Override
    public void onResume() {
        super.onResume();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener((view, i, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_BACK) {
                handleBack();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            Log.d("a", "" + grantResults[0]);
            if (grantResults != null && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                File root = Environment.getExternalStorageDirectory();
                if (root.exists()) {
                    currentParent = root;
                    currentFiles = root.listFiles();

                    Collections.addAll(myList, currentFiles);
                    myAdapter.notifyDataSetChanged();

                }
            } else {
                Toast.makeText(getActivity(), "no permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void handleBack() {
        if (!Environment.getExternalStorageDirectory().equals(currentParent)) {
            currentParent = currentParent.getParentFile();
            currentFiles = currentParent.listFiles();

            myList.clear();
            Collections.addAll(myList, currentFiles);
            myAdapter.notifyDataSetChanged();
        }
    }
}
