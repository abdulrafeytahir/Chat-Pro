package com.wartech.chatpro;

/**
 * Created by user on 03-Dec-17.
 */

public class chatFragmentContact {
    private String mName;
    private String mPhoneNumber;
    private String mImageURL;
    private String mStatus;
    private String mLatestMessage;
    private String mTime;

    public chatFragmentContact(String mName, String mPhoneNumber, String mImageURL, String mStatus, String mLatestMessage, String mTime) {
        this.mName = mName;
        this.mPhoneNumber = mPhoneNumber;
        this.mImageURL = mImageURL;
        this.mStatus = mStatus;
        this.mLatestMessage = mLatestMessage;
        this.mTime = mTime;
    }

    public String getmName() {
        return mName;
    }

    public String getmPhoneNumber() {
        return mPhoneNumber;
    }

    public String getmImageURL() {
        return mImageURL;
    }

    public String getmStatus() {
        return mStatus;
    }

    public String getmLatestMessage() {
        return mLatestMessage;
    }

    public String getmTime() {
        return mTime;
    }
}
