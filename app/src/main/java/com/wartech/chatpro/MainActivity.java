package com.wartech.chatpro;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wartech.chatpro.data.ChatContract;

import static com.wartech.chatpro.SignupActivity.mUserPhoneNumber;
import static com.wartech.chatpro.data.ChatContract.USERS;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private DatabaseReference mDatabaseRef;
    private ChildEventListener mChildeEventListener;
    private int MAIN_ACTIVITY_LOADER_ID = 0;

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private static final String TAG = "chatpro";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setElevation(0);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        loadMainUI();

        // ReminderUtilities.scheduleChatReminder(this);
    }

    // implementing childEventListener callback methods to update database
    private void attachDatabaseReadListener() {

        if (mChildeEventListener == null) {
            mChildeEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    final String phoneNumber = dataSnapshot.getKey();
                    if (!phoneNumber.equals(mUserPhoneNumber)) {
                        Log.d(TAG, "contact: " + phoneNumber);
                        // see if number is already added in local db
                        boolean ifNumberExistsInDb = ifContactExistsInDb(MainActivity.this, phoneNumber);
                        if (!ifNumberExistsInDb) {
                            // then check if number is present in phonebook
                            boolean ifNumberExistsinPhoneBook = ifContactExistsInPhoneBook(MainActivity.this, phoneNumber);
                            if (ifNumberExistsinPhoneBook) {
                                // save contact details in local database
                                saveContactDetails(phoneNumber);

                                mDatabaseRef.child(USERS).child(mUserPhoneNumber).child(ChatContract.Contacts.TABLE_NAME)
                                        .child(phoneNumber).child(ChatContract.Contacts.COLUMN_CHAT_ID)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                String value = dataSnapshot.getValue(String.class);
                                                if (TextUtils.isEmpty(value)) {
                                                    // save contact in firebase database
                                                    mDatabaseRef.child(USERS).child(mUserPhoneNumber)
                                                            .child(ChatContract.Contacts.TABLE_NAME)
                                                            .child(phoneNumber).child(ChatContract
                                                            .Contacts.COLUMN_CHAT_ID).setValue("");

                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }

                                        });
                            }
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
            };
            mDatabaseRef.child(USERS).addChildEventListener(mChildeEventListener);
        }

    }

    // Get user details for give phone number
    public void saveContactDetails(final String phoneNumber) {
        final String[] username = {null};
        final String[] last_seen = {null};

        mDatabaseRef.child("users").child(phoneNumber).child("user_details").child("username")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                username[0] = dataSnapshot.getValue(String.class);
                if(username[0] == null){
                    throw new NullPointerException("contact name is null");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseRef.child("users").child(phoneNumber).child("user_details").child("last_seen")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        last_seen[0] = dataSnapshot.getValue(String.class);
                        if(last_seen[0] == null){
                            throw new NullPointerException("contact status is null");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        saveContactInDB(this, phoneNumber, username[0], last_seen[0]);
    }


    // Save contact number in local DB
    public void saveContactInDB(Context context, String phoneNumber, String username, String lastSeen) {
        ContentValues values = new ContentValues();
        values.put(ChatContract.Contacts.COLUMN_PHONE_NUMBER, phoneNumber);
        values.put(ChatContract.Contacts.COLUMN_NAME, username);
        values.put(ChatContract.Contacts.COLUMN_LAST_SEEN, lastSeen);

        Uri insertUri = ChatContract.Contacts.CONTENT_URI;
        Uri contactUri = context.getContentResolver().insert(insertUri, values);

        if (contactUri == null) {
            Toast.makeText(this, "Unable to save contact in local DB", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Contact saved!!", Toast.LENGTH_SHORT).show();
        }
    }

    // check if contact number is already added in local DB
    public boolean ifContactExistsInDb(Context context, String number) {

        Uri lookupUri = ChatContract.Contacts.CONTENT_URI;
        Log.d(TAG, "uri: " + lookupUri);
        String[] mPhoneNumberProjection = {ChatContract.Contacts.COLUMN_PHONE_NUMBER};
        String selection = ChatContract.Contacts.COLUMN_PHONE_NUMBER + "=?";
        String[] selectionArgs = new String[]{number};

        Cursor cur = context.getContentResolver().query(lookupUri, mPhoneNumberProjection, selection, selectionArgs, null);
        try {
            if (cur != null && cur.moveToFirst()) {
                return true;
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        return false;
    }

    // Check if contact number exists in user's phone book
    public boolean ifContactExistsInPhoneBook(Context context, String number) {
        /// number is the phone number
        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String[] mPhoneNumberProjection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER,
                ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor cur = context.getContentResolver().query(lookupUri, mPhoneNumberProjection, null, null, null);
        try {
            if (cur != null && cur.moveToFirst()) {
                return true;
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        return false;
    }

    public void loadMainUI() {

        // Get the ViewPager and set its PagerAdapter so that it can display fragments
        ViewPager viewPager = findViewById(R.id.view_pager);
        TabAdapter adapter = new TabAdapter(MainActivity.this, getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        // attach viewPager to TabLayout
        TabLayout tabLayout = findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        // request permission to access contacts
        requestContactsPermission();


    }

    public void requestContactsPermission() {
        // get permission if it is not granted
        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);


        } else {
            // calling initLoader method on Loader Manager
            getLoaderManager().initLoader(MAIN_ACTIVITY_LOADER_ID, null, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // calling initLoader method on Loader Manager
                    getLoaderManager().initLoader(MAIN_ACTIVITY_LOADER_ID, null, this);

                }
            }

        }
    }

    // implement of ladder callback methods
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        attachDatabaseReadListener();
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    // implement FragmentPagerAdapter as a subclass of TabAdapter in MainActivity.class
    public class TabAdapter extends FragmentPagerAdapter {

        private Context mContext;

        public TabAdapter(Context context, FragmentManager fm) {
            super(fm);
            mContext = context;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new ChatFragment();
            } else {
                return new ContactFragment();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return mContext.getString(R.string.chats_tab);
            } else {
                return mContext.getString(R.string.contacts_tab);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    // create a menu for main screen
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    // set up what to do with selected menu option
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.user_details:
                Intent i = new Intent(MainActivity.this, DisplayContactDetailsActivity.class);
                i.setData(ChatContract.UserDetails.CONTENT_URI);
                startActivity(i);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mChildeEventListener != null) {
            mDatabaseRef.child(USERS).removeEventListener(mChildeEventListener);
            mChildeEventListener = null;
        }

    }
}