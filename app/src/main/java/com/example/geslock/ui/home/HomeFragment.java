package com.example.geslock.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.GridLayoutAnimationController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geslock.MainActivity;
import com.example.geslock.R;
import com.example.geslock.tools.MyToastMaker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private Activity activity;

    private File root;
    private RecyclerView recyclerFileList;
    private Button btnBack;
    private File currentParent;
    private File[] currentFiles;

    private final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private MyAdapter myAdapter;
    private final List<File> myList = new ArrayList<>();

    private FloatingActionButton fabAdd;
    private FloatingActionButton fabAddFile;
    private FloatingActionButton fabAddFolder;
    private TextView tvNewFile;
    private TextView tvNewFolder;

    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation fromBottom;
    private Animation toBottom;

    private boolean addClicked = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @SuppressLint({"NotifyDataSetChanged", "ClickableViewAccessibility"})
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = getActivity();
        assert activity != null;

        root = new File(getActivity().getExternalCacheDir().toString() + "/safe");
        if (!root.exists()) {
            root.mkdir();
        }

        recyclerFileList = activity.findViewById(R.id.recyclerFileList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        recyclerFileList.setLayoutManager(linearLayoutManager);
        recyclerFileList.setItemAnimator(new DefaultItemAnimator());

        btnBack = activity.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> {
            handleBack();
        });

        myAdapter = new MyAdapter(activity, myList);
        myAdapter.setOnItemClickListener((view, position) -> {
            if (currentFiles[position].isFile()) {
                MyToastMaker.make("File clicked", activity);
                return;
            }
            File[] files = currentFiles[position].listFiles();
            currentParent = currentFiles[position];
            currentFiles = files;
            refresh();
        });
        myAdapter.setOnItemLongClickListener(((view, position) -> {
            MyToastMaker.make("long", activity);
        }));
        recyclerFileList.setAdapter(myAdapter);

        requestPermissions(permissions, 1);

        fabAdd = activity.findViewById(R.id.fabAdd);
        fabAddFile = activity.findViewById(R.id.fabAddFile);
        fabAddFolder = activity.findViewById(R.id.fabAddFolder);
        tvNewFile = activity.findViewById(R.id.tvNewFile);
        tvNewFolder = activity.findViewById(R.id.tvNewFolder);

        rotateOpen = AnimationUtils.loadAnimation(activity, R.anim.rotate_open_anim);
        rotateClose = AnimationUtils.loadAnimation(activity, R.anim.rotate_close_anim);
        fromBottom = AnimationUtils.loadAnimation(activity, R.anim.from_bottom_anim);
        toBottom = AnimationUtils.loadAnimation(activity, R.anim.to_bottom_anim);

        fabAdd.setOnClickListener(view -> {
            switchFabs();
        });

        fabAddFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchFabs();
                MyToastMaker.make("file", activity);
            }
        });

        fabAddFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog(activity);
                switchFabs();
                MyToastMaker.make("folder", activity);
            }
        });
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

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            Log.d("a", "" + grantResults[0]);
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (root.exists()) {
                    currentParent = root;
                    currentFiles = root.listFiles();
                    assert currentFiles != null;
                    refresh();
                }
            } else {
                Toast.makeText(getActivity(), "no permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void dialog(Activity activity) {
        final EditText editText = new EditText(activity);
        editText.setHint(R.string.new_folder_hint);
        editText.setPadding(70, 30, 70, 30);
        AlertDialog dialog = new AlertDialog.Builder(activity)
            .setIcon(R.drawable.ic_folder)
            .setTitle(R.string.new_folder)
            .setView(editText)
            .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
            .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String folderName = editText.getText().toString();
                    newFolder(folderName);
                    dialog.dismiss();
                    currentFiles = currentParent.listFiles();
                    refresh();
                }
            }).create();
        dialog.show();
    }

    public void refresh() {
        Arrays.sort(currentFiles);
        myList.clear();
        Collections.addAll(myList, currentFiles);
        myAdapter.notifyDataSetChanged();
    }

    public void newFolder(String name) {
        File file = new File(currentParent.getPath() + "/" + name);
        if (file.exists()) {
            MyToastMaker.make((String) activity.getText(R.string.new_folder_exists), activity);
            return;
        }
        file.mkdir();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void handleBack() {
        if (!root.equals(currentParent)) {
            currentParent = currentParent.getParentFile();
            assert currentParent != null;
            currentFiles = currentParent.listFiles();
            refresh();
        }
    }

    public void switchFabs() {
        setVisibility();
        setAnimation();
        setClickable();
        addClicked = !addClicked;
    }

    public void setVisibility() {
        if (!addClicked) {
            fabAddFile.setVisibility(View.VISIBLE);
            fabAddFolder.setVisibility(View.VISIBLE);
            tvNewFile.setVisibility(View.VISIBLE);
            tvNewFolder.setVisibility(View.VISIBLE);
        } else {
            fabAddFile.setVisibility(View.GONE);
            fabAddFolder.setVisibility(View.GONE);
            tvNewFile.setVisibility(View.GONE);
            tvNewFolder.setVisibility(View.GONE);
        }
    }

    public void setAnimation() {
        if (!addClicked) {
            fabAdd.startAnimation(rotateOpen);
            fabAddFile.startAnimation(fromBottom);
            fabAddFolder.startAnimation(fromBottom);
            tvNewFile.startAnimation(fromBottom);
            tvNewFolder.startAnimation(fromBottom);
        } else {
            fabAdd.startAnimation(rotateClose);
            fabAddFile.startAnimation(toBottom);
            fabAddFolder.startAnimation(toBottom);
            tvNewFile.startAnimation(toBottom);
            tvNewFolder.startAnimation(toBottom);
        }
    }

    public void setClickable() {
        if (!addClicked) {
            fabAddFile.setClickable(true);
            fabAddFolder.setClickable(true);
        } else {
            fabAddFile.setClickable(false);
            fabAddFolder.setClickable(false);
        }
    }
}
