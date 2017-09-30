package com.wartech.chatpro;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class ContactFragment extends Fragment {

    private DatabaseReference mDatabaseRef;
    private ChildEventListener mChildEventListener;

    public ContactFragment() {
        // empty public constructor
    }

    private ListView listView;
    private ContactAdapter contactAdapter;

    private final String TAG = "Contacts";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // initialize fragment rootView
        View rootView = inflater.inflate(R.layout.fragment_contacts_layout, container, false);

        // Initialize Firebase components
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        // attach contacts adapter to listview
        ArrayList<Contact> contacts = new ArrayList<>();
        contactAdapter = new ContactAdapter(getContext(), R.layout.item_contact, contacts);
        listView = rootView.findViewById(R.id.contactListView);

        // attach contacts adapter to list view to display contacts
        listView.setAdapter(contactAdapter);

        // initiate chat with friend
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getContext(), DisplayContactDetailsActivity.class);
                intent.putExtra("phoneNumber", contactAdapter.getItem(i).getPhoneNumber());
                intent.putExtra("name", contactAdapter.getItem(i).getName());
                startActivity(intent);
            }
        });

        // initialize floating action button
        FloatingActionButton fab = rootView.findViewById(R.id.chatActionButton);
        fab.setVisibility(View.VISIBLE);

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        // clean up everything on signout
        detatchDatabaseReadListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        // attach DB read listener on resume
        attachDatabaseReadListener();
    }

    // implementing childEventListener callback methods to update database
    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            contactAdapter.clear();
            mChildEventListener = new ChildEventListener() {
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
            };
            mDatabaseRef.child("users").child(mUserPhoneNumber).child("contacts")
                    .addChildEventListener(mChildEventListener);
        }
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

    private void detatchDatabaseReadListener() {
        // removing database read listener
        if (mChildEventListener != null) {
            mDatabaseRef.child("users").removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

}
