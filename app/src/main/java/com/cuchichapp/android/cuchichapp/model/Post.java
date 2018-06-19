package com.cuchichapp.android.cuchichapp.model;

import android.net.Uri;
import android.provider.ContactsContract;

public class Post {

    public String Img;
    public String Location;
    public String MyPostContent;
    public String MyPostTitle;
    public String User;
    public String uID;

    public Post(String Img
            , String Location
            , String MyPostContent
            , String MyPostTitle
            , String User
            , String uID){
        this.MyPostTitle = MyPostTitle;
        this.MyPostContent = MyPostContent;
        this.Img = Img;
        this.User = User;
        this.Location = Location;
        this.uID = uID;
    }

    public Post(String Img
            , String MyPostContent
            , String MyPostTitle
            , String User
            , String uID){
        this.MyPostTitle = MyPostTitle;
        this.MyPostContent = MyPostContent;
        this.Img = Img;
        this.User = User;
        this.Location = null;
        this.uID = uID;
    }

    public Post(){
        this.MyPostTitle = null;
        this.MyPostContent = null;
        this.Img = null;
        this.User = null;
        this.Location = null;
        this.uID = null;
    }
}
