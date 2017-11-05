package com.wartech.chatpro.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import static android.text.style.TtsSpan.GENDER_FEMALE;
import static android.text.style.TtsSpan.GENDER_MALE;

/**
 * Created by user on 06-Aug-17.
 */

public final class ChatContract {

    // Empty constructor
    private ChatContract(){}

    // Users table name in Firebase Database
    public static final String USERS = "users";

    // status for given user
    public static final String LAST_SEEN = "last_seen";

    // Content authority for ChatProvider
    public static final String CONTENT_AUTHORITY = "com.wartech.chatpro";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Path for User Details data (appended to base content URI for possible URI's)
    public static final String PATH_USER_DETAILS = "user_details";

    // Fields for User Details Table
    public static abstract class UserDetails implements BaseColumns {

        /** The content URI to access the pet data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_USER_DETAILS);

        // constants for table name and column names
        public static final String TABLE_NAME = "user_details";

        // The MIME type of the {@link #CONTENT_URI} for user details.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USER_DETAILS;

        // The MIME type of the {@link #CONTENT_URI} for a single pet.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USER_DETAILS;

        public static final String COLUMN_PHONE_NUMBER = "user_phone_number"; //
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_IMAGE_URL = "image";
    }

    // Path for Contact data
    public static final String PATH_CONTACTS = "contacts";

    // Fields for Contacts Table
    public static abstract class Contacts implements BaseColumns {

        /** The content URI to access the pet data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CONTACTS);

        // constants for table name and column names
        public static final String TABLE_NAME = "contacts";

        // The MIME type of the {@link #CONTENT_URI} for a list of contacts.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTACTS;

        // The MIME type of the {@link #CONTENT_URI} for a single contact.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTACTS;

        public static final String COLUMN_PHONE_NUMBER = "contact_phone_number"; //
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_IMAGE_URL = "image_url";
        public static final String COLUMN_CHAT_ID = "chat_id";
        public static final String COLUMN_LAST_SEEN = "last_seen";

    }

    // Path for Contact data
    public static final String PATH_CHATS = "chats";

    // Fields for Contacts Table
    public static abstract class Chats implements BaseColumns {

        /** The content URI to access the pet data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CONTACTS);

        // constants for table name and column names
        public static final String TABLE_NAME = "chats";

        // The MIME type of the {@link #CONTENT_URI} for a list of chats.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CHATS;

        // The MIME type of the {@link #CONTENT_URI} for a single chat.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CHATS;

        public static final String COLUMN_CHAT_ID = "chat_id";
        public static final String COLUMN_MESSAGE_ID = "message_id";
        public static final String COLUMN_SENDER_NAME = "sender_name";
        public static final String COLUMN_TEXT = "text";
        public static final String COLUMN_IMAGE_URL = "image_url";
        public static final String COLUMN_TIME = "time";
    }

}
