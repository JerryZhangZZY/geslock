package com.example.geslock.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geslock.R;
import com.example.geslock.tools.MyToastMaker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private Activity activity;

    private String title;
    private File root;
    private File currentParent;
    private File[] currentFiles;

    private final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private MyAdapter myAdapter;
    private final List<File> myList = new ArrayList<>();

    private Button btnBack;
    private TextView tvPath;
    private RecyclerView recyclerFileList;
    private FloatingActionButton fabAdd;
    private FloatingActionButton fabAddFile;
    private FloatingActionButton fabAddFolder;
    private TextView tvNewFile;
    private TextView tvNewFolder;
    private ImageView imgEmpty;
    private TextView tvEmpty;

    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation fromBottom;
    private Animation toBottom;
    private Animation itemFallDown;

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

        title = (String) activity.getText(R.string.title_home);

        root = new File(getActivity().getExternalCacheDir().toString() + "/root");
        if (!root.exists()) {
            root.mkdir();
        }

        recyclerFileList = activity.findViewById(R.id.recyclerFileList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        recyclerFileList.setLayoutManager(linearLayoutManager);
        recyclerFileList.setItemAnimator(new DefaultItemAnimator());

        tvPath = activity.findViewById(R.id.tvPath);

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
            }
        });

        fabAddFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogNewFolder(activity);
                switchFabs();
            }
        });

        imgEmpty = activity.findViewById(R.id.imgEmpty);
        tvEmpty = activity.findViewById(R.id.tvEmpty);

        itemFallDown = AnimationUtils.loadAnimation(activity, R.anim.item_fall_down_anim);
    }

    @Override
    public void onResume() {
        super.onResume();
        requireView().setFocusableInTouchMode(true);
        requireView().requestFocus();
        requireView().setOnKeyListener((view, i, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_BACK) {
                handleBack();
                return true;
            }
            return false;
        });
    }

    /**
     * Init recycler view after permission granted.
     * @param requestCode request code
     * @param permissions permissions
     * @param grantResults grant results
     */
    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (root.exists()) {
                    currentParent = root;
                    currentFiles = root.listFiles();
                    assert currentFiles != null;
                    refresh();
                }
            } else {
                MyToastMaker.make((String) activity.getText(R.string.no_permission), activity);
            }
        }
    }

    /**
     * The dialog of creating new folder.
     * @param activity current activity
     */
    public void dialogNewFolder(Activity activity) {
        final EditText editText = new EditText(activity);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setHint(R.string.new_folder_hint);
        editText.setPadding(70, 30, 70, 30);
        int btnColor = activity.getColor(R.color.yellow_500);
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
        Button btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {
                    btnPositive.setTextColor(activity.getColor(R.color.gray_500));
                    btnPositive.setClickable(false);
                } else {
                    btnPositive.setTextColor(btnColor);
                    btnPositive.setClickable(true);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        btnPositive.setTextColor(activity.getColor(R.color.gray_500));
        btnPositive.setClickable(false);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(btnColor);
    }

    /**
     * Refresh path bar, recycler view and empty sign.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void refresh() {
        // refresh path bar
        if (root.equals(currentParent)) {
            btnBack.setVisibility(View.GONE);
            tvPath.setText(title);
        } else {
            btnBack.setVisibility(View.VISIBLE);
            File parent = currentParent.getParentFile();
            if (root.equals(parent)) {
                btnBack.setText(title);
            } else {
                assert parent != null;
                btnBack.setText(parent.getName());
            }

            tvPath.setText(currentParent.getName());
        }

        // refresh recycler view
        currentFiles = sortFiles(currentFiles);
        myList.clear();
        Collections.addAll(myList, currentFiles);
        myAdapter.notifyDataSetChanged();
        recyclerFileList.scheduleLayoutAnimation();

        // refresh empty sign
        if (currentFiles.length == 0) {
            imgEmpty.setVisibility(View.VISIBLE);
            imgEmpty.startAnimation(itemFallDown);
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.startAnimation(itemFallDown);
        } else {
            imgEmpty.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    /**
     * Create a new folder under the current path.
     * @param name name of the folder to be created
     */
    public void newFolder(String name) {
        File file = new File(currentParent.getPath() + "/" + name);
        if (file.exists()) {
            MyToastMaker.make((String) activity.getText(R.string.new_folder_exists), activity);
            return;
        }
        file.mkdir();
    }

    /**
     * Priority: folder > file.
     * @param currentFiles files to be sorted
     * @return sorted files
     */
    private File[] sortFiles(File[] currentFiles) {
        ArrayList<File> folders = new ArrayList<>();
        ArrayList<File> files = new ArrayList<>();
        for (File file : currentFiles) {
            if (file.isFile()) {
                folders.add(file);
            } else {
                files.add(file);
            }
        }
        folders.sort(Comparator.naturalOrder());
        files.sort(Comparator.naturalOrder());
        files.addAll(folders);
        return files.toArray(new File[0]);
    }

    /**
     * Actions after back operation is triggered.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void handleBack() {
        if (!root.equals(currentParent)) {
            currentParent = currentParent.getParentFile();
            assert currentParent != null;
            currentFiles = currentParent.listFiles();
            refresh();
        }
    }

    /**
     * Switch floating action buttons' expansion status.
     */
    public void switchFabs() {
        setVisibility();
        setAnimation();
        setClickable();
        addClicked = !addClicked;
    }

    /**
     * Switch floating action buttons' visibility status.
     */
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

    /**
     * Play floating action buttons' animations.
     */
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

    /**
     * Switch floating action buttons' clickable status.
     */
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
