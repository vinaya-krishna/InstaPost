package com.example.instapost.Models;

public class Post {
    private String mOptionalText;
    private String mImageURl;
    private String mHashTag;
    private String mEmail;

    public Post(){}

    public String getmEmail() {
        return mEmail;
    }

    public void setmEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public Post(String mOptionalText, String mImageURl, String mHashTag, String mEmail) {

//        if(mOptionalText.trim().equals(""))
//            mOptionalText = "";
        this.mOptionalText = mOptionalText;
        this.mImageURl = mImageURl;
        this.mHashTag = mHashTag;
        this.mEmail = mEmail;
    }

    public String getmOptionalText() {
        return mOptionalText;
    }

    public void setmOptionalText(String mOptionalText) {
        this.mOptionalText = mOptionalText;
    }

    public String getmImageURl() {
        return mImageURl;
    }

    public void setmImageURl(String mImageURl) {
        this.mImageURl = mImageURl;
    }

    public String getmHashTag() {
        return mHashTag;
    }

    public void setmHashTag(String mHashTag) {
        this.mHashTag = mHashTag;
    }
}