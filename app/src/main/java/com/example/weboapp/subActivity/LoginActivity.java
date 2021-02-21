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
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";

    TextView mLoginUsername;
    TextView mLoginPassword;
    TextView mLoginToRegister;
    Button mLoginCommit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //去掉标题栏
        getSupportActionBar().hide();

        //获取控件
        mLoginUsername = (TextView)findViewById(R.id.login_username);
        mLoginPassword = (TextView)findViewById(R.id.login_password);
        mLoginCommit = (Button)findViewById(R.id.login_commit);
        mLoginToRegister = (TextView)findViewById(R.id.login_to_register);

        //设置密码暗文
        mLoginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

        //设置监听的事件
        mLoginCommit.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            //创建登录提交事件
            public void onClick(View v) {
                //TODO 现在这里只实现了成功的功能

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //登录判断变量
                        boolean issuc = false;
                        //获取账户名和密码
                        String username = mLoginUsername.getText().toString();
                        String password = mLoginPassword.getText().toString();
                        String url = "http://121.196.149.163:8080/dzwblog/Login_server";

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
                                issuc = Boolean.parseBoolean(myjson.get("login_result").toString());
                            }catch(Exception e){
                                e.printStackTrace();
                            }


                            if(issuc){
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);    //第二个参数即为执行完跳转后的Activity
                                intent.putExtra("username",username);
                                startActivity(intent);
                                LoginActivity.this.finish();
                            }
                            else{
                                Looper.prepare();
                                Toast.makeText(LoginActivity.this,R.string.login_failed,Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }});

        mLoginToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            //创建登录跳转到注册的事件
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);    //第二个参数即为执行完跳转后的Activity
                startActivity(intent);
            }
        });


    }
}