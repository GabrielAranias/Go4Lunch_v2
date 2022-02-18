package com.gabriel.aranias.go4lunch_v2.model;

import java.util.Date;

public class Message {

    public String senderId, receiverId, content, date;
    public Date dateObject;

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getContent() {
        return content;
    }

    public String getDate() {
        return date;
    }

    public Date getDateObject() {
        return dateObject;
    }
}
