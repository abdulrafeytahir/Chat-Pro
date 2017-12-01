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

import static com.wartech.chatpro.ChatProConstants.CHATS;
import static com.wartech.chatpro.ChatProConstants.CHAT_ID;
import static com.wartech.chatpro.ChatProConstants.CONTACTS;
import static com.wartech.chatpro.ChatProConstants.PROFILE_PIC_URI;
import static com.wartech.chatpro.ChatProConstants.STATUS;
import static com.wartech.chatpro.ChatProConstants.USERNAME;
import static com.wartech.chatpro.ChatProConstants.USERS;
import static com.wartech.chatpro.ChatProConstants.USER_DETAILS;
import static com.wartech.chatpro.SignupActivity.mUserPhoneNumber;

public class ChatFragment extends Fragment {

    private DatabaseReference mDatabaseRef;
    private ChildEventListener mChildEventListener;
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

        // attach DB read listener on create
        attachDatabaseReadListener();

        return rootView;
    }

    public void attachDatabaseReadListener() {
        contactAdapter.clear();
        DatabaseReference reference = mDatabaseRef.child(USERS).child(mUserPhoneNumber).child(CONTACTS);
        reference.keepSynced(true);
        reference.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.exists()) {
                            final String contactNumber = dataSnapshot.getKey();
                            if (!TextUtils.isEmpty(contactNumber)) {
                                DatabaseReference ref = mDatabaseRef.child(USERS).child(mUserPhoneNumber)
                                        .child(CONTACTS).child(contactNumber).child(CHAT_ID);
                                ref.keepSynced(true);
                                ref.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String chatID = dataSnapshot.getValue(String.class);
                                        if (!TextUtils.isEmpty(chatID)) {
                                            getContactUserDetails(contactNumber, chatID);
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

    public void getContactUserDetails(final String phoneNumber, final String chatID) {
        DatabaseReference reference = mDatabaseRef.child(USERS).child(phoneNumber).child(USER_DETAILS);
        reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child(USERNAME).getValue(String.class);
                String imageURL = dataSnapshot.child(PROFILE_PIC_URI).getValue(String.class);
                String status = dataSnapshot.child(STATUS).getValue(String.class);

                String chat = getChatMessage(chatID);
                Contact contact = new Contact(username, phoneNumber, imageURL, status);
                contactAdapter.add(contact);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private String getChatMessage(String chatID) {
        final String[] latestMessage = {null};
        DatabaseReference reference = mDatabaseRef.child(CHATS).child(chatID);
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatMessage chatMessage = snapshot.getValue(ChatMessage.class);
                    latestMessage[0] = chatMessage.getText();
                    if(!TextUtils.isEmpty(latestMessage[0])) {
                        break;
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return latestMessage[0];
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
