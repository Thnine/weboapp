package com.example.weboapp.subActivity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weboapp.Adapter.UnitImageAdapter;
import com.example.weboapp.Controls.RoundImageView;
import com.example.weboapp.Entity.CommentRecord;
import com.example.weboapp.Entity.PostRecord;
import com.example.weboapp.Entity.img_entity;
import com.example.weboapp.R;
import com.example.weboapp.utils.IconLoader;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PostInfoActivity extends AppCompatActivity {

    //当前用户信息
    PostRecord mypost;
    //当前用户用户名
    String username;
    //评论信息
    List<CommentRecord> comments;

    //控件信息
    private RecyclerView commentRv;
    private RecyclerView commentImageRv;
    private CommentAdapter commentAp;
    private TextView PostInfoUsername;
    private TextView PostInfoBody;
    private TextView PostInfoDate;
    private RoundImageView PostInfoIcon;
    private ImageButton AddCommentButton;
    private EditText mCommentText;

    //转发控件信息
    private RoundImageView transportIcon;
    private TextView transportUsername;
    private TextView transportDate;
    private TextView transportBody;
    private RecyclerView transportImageRv;
    private LinearLayout transportL;
    //Adapter
    private UnitImageAdapter mImageAp;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_info);
        //去掉标题栏
        getSupportActionBar().hide();
        //从intent中提取数据
        mypost = (PostRecord) getIntent().getSerializableExtra("mypost");
        username = getIntent().getStringExtra("username");
        //配置帖子详细信息控件
        this.PostInfoIcon = (RoundImageView) findViewById(R.id.post_info_user_icon);
        this.PostInfoUsername = (TextView)findViewById(R.id.post_info_user_name);
        this.PostInfoBody = (TextView)findViewById(R.id.post_info_post_body);
        this.PostInfoDate = (TextView)findViewById(R.id.post_info_date);
        this.AddCommentButton = (ImageButton)findViewById(R.id.post_info_commit_button);
        this.mCommentText = (EditText)findViewById(R.id.post_info_comment_text);
        PostInfoUsername.setText(mypost.getUser());
        PostInfoBody.setText(mypost.getBody());
        Timestamp thisdate = mypost.getDate();
        PostInfoDate.setText(thisdate.toString().replaceAll("\\.0",""));
        IconLoader IL1 = new IconLoader(PostInfoActivity.this);
        IL1.OnBindInfo(PostInfoIcon,mypost.getUser());
        IL1.setIcon();

        //转发控件初始化
        this.transportIcon = (RoundImageView)findViewById(R.id.transport_user_icon);
        this.transportUsername = (TextView)findViewById(R.id.transport_user_name);
        this.transportDate = (TextView)findViewById(R.id.transport_date);
        this.transportBody = (TextView)findViewById(R.id.transport_post_body);
        this.transportImageRv = (RecyclerView)findViewById(R.id.transport_unit_image_re);
        this.transportL = (LinearLayout)findViewById(R.id.home_transport_layout);
        PostRecord transportPost = mypost.getTransportRecord();
        if(transportPost != null) {
            //转发信息绘制
            transportL.setVisibility(View.VISIBLE);
            this.transportUsername.setText(transportPost.getUser());
            Timestamp thisdate1 = transportPost.getDate();
            this.transportDate.setText(thisdate1.toString().replaceAll("\\.0",""));
            this.transportBody.setText(transportPost.getBody());
            IconLoader IL2 = new IconLoader(PostInfoActivity.this);
            IL2.OnBindInfo(this.transportIcon,transportPost.getUser());
            IL2.setIcon();
            //配置转发RV
            LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(PostInfoActivity.this);
            linearLayoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
            this.transportImageRv.setLayoutManager(linearLayoutManager1);
            this.mImageAp = new UnitImageAdapter(transportPost.get_imgs(), PostInfoActivity.this);
            this.transportImageRv.setAdapter(this.mImageAp);
        }
        else{
            transportL.setVisibility(View.GONE);
        }

        comments = new ArrayList<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                //网络传输获取评论数据
                getCommentFromDataBase();
            }
        }).start();

        //配置Rv
        commentRv = (RecyclerView)findViewById(R.id.post_info_comment_re);
        commentRv.setLayoutManager(new LinearLayoutManager(PostInfoActivity.this));
        commentAp = new CommentAdapter(comments);
        commentRv.setAdapter(commentAp);

        commentImageRv = (RecyclerView)findViewById(R.id.post_info_unit_image_re);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(PostInfoActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        commentImageRv.setLayoutManager(linearLayoutManager);
        CommentImageAdapter commentImageAp = new CommentImageAdapter(this.mypost.get_imgs());
        commentImageRv.setAdapter(commentImageAp);


        //添加评论事件
        this.AddCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commitComment();
                mCommentText.setText("");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        getCommentFromDataBase();
                    }
                }).start();
            }
        });
    }

    //发送评论
    private void commitComment(){
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                String text = mCommentText.getText().toString();
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
                        Log.d("AddCommentActivity",entityStringBuilder.toString());
                        JSONObject myjson = new JSONObject(entityStringBuilder.toString());
                        result = Boolean.parseBoolean(myjson.get("result").toString());
                    }catch(Exception e){
                        e.printStackTrace();
                    }


                    if(result){
                        Looper.prepare();
                        Toast.makeText(PostInfoActivity.this,R.string.add_comment_success,Toast.LENGTH_SHORT).show();
                        Looper.loop();

                    }
                    else{
                        Looper.prepare();
                        Toast.makeText(PostInfoActivity.this,R.string.add_comment_failed,Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    //从数据库获取评论
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected void getCommentFromDataBase(){

        /**网络传输从数据库获取评论**/
        String url = "http://121.196.149.163:8080/dzwblog/GetComment_server";
        //创建HTTP对象
        HttpPost httpRequset = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("post_id", Integer.toString(mypost.getPost_id())));

        try{
            //装入数据和设置数据格式
            httpRequset.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

            Log.d("PostInfoActivity","hh1");
            //发出请求
            CloseableHttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpRequset);

            Log.d("PostInfoActivity","hh2");
            //获取服务器响应的资源

            String line=null;
            StringBuilder entityStringBuilder=new StringBuilder();
            try {
                BufferedReader b = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"),8*1024);
                while ((line=b.readLine())!=null) {
                    entityStringBuilder.append(line+"/n");
                }
                //处理Array数据
                Log.d("PostInfoActivity","收到jsonArray：" + entityStringBuilder.toString());
                JSONArray myjson = new JSONArray(entityStringBuilder.toString());

                this.comments.clear();
                for(int i = 0;i < myjson.length();i++){

                    JSONObject temp_ob = myjson.getJSONObject(i);
                    CommentRecord toadd = new CommentRecord(temp_ob.getString("comment_username"),Timestamp.valueOf(temp_ob.getString("comment_date")),temp_ob.getString("comment_text"));
                    this.comments.add(toadd);
                }
                Log.d("PostInfoActivity","hh3");
            }catch(Exception e){
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        Runnable t = new Runnable() {
            public void run() {
                commentAp.notifyDataSetChanged();
            }

        };
        PostInfoActivity.this.runOnUiThread(t);

    }


    //commentRv的Holder
    private class CommentHolder extends RecyclerView.ViewHolder{
        //定义部件
        public RoundImageView mCommentUserIcon;//评论单元的图标样式
        public TextView mCommentUsername;//评论单元的作者名
        public TextView mBodyTextView;//评论单元的主体文本
        public TextView mDateTextView;//评论单元时间的文本

        //初始化函数
        public CommentHolder(View itemview){
            super(itemview);
            //初始化控件
            mCommentUserIcon = (RoundImageView) itemview.findViewById(R.id.comment_user_icon);
            mCommentUsername = (TextView) itemview.findViewById(R.id.comment_user_name);
            mBodyTextView = (TextView) itemview.findViewById(R.id.comment_body);
            mDateTextView = (TextView) itemview.findViewById(R.id.comment_date);
        }

        //重绘函数
        public void repaint_unit(CommentRecord mycomment){
            mCommentUsername.setText(mycomment.getUsername());
            mBodyTextView.setText(mycomment.getContent());
            Timestamp thisdate = mycomment.getDate();
            mDateTextView.setText(thisdate.toString().replaceAll("\\.0",""));
            IconLoader IL = new IconLoader(PostInfoActivity.this);
            IL.OnBindInfo(this.mCommentUserIcon,mycomment.getUsername());
            IL.setIcon();
        }

    }

    //commentRv的Adapter
    private class CommentAdapter extends RecyclerView.Adapter<CommentHolder>{
        private List<CommentRecord> comments;
        public CommentAdapter(List<CommentRecord> comments){this.comments = comments;}

        @NonNull
        @Override
        public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(PostInfoActivity.this);
            View view = layoutInflater.inflate(R.layout.recycle_comment_unit_layout,parent,false);
            return new CommentHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CommentHolder holder, int position) {
            CommentRecord comment = comments.get(position);
            holder.repaint_unit(comment);
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }


    }


    //commentImageRv的Holder
    public class CommentImageHolder extends RecyclerView.ViewHolder{

        ImageView imgv;
        img_entity img;
        public CommentImageHolder(@NonNull View itemView) {
            super(itemView);
            imgv = (ImageView)itemView.findViewById(R.id.re_image_image);
            /**实现阅览界面**/
            imgv.setClickable(true);
            imgv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(img != null) {
                        final Dialog dialog = new Dialog(PostInfoActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                        ImageView bigview = new ImageView(PostInfoActivity.this);
                        bigview.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                        bigview.setImageURI(Uri.fromFile(img.get_content()));
                        dialog.setContentView(bigview);
                        dialog.show();
                    }
                }
            });
        }

        public void repaint_unit(img_entity img){
            this.img = img;
            if(img.is_loaded())
                imgv.setImageURI(Uri.fromFile(img.get_content()));
        }
    }

    //commentImageRv的Adapter
    public class CommentImageAdapter extends RecyclerView.Adapter<CommentImageHolder>{
        List<img_entity> imgs;
        public CommentImageAdapter(List<img_entity> imgs){
            this.imgs = imgs;
        }
        @NonNull
        @Override
        public CommentImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(PostInfoActivity.this);
            View view = layoutInflater.inflate(R.layout.recycle_image_unit_layout,parent,false);
            return new CommentImageHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CommentImageHolder holder, int position) {
            img_entity img = imgs.get(position);
            holder.repaint_unit(img);
        }

        @Override
        public int getItemCount() {
            return imgs.size();
        }
    }


}