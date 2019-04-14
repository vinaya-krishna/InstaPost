package com.example.instapost.Models;

public class Post {
    private String mOptionalText;
    private String mImageURl;
    private String mHashTag;
    private String mEmail;
    private String mName;

    public Post(){}

    public String getmEmail() {
        return mEmail;
    }

    public void setmEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public Post(String mOptionalText, String mImageURl, String mHashTag, String mEmail, String mName) {
        this.mOptionalText = mOptionalText;
        this.mImageURl = mImageURl;
        this.mHashTag = mHashTag;
        this.mEmail = mEmail;
        this.mName = mName;
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
