package com.example.lakshaysharma.instaclone.DataModels;

public class DBLikes {

    private String user_id;

    public DBLikes() {

    }

    public DBLikes(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    @Override
    public String toString() {
        return "DBLikes{" +
                "user_id='" + user_id + '\'' +
                '}';
    }

}
