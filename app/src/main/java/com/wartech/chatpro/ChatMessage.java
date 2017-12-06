package com.wartech.chatpro;

public class ChatMessage {

    private String messsage_id;
    private String text;
    private String senderName;
    private String photoUrl;
    private String time;
    private String deleteForSender;
    private String deleteForReceiver;

    public ChatMessage() {
    }

    public ChatMessage(String mMesssageId, String text, String senderName, String photoUrl,
                       String time, String isDeleteForSender, String isDeleteForReceiver) {
        this.messsage_id = mMesssageId;
        this.text = text;
        this.senderName = senderName;
        this.photoUrl = photoUrl;
        this.time = time;
        this.deleteForSender = isDeleteForSender;
        this.deleteForReceiver = isDeleteForReceiver;
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

    public String isDeleteForSender() {
        return deleteForSender;
    }

    public String isDeleteForReceiver() {
        return deleteForReceiver;
    }

    public String getMesssage_id() {
        return messsage_id;
    }
}
