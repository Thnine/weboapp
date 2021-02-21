package com.example.weboapp.subActivity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.weboapp.R;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class AddPicActivity extends AppCompatActivity {

    //用户信息
    String username;

    //控件信息
    ImageButton mCommitButton;
    ImageButton mUploadButton;
    EditText mEditText;
    ImageView mPic1;
    ImageView mPic2;

    //图片信息
    List<Uri> uris;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pic);

        //隐藏标题栏
        getSupportActionBar().hide();

        //获取intent中数据
        username = getIntent().getStringExtra("username");

        /**
         * 初始化控件
         */
        //提交按钮
        mCommitButton = findViewById(R.id.add_pic_commit);
        //上传按钮
        mUploadButton = findViewById(R.id.add_pic_upload_pic);
        //编辑框
        mEditText = findViewById(R.id.add_pic_text);
        //图片控件
        mPic1 = findViewById(R.id.add_pic_pic1);
        mPic2 = findViewById(R.id.add_pic_pic2);


        //获取读写文件权限

        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                }
            }
        }

        //初始化图片url数组
        uris = new ArrayList<>();

        /**
         * 编辑按钮事件
         */
        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            //上传图片按钮
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, 2);
            }
        });

        /**
         * 编辑图片提交事件
         */
        mCommitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void run() {

                        String text = mEditText.getText().toString();
                        String url = "http://121.196.149.163:8080/dzwblog/AddPic_server";

                        boolean result = false;
                        //创建HTTP对象
                        HttpPost httpRequset = new HttpPost(url);
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("username", username));
                        params.add(new BasicNameValuePair("text", text));
                        params.add(new BasicNameValuePair("pic_num", Integer.toString(uris.size())));
                        Log.d("AddPicActivity","uris.size:" + Integer.toString(uris.size()));
                        for (int i = 0; i < uris.size(); i++) {

                            try {
                                Log.d("AddPicActivity", "before_uri");

                                String[] proj = {MediaStore.Images.Media.DATA};
                                Cursor actualimagecursor = managedQuery(uris.get(i), proj, null, null, null);
                                int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                                actualimagecursor.moveToFirst();
                                String img_path = actualimagecursor.getString(actual_image_column_index);
                                Log.d("AddPicActivity", "" + img_path);
                                File file = new File(img_path);

                                //读取文件为字节流
                                byte[] buffer = null;
                                try {
                                    FileInputStream fis = new FileInputStream(file);
                                    ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
                                    byte[] b = new byte[1000];
                                    int n;
                                    while ((n = fis.read(b)) != -1) {
                                        bos.write(b, 0, n);
                                    }
                                    fis.close();
                                    bos.close();
                                    buffer = bos.toByteArray();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                //传输数据
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    params.add(new BasicNameValuePair("pic" + (i + 1), Base64.getEncoder().encodeToString(buffer)));
                                    params.add(new BasicNameValuePair("pic_name" + (i + 1), file.getName()));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                            try {
                                //装入数据和设置数据格式
                                httpRequset.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
                                //发出请求
                                CloseableHttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpRequset);



                                //获取服务器响应的资源
                                String line = null;
                                StringBuilder entityStringBuilder = new StringBuilder();
                                try {
                                    BufferedReader b = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"), 8 * 1024);
                                    while ((line = b.readLine()) != null) {
                                        entityStringBuilder.append(line + "/n");
                                    }
                                    Log.d("AddPicActivity","response:" + entityStringBuilder.toString());
                                    JSONObject myjson = new JSONObject(entityStringBuilder.toString());
                                    result = Boolean.parseBoolean(myjson.get("addpost_result").toString());
                                    Log.d("AddPicActivity","state:" + myjson.getString("state"));



                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                                if (result) {
                                    Looper.prepare();
                                    Toast.makeText(AddPicActivity.this, R.string.add_post_success, Toast.LENGTH_SHORT).show();
                                    Looper.loop();

                                } else {
                                    Looper.prepare();
                                    Toast.makeText(AddPicActivity.this, R.string.add_post_failed, Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                    }
                }).start();
                AddPicActivity.this.finish();

            }
        });

        //编辑图片删除事件
        mPic1.setClickable(true);
        mPic2.setClickable(true);
        mPic1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(uris.size() > 0)
                    uris.remove(0);
                Log.d("pic1","click!");
                repaint_pics();
            }
        });
        mPic2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(uris.size() > 1)
                    uris.remove(1);
                Log.d("pic2","click!");
                repaint_pics();
            }
        });


    }

    //响应打开新窗口的结果的函数
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            // 从相册返回的数据
            if (data != null) {
                // 得到图片的全路径
                Uri uri = data.getData();
                uris.add(uri);
                //绘制图片
                repaint_pics();
            }
        }
    }

    //重绘制图片
    private void repaint_pics(){
        mPic1.setImageURI(null);
        mPic2.setImageURI(null);
        for(int i = 0;i < uris.size();i++){
            if(i == 0){
                mPic1.setImageURI(uris.get(i));
            }
            else if(i == 1){
                mPic2.setImageURI(uris.get(i));
            }
        }
    }

}