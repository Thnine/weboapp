package com.example.weboapp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;


import com.example.weboapp.Controls.RoundImageView;
import com.example.weboapp.R;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

//图标获取类
public class IconLoader {
    public Activity contextAc = null;
    public Fragment contextFg = null;
    public RoundImageView RoundImage;
    public String username;

    public IconLoader(Activity contextAc){
        this.contextAc = contextAc;
        //申请权限
        /**
         * 动态申请权限
         */
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M
                && contextAc.checkSelfPermission
                (Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            contextAc.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
    }


    public IconLoader(Fragment contextFg){
        this.contextFg = contextFg;
        //申请权限
        /**
         * 动态申请权限
         */
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M
                && contextFg.getContext().checkSelfPermission
                (Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            contextFg.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
    }

    public void OnBindInfo(RoundImageView RoundImage,String username){
        this.RoundImage = RoundImage;
        this.username = username;
    }

    //获取图标文件夹路径
    public String getIconDirPath(){
        if(contextAc != null){
            return contextAc.getFilesDir().getPath();
        }
        else if(contextFg != null){
            return contextFg.getContext().getFilesDir().getPath();
        }
        else
        {
            return "error!";
        }
    }

    public boolean setIconFromUsernameWithoutInternet(){
        if(contextAc != null){
            File file = new File(contextAc.getFilesDir() + File.separator + username + ".jpg");
            if(!file.exists()){
                file = new File(contextAc.getFilesDir() + File.separator + username + ".png");
                if(!file.exists()){
                    return false;
                }
            }
            final File finalFile = file;
            this.contextAc.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap= BitmapFactory.decodeFile(finalFile.getPath());
                    RoundImage.setImageBitmap(bitmap);
                }
            });

            return true;
        }
        else if(contextFg != null){
            File file = new File(contextFg.getContext().getFilesDir() + File.separator + username + ".jpg");
            if(!file.exists()){
                file = new File(contextFg.getContext().getFilesDir() + File.separator + username + ".png");
                if(!file.exists()){
                    return false;
                }
            }
            final File finalFile = file;
            this.contextFg.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap= BitmapFactory.decodeFile(finalFile.getPath());
                    RoundImage.setImageBitmap(bitmap);
                }
            });
            return true;
        }
        else{
            return false;
        }


    }

    public void setIcon(){
        if(!setIconFromUsernameWithoutInternet()){
            new LoadThread().start();
        }
    }

    private class LoadThread extends Thread{
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void run(){
            String url = "http://121.196.149.163:8080/dzwblog/GetIcon_server";

            //创建HTTP对象
            HttpPost httpRequset = new HttpPost(url);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("username", "" + username));

            try{
                //装入数据和设置数据格式
                httpRequset.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
                //发出请求
                CloseableHttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpRequset);
                //获取服务器响应的资源

                String line=null;
                StringBuilder entityStringBuilder=new StringBuilder();
                try {
                    BufferedReader b = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"),8*1024);
                    while ((line=b.readLine())!=null) {
                        entityStringBuilder.append(line+"/n");
                    }
                    //处理Array数据
                    JSONObject myjson = new JSONObject(entityStringBuilder.toString());
                    boolean result = myjson.getBoolean("result");
                    if(result) {


                        String suffix = myjson.getString("suffix");
                        String temp_icon = myjson.getString("icon");
                        byte[] toT = new byte[0];
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            toT = Base64.getDecoder().decode(temp_icon);
                        }
                        //byte数组转文件
                        //开始传输文件
                        BufferedOutputStream bos = null;
                        FileOutputStream fos = null;
                        File file = new File(getIconDirPath() + File.separator + username + "." + suffix);
                        fos = new FileOutputStream(file);
                        bos = new BufferedOutputStream(fos);
                        bos.write(toT);
                        bos.flush();
                        bos.close();
                        fos.close();
                        setIconFromUsernameWithoutInternet();
                    }
                    else{
                        RoundImage.setImageResource(R.drawable.anoymous_novec);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void reloadIcon(){
        new LoadThread().start();
    }

}
