package com.example.geslock.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geslock.R;
import com.example.geslock.tools.MyDefaultPref;
import com.example.geslock.tools.MyNameFormatter;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    /*
    all supported file types
     */
    private final List<String> textExtensions = Collections.singletonList("txt");
    private final List<String> imageExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "tiff", "webp", "ico");
    private final List<String> videoExtensions = Arrays.asList("mp4", "mov", "avi", "flv", "wmv", "mpeg", "mkv", "asf");
    private final List<String> audioExtensions = Arrays.asList("mp3", "wav", "cda", "aif", "aiff");
    private final List<String> zipExtensions = Arrays.asList("zip", "7z", "rar", "tz", "arj");
    private final List<String> apkExtensions = Collections.singletonList("apk");
    private final List<String> docExtensions = Arrays.asList("doc", "docx");
    private final List<String> pptExtensions = Arrays.asList("ppt", "pptx");
    private final List<String> xlsExtensions = Arrays.asList("xls", "xlsx", "xlsb", "xlsm", "csv");
    private final List<String> pdfExtensions = Collections.singletonList("pdf");

    private final Context context;
    private final List<File> list;
    private final SharedPreferences pref;
    private final boolean itemCount;
    private int icon;
    private String type;
    private MyViewHolder.OnItemClickListener clickListener;
    private MyViewHolder.OnItemLongClickListener longClickListener;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public MyAdapter(Context context, List<File> list) {
        this.context = context;
        this.list = list;
        pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        itemCount = pref.getBoolean("item-count", MyDefaultPref.getDefaultBoolean("item-count"));
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item, viewGroup, false);
        return new MyViewHolder(view, clickListener, longClickListener);
    }

    /**
     * Set file entry appearance.
     *
     * @param myViewHolder view holder
     * @param i            file index
     */
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        File file = list.get(i);
        setIconAndType(file);
        myViewHolder.imgIcon.setImageResource(icon);
        // set file name, file icon and enter icon
        if (file.isFile()) {
            String fileName = file.getName();
            myViewHolder.tvFileName.setText(fileName.substring(0, fileName.length() - 2));
            myViewHolder.imgEnter.setVisibility(View.INVISIBLE);
            myViewHolder.tvItemCount.setVisibility(View.INVISIBLE);
        } else {
            String name = MyNameFormatter.parseFolderName(file.getName());
            myViewHolder.tvFileName.setText(name);
            myViewHolder.imgEnter.setVisibility(View.VISIBLE);
            if (itemCount) {
                myViewHolder.tvItemCount.setText(String.valueOf(Objects.requireNonNull(file.list()).length));
                myViewHolder.tvItemCount.setVisibility(View.VISIBLE);
            } else {
                myViewHolder.tvItemCount.setVisibility(View.INVISIBLE);
            }
        }

        // set file property
        setProperty(myViewHolder.tvFileProperty, file);
    }

    /**
     * Set additional file property on the subtitle
     * @param textView subtitle textview
     * @param file corresponding file
     */
    public void setProperty(TextView textView, File file) {
        int property = pref.getInt("property", MyDefaultPref.getDefaultInt("property"));
        switch (property) {
            case 0:
                // show creation time
                textView.setText(simpleDateFormat.format(new Date(file.lastModified())));
                break;
            case 1:
                // show file size
                textView.setText(formatFileSize(getFileSize(file)));
                break;
            case 2:
                // show file type
                textView.setText(type);
                break;
            case 3:
                // hide property
                textView.setText(null);
        }
    }

    /**
     * Get size of a file or folder.
     * @param file  file or folder
     * @return size in bytes
     */
    public static long getFileSize(File file) {
        if (file.isFile()) {
            return file.length();
        }
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (File value : fileList) {
                if (value.isDirectory()) size = size + getFileSize(value);
                else size = size + value.length();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * Format file size.
     * @param size file size in bytes
     * @return formatted file size with standard units
     */
    public String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public void setOnItemClickListener(MyViewHolder.OnItemClickListener listener) {
        clickListener = listener;
    }

    public void setOnItemLongClickListener(MyViewHolder.OnItemLongClickListener listener) {
        longClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        CardView cardFile;
        ImageView imgIcon;
        ImageView imgEnter;
        TextView tvFileName;
        TextView tvFileProperty;
        TextView tvItemCount;
        OnItemClickListener clickListener;
        OnItemLongClickListener longClickListener;

        public MyViewHolder(@NonNull View itemView, OnItemClickListener clickListener, OnItemLongClickListener longClickListener) {
            super(itemView);
            cardFile = itemView.findViewById(R.id.cardFile);
            imgIcon = itemView.findViewById(R.id.imgFileIcon);
            imgEnter = itemView.findViewById(R.id.imgEnter);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvFileProperty = itemView.findViewById(R.id.tvFileProperty);
            tvItemCount = itemView.findViewById(R.id.tvItemCount);
            this.clickListener = clickListener;
            this.longClickListener = longClickListener;
            cardFile.setOnClickListener(this);
            cardFile.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            clickListener.onItemClick(view, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            longClickListener.onItemLongClick(view, getAdapterPosition());
            return true;
        }

        public interface OnItemClickListener {
            void onItemClick(View view, int position);
        }

        public interface OnItemLongClickListener {
            void onItemLongClick(View view, int position);
        }
    }

    /**
     * TODO
     *
     * @param file file
     */
    public void setIconAndType(File file) {
        String fileName = file.getName();
        if (file.isDirectory()) {
            if (MyNameFormatter.isLocked(fileName)) {
                icon = R.drawable.ic_folder_locked;
                type = context.getString(R.string.type_folder_locked);
            } else {
                icon = R.drawable.ic_folder;
                type = context.getString(R.string.type_folder);
            }
            return;
        }
        // extract extension string from file name
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length() - 2).toLowerCase();
        if (textExtensions.contains(extension)) {
            icon = R.drawable.ic_txt;
            type = context.getString(R.string.type_text);
        } else if (imageExtensions.contains(extension)) {
            icon = R.drawable.ic_image;
            type = context.getString(R.string.type_image);
        } else if (videoExtensions.contains(extension)) {
            icon = R.drawable.ic_video;
            type = context.getString(R.string.type_video);
        } else if (audioExtensions.contains(extension)) {
            icon = R.drawable.ic_audio;
            type = context.getString(R.string.type_audio);
        } else if (zipExtensions.contains(extension)) {
            icon = R.drawable.ic_zip;
            type = context.getString(R.string.type_zip);
        } else if (apkExtensions.contains(extension)) {
            icon = R.drawable.ic_apk;
            type = context.getString(R.string.type_apk);
        } else if (docExtensions.contains(extension)) {
            icon = R.drawable.ic_doc;
            type = context.getString(R.string.type_doc);
        } else if (pptExtensions.contains(extension)) {
            icon = R.drawable.ic_ppt;
            type = context.getString(R.string.type_ppt);
        } else if (xlsExtensions.contains(extension)) {
            icon = R.drawable.ic_xls;
            type = context.getString(R.string.type_xls);
        } else if (pdfExtensions.contains(extension)) {
            icon = R.drawable.ic_pdf;
            type = context.getString(R.string.type_pdf);
        } else {
            icon = R.drawable.ic_file;
            type = context.getString(R.string.type_other);
        }
    }
}

