package com.example.weboapp.Holder;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weboapp.Entity.img_entity;
import com.example.weboapp.R;

public class UnitImageHolder extends RecyclerView.ViewHolder{

    ImageView imgv;
    img_entity img;
    Context mConText;
    public UnitImageHolder(@NonNull View itemView,Context context) {
        super(itemView);
        this.mConText = context;
        imgv = (ImageView)itemView.findViewById(R.id.re_image_image);
        /**实现阅览界面**/
        imgv.setClickable(true);
        imgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(img != null) {
                    final Dialog dialog = new Dialog(mConText, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                    ImageView bigview = new ImageView(mConText);
                    bigview.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    bigview.setImageURI(Uri.fromFile(img.get_content()));
                    dialog.setContentView(bigview);
                    dialog.show();
                }
            }
        });
    }

    public void repaint_unit(img_entity img){
        this.img = img;
        if(img.is_loaded())
            imgv.setImageURI(Uri.fromFile(img.get_content()));
    }
}
