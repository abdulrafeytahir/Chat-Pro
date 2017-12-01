package com.wartech.chatpro;

public class Contact {
    private String mName;
    private String mPhoneNumber;
    private String mImageURL;
    private String mStatus;

    public Contact(String name, String phoneNumber, String imageURL, String mStatus) {
        this.mName = name;
        this.mPhoneNumber = phoneNumber;
        this.mImageURL = imageURL;
        this.mStatus = mStatus;
    }

    public Contact(String mName, String mPhoneNumber, String mImageURL) {
        this.mName = mName;
        this.mPhoneNumber = mPhoneNumber;
        this.mImageURL = mImageURL;
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

    public String getmStatus() {
        return mStatus;
    }
}
