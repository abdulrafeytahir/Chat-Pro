package com.wartech.chatpro.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import static android.provider.Telephony.Mms.Addr.CONTACT_ID;


/**
 * {@link ContentProvider} for Chat Pro app.
 */
public class ChatProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = ChatProvider.class.getSimpleName();

    /*** Initialize the provider and the database helper object.*/
    private ChatDbHelper mDbHelper;

    // URI matcher code for the content URI for the User Details table
    private static final int USER_DETAILS = 100;

    // URI matcher code for the content URI for the Contacts table
    private static final int CONTACTS = 200;

    // URI matcher code for the content URI for the Chats table
    private static final int CHATS = 300;

    // URI matcher object to match a context URI to a corresponding code.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        /**URI matcher for the whole user_details table*/
        sUriMatcher.addURI(ChatContract.CONTENT_AUTHORITY, ChatContract.PATH_USER_DETAILS, USER_DETAILS);

        /**URI matcher for the whole contacts table*/
        sUriMatcher.addURI(ChatContract.CONTENT_AUTHORITY, ChatContract.PATH_CONTACTS, CONTACTS);


        /**URI matcher for the whole chats table*/
        sUriMatcher.addURI(ChatContract.CONTENT_AUTHORITY, ChatContract.PATH_CHATS, CHATS);

    }

    @Override
    public boolean onCreate() {
        mDbHelper = new ChatDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor = null;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);

        switch (match) {
            case USER_DETAILS:
                cursor = database.query(ChatContract.UserDetails.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case CONTACTS:
                cursor = database.query(ChatContract.Contacts.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case CHATS:
                cursor = database.query(ChatContract.Chats.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        //Set notification URI on the cursor so we know what content URI the cursor was created for
        //If data changes at this URI, then we update the cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // return the cursor
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        // content://com.wartech.chatpro/user_details/923425297771
        switch (match) {
            case USER_DETAILS:
                return insertUserDetails(uri, contentValues);

            case CONTACTS:
                return insertContact(uri, contentValues);

            case CHATS:
                return insertChat(uri, contentValues);

            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    // Insert user details in the database
    private Uri insertUserDetails(Uri uri, ContentValues values) {

        // Check that the phone number is not null
        String phoneNumber = values.getAsString(ChatContract.UserDetails.COLUMN_PHONE_NUMBER);
        if (TextUtils.isEmpty(phoneNumber)) {
            throw new IllegalArgumentException("User details require a phone number");
        }

        // Check that username is not null
        String username = values.getAsString(ChatContract.UserDetails.COLUMN_USERNAME);
        if (TextUtils.isEmpty(username)) {
            throw new IllegalArgumentException("User details require valid username");
        }


        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Insert the new row, returning the primary key value of the new row
        long id = db.insert(ChatContract.UserDetails.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    // Insert a contact entry in the database
    private Uri insertContact(Uri uri, ContentValues values) {

        // Check that the phone number is not null
        String phoneNumber = values.getAsString(ChatContract.Contacts.COLUMN_PHONE_NUMBER);
        if (TextUtils.isEmpty(phoneNumber)) {
            throw new IllegalArgumentException("Contact entry requires a valid phone number");
        }

        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Insert the new row, returning the primary key value of the new row
        long id = db.insert(ChatContract.Contacts.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    // Insert a chat entry in the database
    private Uri insertChat(Uri uri, ContentValues values) {

        // Check that chat id is not null
        String chatId = values.getAsString(ChatContract.Chats.COLUMN_CHAT_ID);
        if (TextUtils.isEmpty(chatId)) {
            throw new IllegalArgumentException("Chat requires a valid chat id");
        }

        // Check that image url is not null
        String messageId = values.getAsString(ChatContract.Chats.COLUMN_MESSAGE_ID);
        if (TextUtils.isEmpty(messageId)) {
            throw new IllegalArgumentException("Chat requires a valid message id");
        }

        // Check that image url is not null
        String senderName = values.getAsString(ChatContract.Chats.COLUMN_SENDER_NAME);
        if (TextUtils.isEmpty(senderName)) {
            throw new IllegalArgumentException("Chat requires a valid sender name");
        }

        // Check that image url is not null
        String text = values.getAsString(ChatContract.Chats.COLUMN_TEXT);
        // Check that image url is not null
        String imageURL = values.getAsString(ChatContract.Chats.COLUMN_IMAGE_URL);

        if (TextUtils.isEmpty(text) && TextUtils.isEmpty(imageURL)) {
            throw new IllegalArgumentException("Chat requires valid text or image");
        }

        // Check that image url is not null
        String time = values.getAsString(ChatContract.Chats.COLUMN_TIME);
        if (TextUtils.isEmpty(time)) {
            throw new IllegalArgumentException("Chat requires a valid time stamp");
        }

        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Insert the new row, returning the primary key value of the new row
        long id = db.insert(ChatContract.Chats.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }


    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        int rowsUpdated = 0;
        switch (match) {
            case USER_DETAILS:
                rowsUpdated = updateUserDetails(uri, contentValues, selection, selectionArgs);
                if (rowsUpdated != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsUpdated;

            case CONTACTS:
                rowsUpdated = updateContacts(uri, contentValues, selection, selectionArgs);
                if (rowsUpdated != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsUpdated;

            case CHATS:
                rowsUpdated = updateChats(uri, contentValues, selection, selectionArgs);
                if (rowsUpdated != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsUpdated;

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }

    }

    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updateUserDetails(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // Check that the phone number is not null
        String phoneNumber = values.getAsString(ChatContract.UserDetails.COLUMN_PHONE_NUMBER);
        if (TextUtils.isEmpty(phoneNumber)) {
            throw new IllegalArgumentException("User details require a phone number");
        }

        // Check that username is not null
        String username = values.getAsString(ChatContract.UserDetails.COLUMN_USERNAME);
        if (TextUtils.isEmpty(username)) {
            throw new IllegalArgumentException("User details require valid username");
        }

        // Check that image url is not null
        String imageURL = values.getAsString(ChatContract.UserDetails.COLUMN_IMAGE_URL);
        if (TextUtils.isEmpty(imageURL)) {
            throw new IllegalArgumentException("User details require valid image");
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Insert the new row, returning the primary key value of the new row
        int noOfRows = db.update(ChatContract.UserDetails.TABLE_NAME, values, selection, selectionArgs);

        if (noOfRows == 0) {
            Log.e(LOG_TAG, "Failed to update user details table " + uri);
            return 0;
        }

        // notify all listeners that the data has changed for the user_details content URI
        getContext().getContentResolver().notifyChange(uri, null);

        return noOfRows;
    }

    private int updateContacts(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Check that the phone number is not null
        String phoneNumber = values.getAsString(ChatContract.Contacts.COLUMN_PHONE_NUMBER);
        if (TextUtils.isEmpty(phoneNumber)) {
            throw new IllegalArgumentException("Contact entry requires a valid phone number");
        }

        // Check that username is not null
        String name = values.getAsString(ChatContract.Contacts.COLUMN_NAME);
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Contact entry requires a valid username");
        }

        // Check that image url is not null
        String imageURL = values.getAsString(ChatContract.Contacts.COLUMN_IMAGE_URL);
        if (TextUtils.isEmpty(imageURL)) {
            throw new IllegalArgumentException("Contact requires a valid image");
        }

        // Check that chat id is not null
        String chatId = values.getAsString(ChatContract.Contacts.COLUMN_CHAT_ID);
        if (TextUtils.isEmpty(chatId)) {
            throw new IllegalArgumentException("Contact requires a valid chat id");
        }

        // Check that image url is not null
        String lastSeen = values.getAsString(ChatContract.Contacts.COLUMN_LAST_SEEN);
        if (TextUtils.isEmpty(lastSeen)) {
            throw new IllegalArgumentException("Contact requires a valid user status");
        }

        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Insert the new row, returning the primary key value of the new row
        int noOfRows = db.update(ChatContract.Contacts.TABLE_NAME, values, selection, selectionArgs);

        if (noOfRows == 0) {
            Log.e(LOG_TAG, "Failed to update contacts table " + uri);
            return 0;
        }

        // notify all listeners that the data has changed for the contacts content URI
        getContext().getContentResolver().notifyChange(uri, null);

        return noOfRows;
    }

    private int updateChats(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // Check that chat id is not null
        String chatId = values.getAsString(ChatContract.Chats.COLUMN_CHAT_ID);
        if (TextUtils.isEmpty(chatId)) {
            throw new IllegalArgumentException("Chat requires a valid chat id");
        }

        // Check that image url is not null
        String messageId = values.getAsString(ChatContract.Chats.COLUMN_MESSAGE_ID);
        if (TextUtils.isEmpty(messageId)) {
            throw new IllegalArgumentException("Chat requires a valid message id");
        }

        // Check that image url is not null
        String senderName = values.getAsString(ChatContract.Chats.COLUMN_SENDER_NAME);
        if (TextUtils.isEmpty(senderName)) {
            throw new IllegalArgumentException("Chat requires a valid sender name");
        }

        // Check that image url is not null
        String text = values.getAsString(ChatContract.Chats.COLUMN_TEXT);
        // Check that image url is not null
        String imageURL = values.getAsString(ChatContract.Chats.COLUMN_IMAGE_URL);

        if (TextUtils.isEmpty(text) && TextUtils.isEmpty(imageURL)) {
            throw new IllegalArgumentException("Chat requires valid text or image");
        }

        // Check that image url is not null
        String time = values.getAsString(ChatContract.Chats.COLUMN_TIME);
        if (TextUtils.isEmpty(time)) {
            throw new IllegalArgumentException("Chat requires a valid time stamp");
        }
        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Insert the new row, returning the primary key value of the new row
        int noOfRows = db.update(ChatContract.Chats.TABLE_NAME, values, selection, selectionArgs);

        if (noOfRows == 0) {
            Log.e(LOG_TAG, "Failed to update chats table " + uri);
            return 0;
        }

        // notify all listeners that the data has changed for the contacts content URI
        getContext().getContentResolver().notifyChange(uri, null);

        return noOfRows;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted = 0;
//        switch (match) {
//            case PETS:
//                // Delete all rows that match the selection and selection args
//                rowsDeleted = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
//                if (rowsDeleted != 0) {
//                    // notify all listeners that the data has changed for the pet content URI
//                    getContext().getContentResolver().notifyChange(uri, null);
//                }
//                return rowsDeleted;
//            case PET_ID:
//                // Delete a single row given by the ID in the URI
//                selection = PetEntry._ID + "=?";
//                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
//                rowsDeleted = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
//                if (rowsDeleted != 0) {
//                    // notify all listeners that the data has changed for the pet content URI
//                    getContext().getContentResolver().notifyChange(uri, null);
//                }
//                return rowsDeleted;
//
//            default:
//                throw new IllegalArgumentException("Deletion is not supported for " + uri);
//        }
        return 0;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case USER_DETAILS:
                return ChatContract.UserDetails.CONTENT_LIST_TYPE;
            case CONTACTS:
                return ChatContract.Contacts.CONTENT_LIST_TYPE;
            case CHATS:
                return ChatContract.Chats.CONTENT_LIST_TYPE;

            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}