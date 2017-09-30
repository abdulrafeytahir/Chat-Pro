package com.wartech.chatpro;

public class Contact {
    private String mName;
    private String mPhoneNumber;
    private String mImageURL;

    public Contact(String name, String phoneNumber, String imageURL) {
        this.mName = name;
        this.mPhoneNumber = phoneNumber;
        this.mImageURL = imageURL;
    }

    public String getName() {
        return mName;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public String getImageURL() {
        return mImageURL;
    }
}
