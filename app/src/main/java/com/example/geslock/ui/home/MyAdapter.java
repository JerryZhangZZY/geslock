package com.example.geslock.ui.home;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geslock.R;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private final Context context;
    private final List<File> list;
    private MyViewHolder.OnItemClickListener mListener;
    private BasicFileAttributes attr;
    private FileTime time;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public MyAdapter(Context context, List<File> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.file_entry, viewGroup, false);
        return new MyViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        File file = list.get(i);

        myViewHolder.tvFileName.setText(file.getName());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                time = attr.creationTime();
                myViewHolder.tvFileDate.setText(simpleDateFormat.format(new Date(time.toMillis())));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (file.isDirectory()) {
            myViewHolder.imgIcon.setImageResource(R.drawable.ic_basketball_small);
        } else {
            myViewHolder.imgIcon.setImageResource(R.drawable.ic_donut_small);
        }
    }

    public void setOnItemClickListener(MyViewHolder.OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imgIcon;
        TextView tvFileName;
        TextView tvFileDate;
        OnItemClickListener mListener;

        public MyViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgFileIcon);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvFileDate = itemView.findViewById(R.id.tvFileDate);
            mListener = listener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mListener.onItemClick(view, getAdapterPosition());
        }

        public interface OnItemClickListener {
            void onItemClick(View view, int position);
        }
    }
}

