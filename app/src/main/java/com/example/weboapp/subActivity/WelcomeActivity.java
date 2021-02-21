package com.example.weboapp.subActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.TextView;

import com.example.weboapp.MainActivity;
import com.example.weboapp.R;

public class WelcomeActivity extends AppCompatActivity {

    private static int DISPLAY_TIME = 2000; //展示事件为2s
    private TextView welcome_title;//welcome界面标题文本控件

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);


        //去掉标题栏
        getSupportActionBar().hide();

        //设置welcome界面标题字体
        AssetManager mgr = getAssets();
        Typeface tf = Typeface.createFromAsset(mgr,"fonts/方正静蕾简体.TTF");
        welcome_title = (TextView)findViewById(R.id.welcome_title);
        welcome_title.setTypeface(tf);

        //welcome结束后的跳转
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);	//第二个参数即为执行完跳转后的Activity
                startActivity(intent);
                WelcomeActivity.this.finish();   //关闭WelcomeActivity，将其回收，否则按返回键会返回此界面
            }
        }, DISPLAY_TIME);
    }
}