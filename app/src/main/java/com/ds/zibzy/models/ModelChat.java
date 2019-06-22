package com.ds.zibzy.models;

public class ModelChat {
    String message, sender , reciever, timestamp;
    boolean isSeen;

    public ModelChat() {
    }

    public String getMessage() {
        return message;
    }

    public ModelChat(String message, String sender, String reciever, String timestamp, boolean isSeen) {
        this.message = message;
        this.sender = sender;
        this.reciever = reciever;
        this.timestamp = timestamp;
        this.isSeen = isSeen;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReciever() {
        return reciever;
    }

    public void setReciever(String reciever) {
        this.reciever = reciever;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }
}
