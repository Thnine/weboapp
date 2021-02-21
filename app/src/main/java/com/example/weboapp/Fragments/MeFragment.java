package com.example.weboapp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.weboapp.R;

public class MeFragment extends Fragment {
    @Override
    //初始化
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
    }


    @Override
    //初始化视图
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_me,container,false);
        return v;
    }


}
