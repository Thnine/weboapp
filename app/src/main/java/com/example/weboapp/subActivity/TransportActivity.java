package com.example.weboapp.subActivity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TransportActivity extends AppCompatActivity {

    public String username;//当前用户
    public int transport_id;//转发帖子

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
        transport_id = getIntent().getIntExtra("transport_id",0);


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
                        String url = "http://121.196.149.163:8080/dzwblog/AddTransport_server";

                        boolean result = false;
                        //创建HTTP对象
                        HttpPost httpRequset = new HttpPost(url);
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("username", username));
                        params.add (new BasicNameValuePair("text", text));
                        params.add(new BasicNameValuePair("transport_id", Integer.toString(transport_id)));

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
                                Toast.makeText(TransportActivity.this,"转发成功",Toast.LENGTH_SHORT).show();
                                Looper.loop();

                            }
                            else{
                                Looper.prepare();
                                Toast.makeText(TransportActivity.this,"转发失败",Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
                TransportActivity.this.finish();

            }
        });

    }
}