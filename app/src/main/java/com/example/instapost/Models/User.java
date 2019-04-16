package com.example.instapost.Models;

public class User {
    public String name;
    public String nickName;
    public String email;
    public String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User(){

    }

    public User(String name, String nickName, String email,String password) {
        this.name = name;
        this.nickName = nickName;
        this.email = email;
        this.password = password;
    }
}
