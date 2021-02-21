package com.example.weboapp.subActivity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.weboapp.MainActivity;
import com.example.weboapp.R;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AddTextActivity extends AppCompatActivity {

    public String username;//当前用户

    ImageButton mCommitButton;
    EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_text);

        //去掉标题栏
        getSupportActionBar().hide();

        //获取Intent数据
        username = getIntent().getStringExtra("username");


        //配置编辑框
        mEditText = (EditText)findViewById(R.id.add_text_text);

        //配置按钮
        mCommitButton = (ImageButton)findViewById(R.id.add_text_commit);
        mCommitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void run() {
                        String text = mEditText.getText().toString();
                        String url = "http://121.196.149.163:8080/dzwblog/AddPost_server";

                        boolean result = false;
                        //创建HTTP对象
                        HttpPost httpRequset = new HttpPost(url);
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("username", username));
                        params.add (new BasicNameValuePair("text", text));

                        try{
                            //装入数据和设置数据格式
                            httpRequset.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
                            //发出请求
                            CloseableHttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpRequset);
                            //获取服务器响应的资源

                            String line=null;
                            JSONObject resultJson=null;
                            StringBuilder entityStringBuilder=new StringBuilder();
                            try {
                                BufferedReader b = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"),8*1024);
                                while ((line=b.readLine())!=null) {
                                    entityStringBuilder.append(line+"/n");
                                }
                                JSONObject myjson = new JSONObject(entityStringBuilder.toString());
                                result = Boolean.parseBoolean(myjson.get("addpost_result").toString());
                            }catch(Exception e){
                                e.printStackTrace();
                            }


                            if(result){
                                Looper.prepare();
                                Toast.makeText(AddTextActivity.this,R.string.add_post_success,Toast.LENGTH_SHORT).show();
                                Looper.loop();

                            }
                            else{
                                Looper.prepare();
                                Toast.makeText(AddTextActivity.this,R.string.add_post_failed,Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
                AddTextActivity.this.finish();

            }
        });

    }
}