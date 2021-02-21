package com.example.weboapp.Entity;

import java.sql.Timestamp;

public class CommentRecord {
    private String username;
    private Timestamp date;
    private String content;//内容

    public  CommentRecord(String username,Timestamp date,String content){
        this.username = username;
        this.date = date;
        this.content = content;
    }

    public String getUsername(){
        return this.username;
    }
    public Timestamp getDate(){
        return this.date;
    }
    public String getContent(){
        return this.content;
    }

}
