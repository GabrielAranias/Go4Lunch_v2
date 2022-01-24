package com.gabriel.aranias.go4lunch_v2.model;

import androidx.annotation.Nullable;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(uid, user.uid) &&
                Objects.equals(username, user.username) &&
                Objects.equals(pictureUrl, user.pictureUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, username, pictureUrl);
    }
}