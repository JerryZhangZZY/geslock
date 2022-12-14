package com.example.geslock.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.FileUtils;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
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
    private MyViewHolder.OnItemClickListener clickListener;
    private MyViewHolder.OnItemLongClickListener longClickListener;
    private BasicFileAttributes attr;
    private FileTime time;
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
        // set file name, file icon and enter icon
        if (file.isFile()) {
            String fileName = file.getName();
            myViewHolder.tvFileName.setText(fileName.substring(0, fileName.length() - 2));
            myViewHolder.imgEnter.setVisibility(View.INVISIBLE);
            myViewHolder.imgIcon.setImageResource(pickIcon(file.getName()));
            myViewHolder.tvItemCount.setVisibility(View.INVISIBLE);
        } else {
            myViewHolder.tvFileName.setText(file.getName());
            myViewHolder.imgEnter.setVisibility(View.VISIBLE);
            myViewHolder.imgIcon.setImageResource(R.drawable.ic_folder);
            if (itemCount) {
                myViewHolder.tvItemCount.setText(String.valueOf(Objects.requireNonNull(file.list()).length));
                myViewHolder.tvItemCount.setVisibility(View.VISIBLE);
            } else {
                myViewHolder.tvItemCount.setVisibility(View.INVISIBLE);
            }
        }

        // set file property
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                time = attr.creationTime();
                myViewHolder.tvFileProperty.setText(simpleDateFormat.format(new Date(time.toMillis())));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        myViewHolder.tvFileProperty.setText(file.length() / 1024 + " KB");
//        myViewHolder.tvFileProperty.setText(getFileSize(file) / 1000 + " KB");
    }

    /**
     * Get size of a file or folder.
     * @param file  file or folder
     * @return size in bytes
     */
    public long getFileSize(File file) {
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
     * Pick an icon for a file according to its name.
     *
     * @param fileName file name
     * @return icon resource id
     */
    public int pickIcon(String fileName) {
        // extract extension string from file name
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length() - 2).toLowerCase();
        if (textExtensions.contains(extension)) {
            return R.drawable.ic_txt;
        } else if (imageExtensions.contains(extension)) {
            return R.drawable.ic_image;
        } else if (videoExtensions.contains(extension)) {
            return R.drawable.ic_video;
        } else if (audioExtensions.contains(extension)) {
            return R.drawable.ic_audio;
        } else if (zipExtensions.contains(extension)) {
            return R.drawable.ic_zip;
        } else if (apkExtensions.contains(extension)) {
            return R.drawable.ic_apk;
        } else if (docExtensions.contains(extension)) {
            return R.drawable.ic_doc;
        } else if (pptExtensions.contains(extension)) {
            return R.drawable.ic_ppt;
        } else if (xlsExtensions.contains(extension)) {
            return R.drawable.ic_xls;
        } else if (pdfExtensions.contains(extension)) {
            return R.drawable.ic_pdf;
        } else {
            return R.drawable.ic_file;
        }
    }
}

