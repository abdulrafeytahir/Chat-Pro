package com.wartech.chatpro;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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

import static com.wartech.chatpro.SignupActivity.mUserPhoneNumber;

public class ContactActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseRef;

    private ListView listView;
    private ContactAdapter contactAdapter;

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

        FloatingActionButton fab = findViewById(R.id.chatActionButton);
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

    @Override
    public void onPause() { super.onPause(); }

    @Override
    public void onResume() { super.onResume(); }

    // implementing childEventListener callback methods to update database
    private void attachDatabaseReadListener() {
        contactAdapter.clear();
        mDatabaseRef.child("users").child(mUserPhoneNumber).child("contacts")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String phoneNumber = dataSnapshot.getKey();
                        getContactUserName(phoneNumber);
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


    public void getContactUserName(final String phoneNumber) {

        mDatabaseRef.child("users").child(phoneNumber).child("user_details").child("username")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String username = dataSnapshot.getValue(String.class);
                        if (!TextUtils.isEmpty(username)) {
                            Contact contact = new Contact(username, phoneNumber, null);
                            contactAdapter.add(contact);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

}
