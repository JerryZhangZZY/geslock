package com.example.geslock.ui.home;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geslock.R;
import com.example.geslock.tools.MyAES;
import com.example.geslock.tools.MyAnimationScaler;
import com.example.geslock.tools.MyDefaultPref;
import com.example.geslock.tools.MyNameFormatter;
import com.example.geslock.tools.MyPixelConverter;
import com.example.geslock.tools.MyToastMaker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

public class HomeFragment extends Fragment {

    private Activity activity;
    private SharedPreferences pref;

    private String title;
    private File rootDir;
    private File cacheDir;
    private File currentParent;
    private File[] currentFiles;

    private final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private MyAdapter myAdapter;
    private final List<File> myList = new ArrayList<>();

    private final Stack<String> folderKeys = new Stack<>();

    private Button btnBack;
    private TextView tvPath;
    private ImageButton btnMenu;
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

        requestPermissions(permissions, 0);

        activity = getActivity();
        assert activity != null;
        pref = activity.getSharedPreferences("pref", Context.MODE_PRIVATE);

        title = (String) activity.getText(R.string.title_home);

        rootDir = activity.getExternalFilesDir("root");
        if (!rootDir.exists()) {
            rootDir.mkdir();
        }
        cacheDir = activity.getExternalCacheDir();

        folderKeys.push(null);

        // set animations
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

