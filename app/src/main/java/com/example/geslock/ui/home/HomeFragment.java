package com.example.geslock.ui.home;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geslock.R;
import com.example.geslock.tools.MyAnimationScaler;
import com.example.geslock.tools.MyToastMaker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
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

    private int yellow_500;
    private int red_500;
    private int gray_500;

    private Animation animRotateOpen;
    private Animation animRotateClose;
    private Animation animFromBottom;
    private Animation animToBottom;
    private Animation animItemFallDown;
    private LayoutAnimationController animRecyclerLayout;
    private int ANIM_DURATION_100;
    private int ANIM_DURATION_200;

    private boolean addClicked = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @SuppressLint({"NotifyDataSetChanged", "ClickableViewAccessibility", "ResourceType"})
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

        ANIM_DURATION_100 = MyAnimationScaler.getDuration(100, activity);
        ANIM_DURATION_200 = MyAnimationScaler.getDuration(200, activity);
        animRotateOpen = AnimationUtils.loadAnimation(activity, R.anim.rotate_open_anim);
        animRotateOpen.setDuration(ANIM_DURATION_100);
        animRotateClose = AnimationUtils.loadAnimation(activity, R.anim.rotate_close_anim);
        animRotateClose.setDuration(ANIM_DURATION_100);
        animFromBottom = AnimationUtils.loadAnimation(activity, R.anim.from_bottom_anim);
        animFromBottom.setDuration(ANIM_DURATION_100);
        animToBottom = AnimationUtils.loadAnimation(activity, R.anim.to_bottom_anim);
        animToBottom.setDuration(ANIM_DURATION_100);
        animItemFallDown = AnimationUtils.loadAnimation(activity, R.anim.item_fall_down_anim);
        animItemFallDown.setDuration(ANIM_DURATION_200);
        animRecyclerLayout = new LayoutAnimationController(animItemFallDown, 0.15F);

        recyclerFileList = activity.findViewById(R.id.recyclerFileList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        recyclerFileList.setLayoutManager(linearLayoutManager);
        recyclerFileList.setLayoutAnimation(animRecyclerLayout);

        tvPath = activity.findViewById(R.id.tvPath);

        btnBack = activity.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> {
            handleBack();
        });

        myAdapter = new MyAdapter(activity, myList);
        myAdapter.setOnItemClickListener((view, position) -> {
            if (currentFiles[position].isFile()) {
                new RockerDialog(activity);

            } else {
                File[] files = currentFiles[position].listFiles();
                currentParent = currentFiles[position];
                currentFiles = files;
                refresh();
            }
        });
        myAdapter.setOnItemLongClickListener(((view, position) -> {
            dialogEdit(position, activity);
        }));
        recyclerFileList.setAdapter(myAdapter);

        requestPermissions(permissions, 1);

        fabAdd = activity.findViewById(R.id.fabAdd);
        fabAddFile = activity.findViewById(R.id.fabAddFile);
        fabAddFolder = activity.findViewById(R.id.fabAddFolder);
        tvNewFile = activity.findViewById(R.id.tvNewFile);
        tvNewFolder = activity.findViewById(R.id.tvNewFolder);

        fabAdd.setOnClickListener(view -> {
            switchFabs();
        });

        fabAddFile.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, ""), 1);
            switchFabs();
        });

        fabAddFolder.setOnClickListener(view -> {
            dialogNewFolder(activity);
            switchFabs();
        });

        imgEmpty = activity.findViewById(R.id.imgEmpty);
        tvEmpty = activity.findViewById(R.id.tvEmpty);

        yellow_500 = activity.getColor(R.color.yellow_500);
        red_500 = activity.getColor(R.color.red_500);
        gray_500 = activity.getColor(R.color.gray_500);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(resultCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            String name = DocumentFile.fromSingleUri(activity, uri).getName();
            copyFile(uri, currentParent.getPath() + "/" + name + "gl");
            currentFiles = currentParent.listFiles();
            refresh();
        }
    }

    public void copyFile(Uri uri, String newPath) {
        try {
            int byteRead;
            InputStream inStream = activity.getContentResolver().openInputStream(uri);
            FileOutputStream fs = new FileOutputStream(newPath);
            byte[] buffer = new byte[1444];
            while ((byteRead = inStream.read(buffer)) != -1) {
                fs.write(buffer, 0, byteRead);
            }
            inStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setIcon(R.drawable.ic_folder)
                .setTitle(R.string.new_folder)
                .setView(editText)
                .setNegativeButton(R.string.cancel, (dialog0, which) -> dialog0.dismiss())
                .setPositiveButton(R.string.ok, (dialog0, which) -> {
                    String folderName = editText.getText().toString();
                    newFolder(folderName);
                    currentFiles = currentParent.listFiles();
                    refresh();
                    dialog0.dismiss();
                }).create();
        setDialogBackground(dialog);
        dialog.show();
        Button btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {
                    btnPositive.setTextColor(gray_500);
                    btnPositive.setClickable(false);
                } else {
                    btnPositive.setTextColor(yellow_500);
                    btnPositive.setClickable(true);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        btnPositive.setTextColor(gray_500);
        btnPositive.setClickable(false);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(yellow_500);
    }

    /**
     * The dialog of editing a file/folder
     * @param position position of the file/folder to be edited
     * @param activity current activity
     */
    public void dialogEdit(int position, Activity activity) {
        final String[] options = {(String) activity.getText(R.string.edit_file_rename),(String) activity.getText(R.string.edit_file_delete)};
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setIcon(getItemIcon(position))
                .setTitle(getItemName(position))
                .setItems(options, (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            dialogRename(position, activity);
                            break;
                        case 1:
                            dialogDeleteConfirm(position, activity);
                            break;
                    }
                }).create();
        setDialogBackground(dialog);
        dialog.show();
    }

    /**
     * The dialog of renaming a file/folder.
     * @param position position of the file/folder to be renamed
     * @param activity current activity
     */
    public void dialogRename(int position, Activity activity) {
        String currentName = getItemName(position);
        File file = currentFiles[position];
        final EditText editText = new EditText(activity);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setHint(R.string.rename_hint);
        editText.setPadding(70, 30, 70, 30);
        // auto fill current name with extension
        editText.setText(currentName);
        // auto select file name without extension
        editText.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                editText.post(() -> {
                    editText.setSelection(0, file.isFile() ? currentName.lastIndexOf(".") : currentName.length());
                    editText.setCursorVisible(true);
                });
            } else {
                editText.setCursorVisible(false);
            }
        });
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setIcon(getItemIcon(position))
                .setTitle(currentName)
                .setView(editText)
                .setNegativeButton(R.string.cancel, (dialog0, which) -> dialog0.dismiss())
                .setPositiveButton(R.string.rename_ok, (dialog0, which) -> {
                    String newName = editText.getText().toString() + (file.isFile() ? "gl" : "");
                    rename(file, newName);
                    currentFiles = currentParent.listFiles();
                    refresh();
                    dialog0.dismiss();
                }).create();
        setDialogBackground(dialog);
        dialog.show();
        // prevent null or same name
        Button btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String newName = charSequence.toString();
                if (newName.isEmpty() || newName.equals(currentName)) {
                    btnPositive.setTextColor(gray_500);
                    btnPositive.setClickable(false);
                } else {
                    btnPositive.setTextColor(yellow_500);
                    btnPositive.setClickable(true);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        btnPositive.setTextColor(gray_500);
        btnPositive.setClickable(false);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(yellow_500);
    }

    /**
     * The dialog of confirming a deletion of a file/folder.
     * @param position position of the file/folder to be renamed
     * @param activity current activity
     */
    public void dialogDeleteConfirm(int position, Activity activity) {
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setIcon(getItemIcon(position))
                .setTitle(getItemName(position))
                .setNegativeButton(R.string.cancel, (dialog0, which) -> dialog0.dismiss())
                .setPositiveButton(R.string.delete_ok, (dialog0, which) -> {
                    deleteFile(currentFiles[position]);
                    currentFiles = currentParent.listFiles();
                    refresh();
                    dialog0.dismiss();
                }).create();
        setDialogBackground(dialog);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(red_500);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(yellow_500);
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
            imgEmpty.startAnimation(animItemFallDown);
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.startAnimation(animItemFallDown);
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
     * Delete a file/folder.
     * @param dirFile file/folder to be deleted
     */
    public void deleteFile(File dirFile) {
        // 如果dir对应的文件不存在，则退出
        if (!dirFile.exists()) {
            return;
        }
        if (dirFile.isFile()) {
            dirFile.delete();
            return;
        } else {
            for (File file : Objects.requireNonNull(dirFile.listFiles())) {
                deleteFile(file);
            }
        }
        dirFile.delete();
    }

    /**
     * Rename a file/folder.
     * @param file file/folder to be renamed
     * @param newName new name
     */
    public void rename(File file, String newName) {
        file.renameTo(new File(file.getParent() + "/" + newName));
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
            fabAdd.startAnimation(animRotateOpen);
            fabAddFile.startAnimation(animFromBottom);
            fabAddFolder.startAnimation(animFromBottom);
            tvNewFile.startAnimation(animFromBottom);
            tvNewFolder.startAnimation(animFromBottom);
        } else {
            fabAdd.startAnimation(animRotateClose);
            fabAddFile.startAnimation(animToBottom);
            fabAddFolder.startAnimation(animToBottom);
            tvNewFile.startAnimation(animToBottom);
            tvNewFolder.startAnimation(animToBottom);
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

    public String getItemName(int position) {
        View viewItem = Objects.requireNonNull(recyclerFileList.getLayoutManager()).findViewByPosition(position);
        assert viewItem != null;
        return (String) ((TextView) viewItem.findViewById(R.id.tvFileName)).getText();
    }

    public Drawable getItemIcon(int position) {
        View viewItem = Objects.requireNonNull(recyclerFileList.getLayoutManager()).findViewByPosition(position);
        assert viewItem != null;
        return ((ImageView) viewItem.findViewById(R.id.imgFileIcon)).getDrawable();
    }

    public void setDialogBackground(AlertDialog dialog) {
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.alert_dialog_background);
    }
}
