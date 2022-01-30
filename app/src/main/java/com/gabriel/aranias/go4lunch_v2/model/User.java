package com.gabriel.aranias.go4lunch_v2.model;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {

    private String uid;
    private String username;
    @Nullable
    private String pictureUrl;
    @Nullable
    private String lunchSpotId;
    @Nullable
    private String lunchSpotName;

    public User() {
    }

    public User(String uid, String username, @Nullable String pictureUrl,
                @Nullable String lunchSpotId, @Nullable String lunchSpotName) {
        this.uid = uid;
        this.username = username;
        this.pictureUrl = pictureUrl;
        this.lunchSpotId = lunchSpotId;
        this.lunchSpotName = lunchSpotName;
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

    @Nullable
    public String getLunchSpotId() {
        return lunchSpotId;
    }

    public void setLunchSpotId(@Nullable String lunchSpotId) {
        this.lunchSpotId = lunchSpotId;
    }

    @Nullable
    public String getLunchSpotName() {
        return lunchSpotName;
    }

    public void setLunchSpotName(@Nullable String lunchSpotName) {
        this.lunchSpotName = lunchSpotName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(uid, user.uid) &&
                Objects.equals(username, user.username) &&
                Objects.equals(pictureUrl, user.pictureUrl) &&
                Objects.equals(lunchSpotId, user.lunchSpotId) &&
                Objects.equals(lunchSpotName, user.lunchSpotName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, username, pictureUrl, lunchSpotId, lunchSpotName);
    }
}