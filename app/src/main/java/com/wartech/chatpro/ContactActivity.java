package com.wartech.chatpro;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.wartech.chatpro.ChatProConstants.CONTACTS;
import static com.wartech.chatpro.ChatProConstants.PROFILE_PIC_URI;
import static com.wartech.chatpro.ChatProConstants.STATUS;
import static com.wartech.chatpro.ChatProConstants.USERNAME;
import static com.wartech.chatpro.ChatProConstants.USERS;
import static com.wartech.chatpro.ChatProConstants.USER_DETAILS;
import static com.wartech.chatpro.SignupActivity.mUserPhoneNumber;

public class ContactActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseRef;

    private ListView listView;
    private ContactAdapter contactAdapter;
    private String TAG = "contact";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_contacts_layout);

        // Initialize Firebase components
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        // attach contacts adapter to listview
        ArrayList<Contact> contacts = new ArrayList<>();
        contactAdapter = new ContactAdapter(this, R.layout.item_contact, contacts);
        listView = findViewById(R.id.contactListView);

        // attach contacts adapter to list view to display contacts
        listView.setAdapter(contactAdapter);

        FloatingActionButton fab = findViewById(R.id.contactActionButton);
        fab.setVisibility(View.GONE);

        // set database listener to display the list of contacts
        attachDatabaseReadListener();

        // initiate chat with friend by clicking on contact number
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(ContactActivity.this, ChatActivity.class);
                intent.putExtra("phoneNumber", contactAdapter.getItem(i).getPhoneNumber());
                startActivity(intent);
            }
        });

    }


    // implementing childEventListener callback methods to update database
    private void attachDatabaseReadListener() {
        contactAdapter.clear();
        mDatabaseRef.child(USERS).child(mUserPhoneNumber).child(CONTACTS).keepSynced(true);
        mDatabaseRef.child(USERS).child(mUserPhoneNumber).child(CONTACTS)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String phoneNumber = dataSnapshot.getKey();
                        getContactUserDetails(phoneNumber);

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

    public void getContactUserDetails(final String phoneNumber) {
        DatabaseReference reference = mDatabaseRef.child(USERS).child(phoneNumber).child(USER_DETAILS);
        reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child(USERNAME).getValue(String.class);
                String imageURL = dataSnapshot.child(PROFILE_PIC_URI).getValue(String.class);
                String status = dataSnapshot.child(STATUS).getValue(String.class);
                Contact contact = new Contact(username, phoneNumber, imageURL, status);
                contactAdapter.add(contact);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}


