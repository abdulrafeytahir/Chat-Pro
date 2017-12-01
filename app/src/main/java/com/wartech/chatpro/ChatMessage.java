package com.wartech.chatpro;

public class ChatMessage {

    private String messageID;
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

    public ChatMessage(String messageID, String text, String senderName, String photoUrl, String time) {
        this.messageID = messageID;
        this.text = text;
        this.senderName = senderName;
        this.photoUrl = photoUrl;
        this.time = time;
    }

    public String getMessageID() {
        return messageID;
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
