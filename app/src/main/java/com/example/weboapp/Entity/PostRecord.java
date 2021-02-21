package com.example.weboapp.Entity;

import java.io.Serializable;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

//帖子的记录信息
public class PostRecord implements Serializable {
    public int post_id;
    public String user;
    public String body;
    public Timestamp date;
    public List<img_entity> imgs;
    public Boolean isGood;
    public int GoodNum;
    public int CommentNum;
    public PostRecord transportRecord;
    public int TransportNum;

    public PostRecord(){
        user = "NoName";
        body = "NoContent";
        date = null;
        this.post_id = 0;
        imgs = new ArrayList<>();
        this.isGood = false;
        this.GoodNum = 0;
        this.CommentNum = 0;
        transportRecord = null;
    }

    public PostRecord(String user,String body,Timestamp date,int post_id,int GoodNum,Boolean isGood,int CommentNum,int TransportNum){
        this.body = body;
        this.user = user;
        this.date = date;
        this.post_id = post_id;
        imgs = new ArrayList<>();
        this.isGood = isGood;
        this.GoodNum = GoodNum;
        this.CommentNum = CommentNum;
        this.TransportNum = TransportNum;
        transportRecord = null;
    }

    public PostRecord(String user,String body,Timestamp date){
        /**
         *
         * 转发模式初始化
         *
         */
        imgs = new ArrayList<>();
        this.body = body;
        this.date = date;
        this.user = user;
        transportRecord = null;
    }

    public String getUser(){
        return user;
    }

    public String getBody(){
        return body;
    }

    public Timestamp getDate(){
        return date;
    }

    public int getPost_id(){return this.post_id;}

    public void set_imgs(List<img_entity> imgs){

        this.imgs.clear();
        for(img_entity x:imgs){
            this.imgs.add(x);
        }
    }

    public List<img_entity> get_imgs(){
        return imgs;
    }

    public void setIsGood(Boolean isGood){
        this.isGood = isGood;
    }

    public int getGoodNum(){
        return this.GoodNum;
    }

    public boolean getIsGood(){
        return this.isGood;
    }

    public void setGoodNum(int GoodNum){
        this.GoodNum = GoodNum;
    }

    public int getCommentNum(){
        return this.CommentNum;
    }

    public void setCommentNum(int CommentNum){this.CommentNum = CommentNum;}

    public void setTransportRecord(PostRecord transportRecord) {
        this.transportRecord = transportRecord;
    }

    public PostRecord getTransportRecord(){
        return this.transportRecord;
    }

    public int getTransportNum(){
        return this.TransportNum;
    }
}
