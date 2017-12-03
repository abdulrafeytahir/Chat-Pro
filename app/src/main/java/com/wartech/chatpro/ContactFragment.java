package com.wartech.chatpro;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;
import static com.wartech.chatpro.ChatProConstants.CONTACTS;
import static com.wartech.chatpro.ChatProConstants.PHONE_NUMBER;
import static com.wartech.chatpro.ChatProConstants.PROFILE_PIC_URI;
import static com.wartech.chatpro.ChatProConstants.STATUS;
import static com.wartech.chatpro.ChatProConstants.USERNAME;
import static com.wartech.chatpro.ChatProConstants.USERS;
import static com.wartech.chatpro.ChatProConstants.USER_DETAILS;
import static com.wartech.chatpro.SignupActivity.mUserPhoneNumber;

public class ContactFragment extends Fragment {

    private DatabaseReference mDatabaseRef;
    private ListView listView;
    private ContactAdapter contactAdapter;
    private ChildEventListener mChildEventListener;
    private static final int RC_PICK_CONTACT = 999;

    public ContactFragment() {
        // empty public constructor
    }


    private final String TAG = "chatpro";

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        // initialize fragment rootView
        View rootView = inflater.inflate(R.layout.fragment_contacts_layout, container, false);

        // Initialize Firebase components
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        // initializing contacts array list
        ArrayList<Contact> contacts = new ArrayList<>();

        // initializing contact adapter and assigning contacts list to it
        contactAdapter = new ContactAdapter(getContext(), R.layout.item_contact, contacts);

        // setting up listview
        listView = rootView.findViewById(R.id.contactListView);

        // attach contacts adapter to list view to display contacts
        listView.setAdapter(contactAdapter);

        // initiate chat with friend
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getContext(), DisplayContactDetailsActivity.class);
                intent.putExtra(USERNAME, contactAdapter.getItem(i).getName());
                intent.putExtra(PHONE_NUMBER, contactAdapter.getItem(i).getPhoneNumber());
                intent.putExtra(PROFILE_PIC_URI, contactAdapter.getItem(i).getImageURL());
                intent.putExtra(STATUS, contactAdapter.getItem(i).getmStatus());
                startActivity(intent);
            }
        });

        // initialize floating action button
        FloatingActionButton fab = rootView.findViewById(R.id.contactActionButton);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                startActivityForResult(intent, RC_PICK_CONTACT);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // attach DB read listener on create
        attachDatabaseReadListener();

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mChildEventListener != null) {
            mDatabaseRef.child(USERS).child(mUserPhoneNumber).child(CONTACTS)
                    .removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    // implementing childEventListener callback methods to update database
    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            contactAdapter.clear();
            mChildEventListener = new ChildEventListener() {
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
            };

            DatabaseReference reference = mDatabaseRef.child(USERS).child(mUserPhoneNumber).child(CONTACTS);
            reference.keepSynced(true);
            reference.addChildEventListener(mChildEventListener);
        }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PICK_CONTACT && resultCode == RESULT_OK) {
            Toast.makeText(getContext(), "Contact Added Successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = new SearchView(((MainActivity) getContext()).getSupportActionBar().getThemedContext());

        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        MenuItemCompat.setActionView(item, searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                contactAdapter.setFilterText(newText);
                contactAdapter.getFilter().filter(newText);
                return true;
            }
        });

    }


}
