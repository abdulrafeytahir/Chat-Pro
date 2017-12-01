package com.wartech.chatpro;

import android.content.Context;
import android.content.Intent;
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

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.wartech.chatpro.ChatProConstants.CHAT_ID;
import static com.wartech.chatpro.ChatProConstants.CONTACTS;
import static com.wartech.chatpro.ChatProConstants.USERS;
import static com.wartech.chatpro.SignupActivity.mUserPhoneNumber;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseRef;
    private ChildEventListener mChilEvenListener;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 123;
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
        // attaching child event listener to go the users
        mDatabaseRef.child(USERS).keepSynced(true);
        mDatabaseRef.child(USERS).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final String phoneNumber = dataSnapshot.getKey();
                if (!phoneNumber.equals(mUserPhoneNumber)) {
                    Log.d(TAG, "contact: " + phoneNumber);
                    // see if number is exists in phone book
                    boolean ifNumberExistsinPhoneBook = ifContactExistsInPhoneBook(MainActivity.this, phoneNumber);
                    if (ifNumberExistsinPhoneBook) {
                        mDatabaseRef.child(USERS).child(mUserPhoneNumber).child(CONTACTS).child(phoneNumber)
                                .child(CHAT_ID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String value = dataSnapshot.getValue(String.class);
                                        if (TextUtils.isEmpty(value)) {
                                            // save contact in firebase database
                                            // set the chat id against this contact to null
                                            mDatabaseRef.child(USERS).child(mUserPhoneNumber)
                                                    .child(CONTACTS).child(phoneNumber).child(CHAT_ID).setValue("");

                                        }
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

    }

    // Check if contact number exists in user's phone book
    public boolean ifContactExistsInPhoneBook(Context context, String number) {
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
            attachDatabaseReadListener();
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

                    attachDatabaseReadListener();

                }
            }

        }
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
            case R.id.search:
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

}