package com.wartech.chatpro;

public class ChatMessage {

    private String text;
    private String senderName;
    private String photoUrl;
    private String time;

    public ChatMessage() {
    }

    public ChatMessage(String text, String name, String photoUrl, String time) {
        this.text = text;
        this.senderName = name;
        this.photoUrl = photoUrl;
        this.time = time;
    }

    public String getText() {
        return text;
    }


    public String getSenderName() {
        return senderName;
    }


    public String getPhotoUrl() {
        return photoUrl;
    }


    public String getTime() {
        return time;
    }

}
