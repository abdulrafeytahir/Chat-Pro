package com.wartech.chatpro;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
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

public class ChatFragment extends Fragment {

    private DatabaseReference mDatabaseRef;

    private ListView listView;
    private ContactAdapter contactAdapter;

    private final String TAG = "Chat";

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize rootVirw component
        View rootView = inflater.inflate(R.layout.fragment_chats_layout, container, false);

        // Initialize Firebase components
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        // attach contacts adapter to listview
        ArrayList<Contact> contacts = new ArrayList<>();
        contactAdapter = new ContactAdapter(getContext(), R.layout.item_contact, contacts);
        listView = rootView.findViewById(R.id.chatListView);

        // attach contacts adapter to list view to display contacts
        listView.setAdapter(contactAdapter);

        // initiate chat with friend by clicking on active chat item
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra("phoneNumber", contactAdapter.getItem(i).getPhoneNumber());
                startActivity(intent);
            }
        });

        // initiate new chat by clicking on floating action button
        FloatingActionButton chatActionButton = rootView.findViewById(R.id.chatActionButton);
        chatActionButton.setVisibility(View.VISIBLE);
        chatActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ContactActivity.class);
                startActivity(intent);
            }
        });

        // attach database listener to populate chat fragment with a list of active chats
        attachDatabaseReadListener();

        return rootView;
    }

    public void attachDatabaseReadListener() {
        contactAdapter.clear();
        mDatabaseRef.child("users").child(mUserPhoneNumber).child("contacts").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // check every contact number of current user
                final String contactNumber = dataSnapshot.getKey();
                if (!TextUtils.isEmpty(contactNumber)) {
                    Log.d(TAG, "current contact number is: " + contactNumber);
                    // implement listener to check if chat_id is set or not
                    mDatabaseRef.child("users").child(mUserPhoneNumber).child("contacts").child(contactNumber)
                            .child("chat_id").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String chatID = dataSnapshot.getValue(String.class);
                            if (!TextUtils.isEmpty(chatID)) {
                                Log.d(TAG, "for contact number: " + contactNumber + " chat id is: " + chatID);

                                // check if chat is initiated, then add contact to adapter
                                getContactUserName(contactNumber);

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
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

    public void getContactUserName(final String phoneNumber) {

        mDatabaseRef.child("users").child(phoneNumber).child("user_details").child("username")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String username = dataSnapshot.getValue(String.class);
                        if (!TextUtils.isEmpty(username)) {
                            Log.d(TAG, "username is: " + username);
                            Contact contact = new Contact(username, phoneNumber, null);
                            contactAdapter.add(contact);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
