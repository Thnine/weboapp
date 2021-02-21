package com.example.weboapp.subActivity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    TextView mRegisterUsername;
    TextView mRegisterPassword;
    Button mRegisterCommit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //去掉标题栏
        getSupportActionBar().hide();

        //获取控件
        mRegisterUsername = (TextView)findViewById(R.id.register_username);
        mRegisterPassword = (TextView)findViewById(R.id.register_password);
        mRegisterCommit = (Button)findViewById(R.id.register_commit);

        //设置密码暗文
        mRegisterPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

        //设置监听的事件
        mRegisterCommit.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            //创建提交事件
            public void onClick(View v) {
                //TODO 现在这里只实现了成功的功能

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //登录判断变量
                        boolean issuc = false;
                        //获取账户名和密码
                        String username = mRegisterUsername.getText().toString();
                        String password = mRegisterPassword.getText().toString();
                        String url = "http://121.196.149.163:8080/dzwblog/Register_server";

                        //创建HTTP对象
                        HttpPost httpRequset = new HttpPost(url);
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("username", username));
                        params.add (new BasicNameValuePair("password", password));
                        params.add(new BasicNameValuePair("flag","0"));

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
                                issuc = Boolean.parseBoolean(myjson.get("register_result").toString());
                            }catch(Exception e){
                                e.printStackTrace();
                            }


                            if(issuc){
                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);    //第二个参数即为执行完跳转后的Activity
                                intent.putExtra("username",username);
                                startActivity(intent);
                                RegisterActivity.this.finish();
                            }
                            else{
                                Looper.prepare();
                                Toast.makeText(RegisterActivity.this,R.string.register_mulUser_failed,Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }});

    }
}