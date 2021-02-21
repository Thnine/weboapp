package com.example.weboapp.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weboapp.Entity.img_entity;
import com.example.weboapp.Holder.UnitImageHolder;
import com.example.weboapp.R;

import java.util.List;

public class UnitImageAdapter extends RecyclerView.Adapter<UnitImageHolder>{
    List<img_entity> imgs;
    Activity mActivity;
    public UnitImageAdapter(List<img_entity> imgs,Activity activity){
        this.imgs = imgs;
        this.mActivity = activity;
    }
    @NonNull
    @Override
    public UnitImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mActivity);
        View view = layoutInflater.inflate(R.layout.recycle_image_unit_layout,parent,false);
        return new UnitImageHolder(view,mActivity);
    }

    @Override
    public void onBindViewHolder(@NonNull UnitImageHolder holder, int position) {
        img_entity img = imgs.get(position);
        holder.repaint_unit(img);
    }


    @Override
    public int getItemCount() {
        return imgs.size();
    }
}