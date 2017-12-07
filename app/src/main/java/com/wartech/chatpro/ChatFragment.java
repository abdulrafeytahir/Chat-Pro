package com.wartech.chatpro;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.wartech.chatpro.ChatProConstants.CHATS;
import static com.wartech.chatpro.ChatProConstants.CHAT_ID;
import static com.wartech.chatpro.ChatProConstants.CONTACTS;
import static com.wartech.chatpro.ChatProConstants.LATEST_MESSAGE;
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
    private ChatFragmentAdapter chatFragmentAdapter;
    private ArrayList<String> chats;

    private final String TAG = "Chat";

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        // Initialize rootVirw component
        View rootView = inflater.inflate(R.layout.fragment_chats_layout, container, false);

        // Initialize Firebase components
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        // attach contacts adapter to listview
        ArrayList<chatFragmentContact> contacts = new ArrayList<>();
        chatFragmentAdapter = new ChatFragmentAdapter(getContext(), R.layout.item_chat_fragment, contacts);
        listView = rootView.findViewById(R.id.chatListView);

        // attach contacts adapter to list view to display contacts
        listView.setAdapter(chatFragmentAdapter);

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
      //  attachDatabaseReadListener();

        return rootView;
    }

    public void attachDatabaseReadListener() {
        chatFragmentAdapter.clear();
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
                                    getContactUserDetails(contactNumber);
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

    public void getContactUserDetails(final String phoneNumber) {
        DatabaseReference reference = mDatabaseRef.child(USERS).child(phoneNumber).child(USER_DETAILS);
        reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child(USERNAME).getValue(String.class);
                String imageURL = dataSnapshot.child(PROFILE_PIC_URI).getValue(String.class);
                String status = dataSnapshot.child(STATUS).getValue(String.class);

                setContactItem(username, phoneNumber, imageURL, status);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void setContactItem(final String username, final String phoneNumber, final String imageURL, final String status) {

        DatabaseReference reference = mDatabaseRef.child(USERS).child(mUserPhoneNumber).child(CONTACTS).
                child(phoneNumber).child(LATEST_MESSAGE);
        reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String latestMessage = dataSnapshot.child("text").getValue(String.class);
                    String messageTime = dataSnapshot.child("time").getValue(String.class);
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.US);

                    Date date = null;
                    String messageDate = null;

                    try {
                        date = sdf.parse(messageTime);
                        sdf.applyPattern("dd/MM/yy");
                        messageDate = sdf.format(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    chatFragmentContact contact = new chatFragmentContact(username, phoneNumber, imageURL,
                            status, latestMessage, messageDate);
                    chatFragmentAdapter.add(contact);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error in loading message", Toast.LENGTH_SHORT).show();
            }
        });

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
                chatFragmentAdapter.setFilterText(newText);
                chatFragmentAdapter.getFilter().filter(newText);
                return true;
            }
        });

    }


}
