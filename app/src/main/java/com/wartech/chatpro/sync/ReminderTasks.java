package com.wartech.chatpro.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wartech.chatpro.ChatMessage;

import static com.wartech.chatpro.ChatProConstants.CONTACTS;
import static com.wartech.chatpro.ChatProConstants.USERS;
import static com.wartech.chatpro.SignupActivity.mUserPhoneNumber;


public class ReminderTasks {

    public static String ACTION_CHAT_REMINDER = "chat-reminder";
    public static String ACTION_DISMISS_NOTIFICATION = "dismiss-notification";
    private static final String TAG = "Notification";

    private static DatabaseReference mDatabaseRef;

    public static void executeTask(final Context context, String action) {

        Log.d(TAG, "user phone number is: " + mUserPhoneNumber);

        if (ACTION_CHAT_REMINDER.equals(action)) {
            mDatabaseRef = FirebaseDatabase.getInstance().getReference();
            mDatabaseRef.child(USERS).child(mUserPhoneNumber).child(CONTACTS).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    final String contactNumber = dataSnapshot.getKey();
                    if (!TextUtils.isEmpty(contactNumber)) {
                        Log.d(TAG, "contact phone number is: " + contactNumber);
                        String chatID = dataSnapshot.child("chat_id").getValue(String.class);
                        if (!TextUtils.isEmpty(chatID)) {
                            Log.d(TAG, "chat id is: " + chatID);
                            mDatabaseRef.child("chats").child(chatID).addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                    if (!mUserPhoneNumber.equals(contactNumber)) {
                                        Log.d(TAG, "notification sent to: " + contactNumber + " by " + mUserPhoneNumber);
                                        ChatMessage friendlyMessage = dataSnapshot.getValue(ChatMessage.class);

                                        NotificationUtils.chatReminder(context, friendlyMessage.getSenderName(),
                                                contactNumber, friendlyMessage.getText());
                                    }
                                }

                                @Override
                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onChildRemoved(DataSnapshot dataSnapshot) {

                                }

                                @Override
                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else if (ACTION_DISMISS_NOTIFICATION.equals(action)) {
            NotificationUtils.clearAllNotification(context);
        }
    }

}
