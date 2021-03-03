package com.example.weboapp.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.weboapp.Adapter.UnitImageAdapter;
import com.example.weboapp.Controls.RoundImageView;
import com.example.weboapp.Entity.PostRecord;
import com.example.weboapp.Entity.img_entity;
import com.example.weboapp.R;
import com.example.weboapp.subActivity.AddPicActivity;
import com.example.weboapp.subActivity.AddTextActivity;
import com.example.weboapp.subActivity.PostInfoActivity;
import com.example.weboapp.subActivity.TransportActivity;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    //用户信息
    public String username;

    //帖子数组
    List<PostRecord> posts;

    //控件信息
    private RecyclerView mHomeRv;//显示首页消息的RecycleView
    private HomeAdapter mHomeAp;//首页消息的RecycleView的Adapter
    private SwipeRefreshLayout mHomeRefresh;//首页消息的刷新布局
    private ImageButton mAddButton;

    @Override
    //初始化
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        username = getActivity().getIntent().getStringExtra("username");
    }

    @Override
    //初始化视图
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_home,container,false);
        //配置add按钮
        mAddButton = (ImageButton) v.findViewById(R.id.home_add_imagebutton);
        mAddButton.setOnClickListener(this);

        //设置刷新按钮
        mHomeRefresh = (SwipeRefreshLayout)v.findViewById(R.id.home_re);
        mHomeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onRefresh() {
                updatePosts();
            }
        });

        //配置Rv
        mHomeRv = (RecyclerView) v.findViewById(R.id.home_rv);
        mHomeRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        if(posts == null){
            posts = new ArrayList<>();
        }
        mHomeAp = new HomeAdapter(posts);
        mHomeRv.setAdapter(mHomeAp);

        //初始化视图
        updatePosts();
        return v;
    }

    //更新当前界面的帖子信息
    private void updatePosts(){
        //启动一个更新线程
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                String url = "http://121.196.149.163:8080/dzwblog/GetPost_server";

                //创建HTTP对象
                HttpPost httpRequset = new HttpPost(url);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));

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

                        JSONArray myjson = new JSONArray(entityStringBuilder.toString());
                        posts.clear();
                        for(int i = 0;i < myjson.length();i++){
                            JSONObject temp_ob = myjson.getJSONObject(i);
                            PostRecord toadd = new PostRecord(temp_ob.getString("post_username"),temp_ob.getString("post_text"), Timestamp.valueOf(temp_ob.getString("post_date")),Integer.parseInt(temp_ob.getString("post_id")),temp_ob.getInt("good_num"),temp_ob.getBoolean("isGood"),temp_ob.getInt("comment_num"),temp_ob.getInt("transport_num"));

                            //处理图片数据
                            int pic_num = temp_ob.getInt("img_num");
                            if(pic_num != 0){
                                img_entity []imgs = new img_entity[pic_num];
                                for(int j = 0;j < pic_num;j++){
                                    String temp_name = temp_ob.getString("img_name"+(j+1));
                                    int temp_id = temp_ob.getInt("img_id"+(j+1));
                                    String temp_format = temp_ob.getString("img_format"+(j+1));
                                    File file = new File("/data/data/com.example.weboapp/files/" + temp_id+temp_format);

                                    imgs[j] = new img_entity(file,temp_name,temp_id);
                                    imgs[j].load_pic();
                                }
                                List<img_entity> list_imgs = new ArrayList<>();
                                for(img_entity x:imgs){
                                    list_imgs.add(x);
                                }
                                toadd.set_imgs(list_imgs);
                            }

                            //获取转发信息
                            boolean IsTransport = temp_ob.getBoolean("isTransport");
                            if(IsTransport){
                                PostRecord TransportRecord = new PostRecord(temp_ob.getString("transport_username"),temp_ob.getString("transport_text"), Timestamp.valueOf(temp_ob.getString("transport_date")));
                                //处理转发图片数据
                                int transport_pic_num = temp_ob.getInt("transport_img_num");
                                if(transport_pic_num != 0){
                                    img_entity []transport_imgs = new img_entity[transport_pic_num];
                                    for(int j = 0;j < transport_pic_num;j++){
                                        String temp_name = temp_ob.getString("transport_img_name"+(j+1));
                                        int temp_id = temp_ob.getInt("transport_img_id"+(j+1));
                                        String temp_format = temp_ob.getString("transport_img_format"+(j+1));
                                        File file = new File("/data/data/com.example.weboapp/files/" + temp_id+temp_format);

                                        transport_imgs[j] = new img_entity(file,temp_name,temp_id);
                                        transport_imgs[j].load_pic();
                                    }
                                    List<img_entity> transpost_list_imgs = new ArrayList<>();
                                    for(img_entity x:transport_imgs){
                                        transpost_list_imgs.add(x);
                                    }
                                    TransportRecord.set_imgs(transpost_list_imgs);
                                }
                                toadd.setTransportRecord(TransportRecord);
                            }

                            posts.add(toadd);

                        }

                    }catch(Exception e){
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Runnable t = new Runnable() {
                    public void run() {
                        mHomeRefresh.setRefreshing(false);
                        mHomeAp.notifyDataSetChanged();
                    }

                };
                getActivity().runOnUiThread(t);


            }
        }).start();
    }

    //mHomeRv的ViewHolder，用于容纳java中的控件（需要与布局中的资源绑定），每个ViewHolder控制一个单元的显示
    private class HomeHolder extends RecyclerView.ViewHolder{
        //定义mHomeRv单元的部件
        public RoundImageView mTitleUserIcon;//首页单元的图标样式
        public TextView mTitleUsername;//首页单元的作者名
        public TextView mBodyTextView;//首页单元的主体文本
        public TextView mDateTextView;//首页单元时间的主体文本
        public Button mTranspostButton;//转发按钮
        public Button mCommentButton;//评论按钮
        public Button mGoodButton;//点赞按钮

        //控件信息
        private RoundImageView transportIcon;
        private TextView transportUsername;
        private TextView transportDate;
        private TextView transportBody;
        private RecyclerView transportImageRv;
        private LinearLayout transportL;
        //Adapter
        private UnitImageAdapter mImageAp;

        //对应的帖子数据
        PostRecord mypost;

        //图片槽控件
        public RecyclerView mUnitImageRv;//首页图片槽的recycleview

        //转发帖子信息
        PostRecord transportPost;

        //监听器
        private class UnitClickListener implements View.OnClickListener{

            static final int PostInfoTag = 1;
            static final int TransportTag = 2;
            static final int CommentTag = 3;
            static final int GoodTag = 4;

            final int tag;

            public UnitClickListener(int tag){
                this.tag = tag;
            }

            public void onClick(View v) {
                switch (this.tag){
                    //查看帖子详细信息的事件
                    case PostInfoTag:
                        Intent PostInfoIntent = new Intent(getActivity(),PostInfoActivity.class);
                        PostInfoIntent.putExtra("mypost",mypost);
                        PostInfoIntent.putExtra("username",username);
                        startActivity(PostInfoIntent);
                        Runnable t1 = new Runnable() {
                            public void run() {
                                mHomeRefresh.setRefreshing(false);
                                mHomeAp.notifyDataSetChanged();
                            }

                        };
                        getActivity().runOnUiThread(t1);
                        break;
                    //转发帖子事件
                    case TransportTag:
                        Intent TransportIntent = new Intent(getActivity(), TransportActivity.class);
                        TransportIntent.putExtra("transport_id",mypost.getPost_id());
                        TransportIntent.putExtra("username",username);
                        startActivity(TransportIntent);
                        //TODO 更新转发量
                        break;
                    //评论按钮事件
                    case CommentTag:
                        Intent PostInfoIntent_ = new Intent(getActivity(),PostInfoActivity.class);
                        PostInfoIntent_.putExtra("mypost",mypost);
                        PostInfoIntent_.putExtra("username",username);
                        startActivity(PostInfoIntent_);
                        Runnable t2 = new Runnable() {
                            public void run() {
                                mHomeRefresh.setRefreshing(false);
                                mHomeAp.notifyDataSetChanged();
                            }

                        };
                        getActivity().runOnUiThread(t2);
                        break;
                    //点赞事件
                    case GoodTag:
                        updateGood();
                        break;
                }
            }
        }

        //初始化函数
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        public HomeHolder(View itemView){

            super(itemView);
            mTitleUsername = (TextView)itemView.findViewById(R.id.home_user_name);
            mTitleUserIcon = (RoundImageView)itemView.findViewById(R.id.home_user_icon);
            mBodyTextView = (TextView)itemView.findViewById(R.id.home_post_body);
            mDateTextView = (TextView)itemView.findViewById(R.id.home_date);
            mTranspostButton = (Button)itemView.findViewById(R.id.home_unit_transport_buttion);
            mCommentButton = (Button)itemView.findViewById(R.id.home_unit_comment_button);
            mGoodButton = (Button)itemView.findViewById(R.id.home_unit_good_unit);

            //转发控件初始化
            this.transportIcon = (RoundImageView)itemView.findViewById(R.id.transport_user_icon);
            this.transportUsername = (TextView)itemView.findViewById(R.id.transport_user_name);
            this.transportDate = (TextView)itemView.findViewById(R.id.transport_date);
            this.transportBody = (TextView)itemView.findViewById(R.id.transport_post_body);
            this.transportImageRv = (RecyclerView)itemView.findViewById(R.id.transport_unit_image_re);
            this.transportL = (LinearLayout)itemView.findViewById(R.id.home_transport_layout);

            //初始化mUnitImageRv
            mUnitImageRv = (RecyclerView) itemView.findViewById(R.id.home_unit_image_re);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mUnitImageRv.setLayoutManager(linearLayoutManager);

            //设置事件
                //打开详细信息
                mBodyTextView.setOnClickListener(new UnitClickListener(UnitClickListener.PostInfoTag));
                //转发
                mTranspostButton.setOnClickListener(new UnitClickListener(UnitClickListener.TransportTag));
                //评论
                mCommentButton.setOnClickListener(new UnitClickListener(UnitClickListener.CommentTag));
                //点赞
                mGoodButton.setOnClickListener(new UnitClickListener(UnitClickListener.GoodTag));


        }


        public void setPost(PostRecord mypost){
            this.mypost = mypost;
            this.transportPost = mypost.getTransportRecord();
        }

        //点赞传送函数
        public void updateGood(){
            //启动一个传送线程
            new Thread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void run() {
                    String url = "http://121.196.149.163:8080/dzwblog/Good_server";
                    //创建HTTP对象
                    HttpPost httpRequset = new HttpPost(url);
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("username", username));
                    params.add(new BasicNameValuePair("post_id", Integer.toString(mypost.getPost_id())));
                    params.add(new BasicNameValuePair("IsCancel", mypost.getIsGood()?"true":"false"));
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
                                JSONObject myjson = new JSONObject(entityStringBuilder.toString());
                                boolean result = Boolean.parseBoolean(myjson.get("result").toString());
                                if(result){
                                    mypost.setGoodNum(mypost.getIsGood()?mypost.getGoodNum()-1:mypost.getGoodNum()+1);
                                    mypost.setIsGood(!mypost.getIsGood());

                                }
                            }catch(Exception e){
                                    e.printStackTrace();
                            }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Runnable t = new Runnable() {
                        public void run() {
                            mHomeRefresh.setRefreshing(false);
                            mHomeAp.notifyDataSetChanged();
                        }

                    };
                    getActivity().runOnUiThread(t);
                }
            }).start();
        }

        //重新绘制函数
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        @SuppressLint("ResourceAsColor")
        public void repaint_unit() {
            mTitleUsername.setText(mypost.getUser());
            mBodyTextView.setText(mypost.getBody());
            Timestamp thisdate = mypost.getDate();
            IconLoader IL1 = new IconLoader(HomeFragment.this);
            IL1.OnBindInfo(mTitleUserIcon,mypost.getUser());
            IL1.setIcon();
            mDateTextView.setText(thisdate.toString().replaceAll("\\.0",""));
            mCommentButton.setText(Integer.toString(mypost.getCommentNum()));
            mTranspostButton.setText(Integer.toString(mypost.getTransportNum()));
            mGoodButton.setText(Integer.toString(mypost.getGoodNum()));
            if (mypost.getIsGood()) {
                Drawable dra = getResources().getDrawable(R.drawable.ic_good_red);
                dra.setBounds(0, 0, 90, 90);
                mGoodButton.setCompoundDrawables(dra, null, null, null);
                mGoodButton.setTextColor(Color.RED);
            } else {
                Drawable dra = getResources().getDrawable(R.drawable.ic_good);
                dra.setBounds(0, 0, 90, 90);
                mGoodButton.setCompoundDrawables(dra, null, null, null);
                mGoodButton.setTextColor(Color.BLACK);
            }
            UnitImageAdapter mUnitImageAp = new UnitImageAdapter(mypost.imgs, getActivity());
            mUnitImageRv.setAdapter(mUnitImageAp);

            if(transportPost != null) {
                //转发信息绘制
                transportL.setVisibility(View.VISIBLE);
                this.transportUsername.setText(transportPost.getUser());
                Timestamp thisdate1 = transportPost.getDate();

                this.transportDate.setText(thisdate1.toString().replaceAll("\\.0",""));
                this.transportBody.setText(transportPost.getBody());
                IconLoader IL2 = new IconLoader(HomeFragment.this);
                IL2.OnBindInfo(this.transportIcon,transportPost.getUser());
                IL2.setIcon();
                //配置转发RV
                LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getActivity());
                linearLayoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
                this.transportImageRv.setLayoutManager(linearLayoutManager1);
                this.mImageAp = new UnitImageAdapter(transportPost.get_imgs(), getActivity());
                this.transportImageRv.setAdapter(this.mImageAp);
            }
            else{
                transportL.setVisibility(View.GONE);
            }
        }
    }

    //mHomeRv的Adapter，对应模型层，保存在内存中的数据，并且对Holder的控件做出一定的变化处理
    private class HomeAdapter extends RecyclerView.Adapter<HomeHolder>{

        private List<PostRecord> mPosts;


        public HomeAdapter(List<PostRecord> mPosts){
            this.mPosts = mPosts;
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public HomeHolder onCreateViewHolder(ViewGroup parent,int viewType){
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.home_unit_layout,parent,false);
            return new HomeHolder(view);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        //创建新的单元
        public void onBindViewHolder(HomeHolder holder,int position){

            PostRecord post = mPosts.get(position);
            holder.setPost(post);
            holder.repaint_unit();
        }

        @Override
        public int getItemCount(){
            return mPosts.size();
        }

    }

    @Override
    //单击事件的处理
    public void onClick(View v) {
            //创建弹出式菜单对象（最低版本11）
            PopupMenu popup = new PopupMenu(this.getActivity(), v);//第二个参数是绑定的那个view
            //获取菜单填充器
            MenuInflater inflater = popup.getMenuInflater();
            //填充菜单
            inflater.inflate(R.menu.home_menu_add_layout, popup.getMenu());
            //绑定菜单项的点击事件
            popup.setOnMenuItemClickListener(this);
            //显示(这一行代码不要忘记了)
            popup.show();
    }

    //弹出式菜单的单击事件处理
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home_menu_add_text://启动纯文本帖子添加activity
                Intent intent_text = new Intent(getActivity(), AddTextActivity.class);	//第二个参数即为执行完跳转后的Activity
                intent_text.putExtra("username",username);
                startActivity(intent_text);
                break;
            case R.id.home_menu_add_pic://启动图片帖子添加activity
                Intent intent_pic = new Intent(getActivity(), AddPicActivity.class);
                intent_pic.putExtra("username",username);
                startActivity(intent_pic);
                break;
            default:
                break;
        }
        return false;
    }

}
