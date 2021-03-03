package com.example.weboapp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.weboapp.Controls.RoundImageView;
import com.example.weboapp.R;
import com.example.weboapp.utils.IconLoader;

public class MeFragment extends Fragment {
    //控件
    RoundImageView RoundImageIcon;
    TextView TextUsername;
    String username;

    @Override
    //初始化
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        if (getArguments() != null) {
            username = getArguments().getString("username");
        }
    }


    @Override
    //初始化视图
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_me,container,false);
        //初始化控件
        RoundImageIcon = (RoundImageView)v.findViewById(R.id.f_me_user_icon);
        TextUsername = (TextView)v.findViewById(R.id.f_me_user_name);
        TextUsername.setText(username);
        IconLoader IL = new IconLoader(this);
        IL.OnBindInfo(RoundImageIcon,username);
        IL.setIcon();
        IL.reloadIcon();
        return v;
    }


}