        // set file list
        recyclerFileList = activity.findViewById(R.id.recyclerFileList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        recyclerFileList.setLayoutManager(linearLayoutManager);
        recyclerFileList.setLayoutAnimation(animRecyclerLayout);

        tvPath = activity.findViewById(R.id.tvPath);

        // set menu
        btnMenu = activity.findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v -> {
            Context wrapper = new ContextThemeWrapper(activity, R.style.popupMenuStyle);
            PopupMenu popupMenu = new PopupMenu(wrapper, v);
            popupMenu.inflate(R.menu.home_menu);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getOrder()) {
                    case 1:
                        int[] entryIdsSort = new int[]{R.id.tvSortName, R.id.tvSortDate, R.id.tvSortSize};
                        int[] checkIdsSort = new int[]{R.id.imgSortNameCheck, R.id.imgSortDateCheck, R.id.imgSortSizeCheck};
                        int[] iconIdsSort = new int[]{R.id.imgSortNameIcon, R.id.imgSortDateIcon, R.id.imgSortSizeIcon};
                        SingleChoiceSheet sortSheet = new SingleChoiceSheet(activity, R.layout.sheet_sort, entryIdsSort, checkIdsSort, iconIdsSort, "sort");
                        sortSheet.setOnDismissListener(dialog -> {
                            if (sortSheet.changed()) {
                                sortFiles();
                                refresh();
                            }
                        });
                        sortSheet.show();
                        break;
                    case 2:
                        int[] entryIdsOrder = new int[]{R.id.tvOrderAscend, R.id.tvOrderDescend};
                        int[] checkIdsOrder = new int[]{R.id.imgOrderAscendCheck, R.id.imgOrderDescendCheck};
                        int[] iconIdsOrder = new int[]{R.id.imgOrderAscendIcon, R.id.imgOrderDescendIcon};
                        SingleChoiceSheet orderSheet = new SingleChoiceSheet(activity, R.layout.sheet_order, entryIdsOrder, checkIdsOrder, iconIdsOrder, "order");
                        orderSheet.setOnDismissListener(dialog -> {
                            if (orderSheet.changed()) {
                                sortFiles();
                                refresh();
                            }
                        });
                        orderSheet.show();
                        break;
                    case 3:
                        int[] entryIdsProperty = new int[]{R.id.tvPropertyDate, R.id.tvPropertySize, R.id.tvPropertyType, R.id.tvPropertyNull};
                        int[] checkIdsProperty = new int[]{R.id.imgPropertyDateCheck, R.id.imgPropertySizeCheck, R.id.imgPropertyTypeCheck, R.id.imgPropertyNullCheck};
                        int[] iconIdsProperty = new int[]{R.id.imgPropertyDateIcon, R.id.imgPropertySizeIcon, R.id.imgPropertyTypeIcon, R.id.imgPropertyNullIcon};
                        SingleChoiceSheet propertySheet = new SingleChoiceSheet(activity, R.layout.sheet_property, entryIdsProperty, checkIdsProperty, iconIdsProperty, "property");
                        propertySheet.setOnDismissListener(dialog -> {
                            if (propertySheet.changed()) {
                                refresh();
                            }
                        });
                        propertySheet.show();
                }
                return false;
            });

            setMenuItemIcon(popupMenu.getMenu().getItem(0), "sort", new int[]{R.drawable.ic_file_name, R.drawable.ic_file_date, R.drawable.ic_file_size});
            setMenuItemIcon(popupMenu.getMenu().getItem(1), "order", new int[]{R.drawable.ic_arrow_up, R.drawable.ic_arrow_down});
            setMenuItemIcon(popupMenu.getMenu().getItem(2), "property", new int[]{R.drawable.ic_file_date, R.drawable.ic_file_size, R.drawable.ic_file_type, R.drawable.ic_null});

            popupMenu.show();
        });

        btnBack = activity.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> handleBack());

        // set adapter
        myAdapter = new MyAdapter(activity, myList);
        myAdapter.setOnItemClickListener((view, position) -> {
            File file = currentFiles[position];
            String fileName = file.getName();
            if (file.isFile()) {
                // handle decryption when clicked on a file
                if (!cacheDir.exists()) {
                    cacheDir.mkdir();
                }
                String destPath = cacheDir.getPath() + "/" + fileName.substring(0, file.getName().length() - 2);
                String folderKey = folderKeys.peek();
                if (folderKey == null) {
                    // under plain folder
                    // set the decryption dialog
                    RockerDialog decryptionDialog = new RockerDialog(activity);
                    decryptionDialog.getBtnPositive().setOnClickListener(v -> {
                        String key = decryptionDialog.getPassword();
                        new MyAES.DecryptTask(activity, file, destPath, key, decryptionDialog).execute();
                    });
                    decryptionDialog.show();
                } else {
                    // under locked folder
                    new MyAES.DecryptTask(activity, file, destPath, folderKey).execute();
                }


            } else {
                if (!MyNameFormatter.isLockedFolder(fileName)) {
                    // plain folder
                    folderKeys.push(null);
                    // enter the clicked folder
                    handleEnter(file);
                } else {
                    // locked folder
                    RockerDialog decryptionDialog = new RockerDialog(activity);
                    decryptionDialog.getBtnPositive().setOnClickListener(v -> {
                        String password = decryptionDialog.getPassword();
                        try {
                            if (Objects.equals(MyAES.decryptString(MyNameFormatter.parseCheck(fileName), password), "[CHECK]")) {
                                folderKeys.push(password);
                                handleEnter(file);
                                decryptionDialog.dismiss();
                            } else {
                                decryptionDialog.handleWrongPassword();
                            }
                        } catch (Exception e) {
                            decryptionDialog.handleWrongPassword();
                        }

                    });
                    decryptionDialog.show();
                }

            }
        });
        // show edit dialog when long clicked
        myAdapter.setOnItemLongClickListener(((view, position) -> dialogEdit(position)));
        recyclerFileList.setAdapter(myAdapter);

        // set floating action buttons
        fabAdd = activity.findViewById(R.id.fabAdd);
        fabAddFile = activity.findViewById(R.id.fabAddFile);
        fabAddFolder = activity.findViewById(R.id.fabAddFolder);
        tvNewFile = activity.findViewById(R.id.tvNewFile);
        tvNewFolder = activity.findViewById(R.id.tvNewFolder);

        fabAdd.setOnClickListener(view -> switchFabs());

        fabAddFile.setOnClickListener(view -> {
            // open system file manager to upload a file
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, ""), 1);
            switchFabs();
        });

        fabAddFolder.setOnClickListener(view -> {
            // create a new folder
            dialogNewFolder();
            switchFabs();
        });

        // set empty folder hint
        imgEmpty = activity.findViewById(R.id.imgEmpty);
        tvEmpty = activity.findViewById(R.id.tvEmpty);

        // set colors
        yellow_500 = activity.getColor(R.color.yellow_500);
        red_500 = activity.getColor(R.color.red_500);
        gray_500 = activity.getColor(R.color.gray_500);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(resultCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            // get uploaded file
            Uri uri = data.getData();
            String name = Objects.requireNonNull(DocumentFile.fromSingleUri(activity, uri)).getName();
            String destPath = currentParent.getPath() + "/" + name + "gl";
            // handle condition of a same name
            while (new File(destPath).exists()) {
                assert name != null;
                int index = name.lastIndexOf(".");
                name = name.substring(0, index) + "_1" + name.substring(index);
                destPath = currentParent.getPath() + "/" + name + "gl";
            }
            RockerDialog encryptionDialog = new RockerDialog(activity);
            String finalDestPath = destPath;
            encryptionDialog.getBtnPositive().setOnClickListener(v -> {
                // get password from dialog
                String key = encryptionDialog.getPassword();

                class EncryptTask extends AsyncTask<Void, Void, Void> {
                    public ProgressDialog progressDialog;
                    boolean done = false;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        progressDialog = new ProgressDialog(activity, R.style.progressDialogStyle);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.setTitle(activity.getString(R.string.progress_encryption));
                        progressDialog.setMessage(activity.getString(R.string.progress_encryption_message));
                        progressDialog.setCancelable(false);
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
                    protected Void doInBackground(Void... voids) {
                        MyAES.encryptFile(uri, finalDestPath, key, activity);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void v) {
                        super.onPostExecute(v);
                        done = true;
                        progressDialog.dismiss();
                        encryptionDialog.dismiss();
                        // refresh
                        currentFiles = currentParent.listFiles();
                        refresh();
                    }
                }
                new EncryptTask().execute();
            });
            encryptionDialog.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // handle back operation
        requireView().setFocusableInTouchMode(true);
        requireView().requestFocus();
        requireView().setOnKeyListener((view, i, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_BACK) {
                handleBack();
                return true;
            }
            return false;
        });
        // delete cache for safety
        deleteFile(cacheDir);
    }

    /**
     * Init recycler view after permission granted.
     *
     * @param requestCode  request code
     * @param permissions  permissions
     * @param grantResults grant results
     */
    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (rootDir.exists()) {
                    currentParent = rootDir;
                    currentFiles = rootDir.listFiles();
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
     */
    public void dialogNewFolder() {
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
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

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
            public void afterTextChanged(Editable editable) {
            }
        });
        btnPositive.setTextColor(gray_500);
        btnPositive.setClickable(false);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(yellow_500);
    }

    /**
     * The dialog of editing a file/folder
     *
     * @param position position of the file/folder to be edited
     */
    public void dialogEdit(int position) {
        final String[] options = {(String) activity.getText(R.string.edit_file_rename), (String) activity.getText(R.string.edit_file_delete)};
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setIcon(getItemIcon(position))
                .setTitle(getItemName(position))
                .setItems(options, (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            dialogRename(position);
                            break;
                        case 1:
                            dialogDeleteConfirm(position);
                            break;
                    }
                }).create();
        setDialogBackground(dialog);
        dialog.show();
    }

    /**
     * The dialog of renaming a file/folder.
     *
     * @param position position of the file/folder to be renamed
     */
    public void dialogRename(int position) {
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
                    // prevent unknown crash
                    try {
                        editText.setSelection(0, file.isFile() ? currentName.lastIndexOf(".") : currentName.length());
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
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
                    String fileName = file.getName();
                    if (MyNameFormatter.isLockedFolder(fileName)) {
                        newName = MyNameFormatter.parsePrefix(file.getName()) + newName;
                    }
                    if (rename(file, newName)) {
                        currentFiles = currentParent.listFiles();
                        refresh();
                    } else {
                        MyToastMaker.make(String.valueOf(activity.getText(R.string.file_name_exist)), activity);
                    }
                    dialog0.dismiss();
                }).create();
        setDialogBackground(dialog);
        dialog.show();
        // prevent null or same name
        Button btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

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
            public void afterTextChanged(Editable editable) {
            }
        });
        btnPositive.setTextColor(gray_500);
        btnPositive.setClickable(false);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(yellow_500);
    }

    /**
     * The dialog of confirming a deletion of a file/folder.
     *
     * @param position position of the file/folder to be renamed
     */
    public void dialogDeleteConfirm(int position) {
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
        if (rootDir.equals(currentParent)) {
            btnBack.setVisibility(View.GONE);
            tvPath.setText(title);
        } else {
            btnBack.setVisibility(View.VISIBLE);
            File parent = currentParent.getParentFile();
            if (rootDir.equals(parent)) {
                btnBack.setText(title);
            } else {
                assert parent != null;
                btnBack.setText(MyNameFormatter.parseFolderName(parent.getName()));
            }
            tvPath.setText(MyNameFormatter.parseFolderName(currentParent.getName()));
        }

        // refresh recycler view
        currentFiles = sortFiles();
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
     *
     * @param name name of the folder to be created
     * @return creation result
     */
    public boolean newFolder(String name) {
        File file = new File(currentParent.getPath() + "/" + name);
        if (file.exists()) {
            MyToastMaker.make((String) activity.getText(R.string.new_folder_exists), activity);
            return false;
        }
        return file.mkdir();
    }

    /**
     * Delete a file/folder.
     *
     * @param dirFile file/folder to be deleted
     * @return deletion result
     */
    public boolean deleteFile(File dirFile) {
        if (!dirFile.exists()) {
            return true;
        }
        if (dirFile.isDirectory()) {
            for (File file : Objects.requireNonNull(dirFile.listFiles())) {
                deleteFile(file);
            }
        }
        return dirFile.delete();
    }

    /**
     * Rename a file/folder.
     *
     * @param file    file/folder to be renamed
     * @param newName new name
     * @return renaming result
     */
    public boolean rename(File file, String newName) {
        File newFile = new File(file.getParent() + "/" + newName);
        if (newFile.exists()) {
            MyToastMaker.make(String.valueOf(activity.getText(R.string.file_name_exist)), activity);
            return false;
        } else {
            return file.renameTo(new File(file.getParent() + "/" + newName));
        }
    }

    /**
     * Priority: folder > file.
     *
     * @return sorted files
     */
    public File[] sortFiles() {
        // separate folders and files
        ArrayList<File> folders = new ArrayList<>();
        ArrayList<File> files = new ArrayList<>();
        for (File file : currentFiles) {
            if (file.isFile()) {
                folders.add(file);
            } else {
                files.add(file);
            }
        }

        // sort respectively with the same rule
        Comparator<File> comparator;
        int sort = pref.getInt("sort", MyDefaultPref.getDefaultInt("sort"));
        boolean ascend = pref.getInt("order", MyDefaultPref.getDefaultInt("order")) == 0;
        switch (sort) {
            case 1:
                // sort by creation date
                comparator = Comparator.comparingDouble(File::lastModified);
                break;
            case 2:
                // sort by size
                comparator = new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        return (int) (MyAdapter.getFileSize(f1) - MyAdapter.getFileSize(f2));
                    }
                };
                break;
            default:
                // sort by name
                comparator = Comparator.naturalOrder();
        }
        if (ascend) {
            folders.sort(comparator);
            files.sort(comparator);
        } else {
            folders.sort(comparator.reversed());
            files.sort(comparator.reversed());
        }

        // concatenate
        files.addAll(folders);
        return files.toArray(new File[0]);
    }

    /**
     * Actions after back operation is triggered.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void handleBack() {
        if (!rootDir.equals(currentParent)) {
            folderKeys.pop();
            // back to parent folder
            currentParent = currentParent.getParentFile();
            assert currentParent != null;
            currentFiles = currentParent.listFiles();
            refresh();
        } else {
            // quit app
            activity.finishAffinity();
        }
    }

    /**
     * Enter the directory
     * @param file directory to be entered
     */
    public void handleEnter(File file) {
        File[] files = file.listFiles();
        currentParent = file;
        currentFiles = files;
        refresh();
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

    /**
     * Get file/folder name of a entry.
     *
     * @param position the index in the recycler view
     * @return file/folder name
     */
    public String getItemName(int position) {
        View viewItem = Objects.requireNonNull(recyclerFileList.getLayoutManager()).findViewByPosition(position);
        assert viewItem != null;
        return (String) ((TextView) viewItem.findViewById(R.id.tvFileName)).getText();
    }

    /**
     * Get file/folder icon of a entry.
     *
     * @param position the index in the recycler view
     * @return file/folder icon
     */
    public Drawable getItemIcon(int position) {
        View viewItem = Objects.requireNonNull(recyclerFileList.getLayoutManager()).findViewByPosition(position);
        assert viewItem != null;
        return ((ImageView) viewItem.findViewById(R.id.imgFileIcon)).getDrawable();
    }

    /**
     * Set dialog background.
     *
     * @param dialog dialog
     */
    public void setDialogBackground(AlertDialog dialog) {
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.alert_dialog_background);
    }

    /**
     * Set an icon for a given menu item from shared preferences.
     *
     * @param menuItem menu item to be proceeded
     * @param prefName entry name of shared preferences
     * @param iconIds list of corresponding icon resource ids
     */
    public void setMenuItemIcon(MenuItem menuItem, String prefName, int[] iconIds) {
        addMenuItemIcon(menuItem, AppCompatResources.getDrawable(activity, iconIds[pref.getInt(prefName, MyDefaultPref.getDefaultInt(prefName))]));
    }

    /**
     * Converts the given MenuItem's title into a Spannable containing both its icon and title.
     */
    private void addMenuItemIcon(MenuItem menuItem, Drawable icon) {
        // handle null icon
        if (icon == null) return;

        // calculate icon size
        int iconHeight = MyPixelConverter.spToPx(18, activity);
        icon.setBounds(0, 0, iconHeight * icon.getIntrinsicWidth() / icon.getIntrinsicHeight(), iconHeight);
        // set color
        icon.setTint(yellow_500);
        // set center alignment
        ImageSpan imageSpan;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            imageSpan = new ImageSpan(icon, ImageSpan.ALIGN_CENTER);
        } else {
            imageSpan = new AlignedImageSpan(icon);
        }
        // add a space placeholder for the icon before the title
        SpannableStringBuilder builder = new SpannableStringBuilder("   " + menuItem.getTitle());
        // replace the space placeholder with icon
        builder.setSpan(imageSpan, 0, 1, 0);
        menuItem.setTitle(builder);
    }
}
