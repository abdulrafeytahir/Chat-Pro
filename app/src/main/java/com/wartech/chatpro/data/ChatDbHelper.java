package com.wartech.chatpro.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by user on 06-Aug-17.
 */

public class ChatDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "chatpro.db";


    public ChatDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String SQL_CREATE_USER_DETAILS_TABLE =
                "CREATE TABLE " + ChatContract.UserDetails.TABLE_NAME + " (" +
                        ChatContract.UserDetails.COLUMN_PHONE_NUMBER + " TEXT PRIMARY KEY NOT NULL," +
                        ChatContract.UserDetails.COLUMN_USERNAME + " TEXT NOT NULL," +
                        ChatContract.UserDetails.COLUMN_IMAGE_URL + " TEXT);";

        db.execSQL(SQL_CREATE_USER_DETAILS_TABLE);

        String SQL_CREATE_CONTACTS_TABLE =
                "CREATE TABLE " + ChatContract.Contacts.TABLE_NAME + " (" +
                        ChatContract.Contacts.COLUMN_PHONE_NUMBER + " TEXT PRIMARY KEY NOT NULL," +
                        ChatContract.Contacts.COLUMN_NAME + " TEXT," +
                        ChatContract.Contacts.COLUMN_IMAGE_URL + " TEXT, " +
                        ChatContract.Contacts.COLUMN_CHAT_ID + "TEXT, " +
                        ChatContract.Contacts.COLUMN_LAST_SEEN + "TEXT);";

        db.execSQL(SQL_CREATE_CONTACTS_TABLE);

        String SQL_CREATE_CHATS_TABLE =
                "CREATE TABLE " + ChatContract.Chats.TABLE_NAME + " (" +
                        ChatContract.Chats.COLUMN_CHAT_ID + " TEXT NOT NULL," +
                        ChatContract.Chats.COLUMN_MESSAGE_ID + " TEXT NOT NULL," +
                        ChatContract.Chats.COLUMN_SENDER_NAME + "TEXT NOT NULL," +
                        ChatContract.Chats.COLUMN_TEXT + "TEXT NOT NULL," +
                        ChatContract.Chats.COLUMN_IMAGE_URL + "TEXT," +
                        ChatContract.Chats.COLUMN_TIME + "TEXT NOT NULL," +
                        "FOREIGN KEY (" +  ChatContract.Chats.COLUMN_CHAT_ID + ") " +
                        "REFERENCES " + ChatContract.Contacts.TABLE_NAME + "(" +
                        ChatContract.Contacts.COLUMN_CHAT_ID + "));";

        db.execSQL(SQL_CREATE_CHATS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
//        String SQL_DELETE_USER_DETAILS = "DROP TABLE IF EXISTS " + ChatContract.UserDetails.TABLE_NAME;
//        db.execSQL(SQL_DELETE_USER_DETAILS);

//        String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + ChatContract.UserDetails.TABLE_NAME;
//        db.execSQL(SQL_DELETE_ENTRIES);
//
//        String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + ChatContract.UserDetails.TABLE_NAME;
//        db.execSQL(SQL_DELETE_ENTRIES);
//
//        String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + ChatContract.UserDetails.TABLE_NAME;
//        db.execSQL(SQL_DELETE_ENTRIES);

        onCreate(db);
    }
}
