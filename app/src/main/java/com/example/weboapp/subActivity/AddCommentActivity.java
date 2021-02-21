package com.example.weboapp.subActivity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.weboapp.Entity.PostRecord;
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

public class AddCommentActivity extends AppCompatActivity {

    //控件信息
    private ImageButton mAddCommentCommitButton;
    private EditText mAddCommentEditText;

    //数据信息
    private String username;
    private PostRecord mypost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comment);
        //去掉标题栏
        getSupportActionBar().hide();
        //获取控件
        mAddCommentCommitButton = (ImageButton)findViewById(R.id.add_comment_commit);
        mAddCommentEditText = (EditText)findViewById(R.id.add_comment_text);
        //获取数据
        username = getIntent().getStringExtra("username");
        mypost = (PostRecord)getIntent().getSerializableExtra("mypost");

        mAddCommentCommitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void run() {
                        String text = mAddCommentEditText.getText().toString();
                        String url = "http://121.196.149.163:8080/dzwblog/AddComment_server";

                        boolean result = false;
                        //创建HTTP对象
                        HttpPost httpRequset = new HttpPost(url);
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("username", username));
                        params.add (new BasicNameValuePair("comment_text", text));
                        params.add(new BasicNameValuePair("post_id",Integer.toString(mypost.getPost_id())));

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
                                result = Boolean.parseBoolean(myjson.get("result").toString());
                            }catch(Exception e){
                                e.printStackTrace();
                            }


                            if(result){
                                Looper.prepare();
                                Toast.makeText(AddCommentActivity.this,R.string.add_comment_success,Toast.LENGTH_SHORT).show();
                                Looper.loop();

                            }
                            else{
                                Looper.prepare();
                                Toast.makeText(AddCommentActivity.this,R.string.add_comment_failed,Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
                AddCommentActivity.this.finish();

            }
        });

    }
}