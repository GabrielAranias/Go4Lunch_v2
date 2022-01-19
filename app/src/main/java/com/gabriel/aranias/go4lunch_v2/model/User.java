package com.gabriel.aranias.go4lunch_v2.model;

import androidx.annotation.Nullable;

public class User {

    private String uid;
    private String username;
    @Nullable
    private String pictureUrl;

    public User() {
    }

    public User(String uid, String username, @Nullable String pictureUrl) {
        this.uid = uid;
        this.username = username;
        this.pictureUrl = pictureUrl;
    }

    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    @Nullable
    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setPictureUrl(@Nullable String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
}