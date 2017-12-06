package com.wartech.chatpro;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.wartech.chatpro.ChatActivity.mUsername;
import static com.wartech.chatpro.ChatActivity.selectedMessages;
import static com.wartech.chatpro.ChatProConstants.CHATS;
import static com.wartech.chatpro.ChatProConstants.CHAT_ID;
import static com.wartech.chatpro.ChatProConstants.CHAT_PHOTOS;
import static com.wartech.chatpro.ChatProConstants.CONTACTS;
import static com.wartech.chatpro.ChatProConstants.LATEST_MESSAGE;
import static com.wartech.chatpro.ChatProConstants.PROFILE_PIC_URI;
import static com.wartech.chatpro.ChatProConstants.STATUS;
import static com.wartech.chatpro.ChatProConstants.USERNAME;
import static com.wartech.chatpro.ChatProConstants.USERS;
import static com.wartech.chatpro.ChatProConstants.USER_DETAILS;
import static com.wartech.chatpro.SignupActivity.mUserPhoneNumber;

public class ContactActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseRef;

    private ListView listView;
    ArrayList<Contact> contacts;
    private ContactAdapter contactAdapter;
    private FloatingActionButton fab;
    private String TAG = "contact";
    private String mTime = null;
    public static boolean clickedFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_contacts_layout);

        // Initialize Firebase components
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        // attach contacts adapter to listview
        contacts = new ArrayList<>();
        contactAdapter = new ContactAdapter(this, R.layout.item_contact, contacts);
        listView = findViewById(R.id.contactListView);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        // attach contacts adapter to list view to display contacts
        listView.setAdapter(contactAdapter);


        // set database listener to display the list of contacts
        attachDatabaseReadListener();

        fab = findViewById(R.id.sendActionButton);

        Intent intent = getIntent();
        if (intent.hasExtra("isComingFromChatActivity")) {

            final int[] pos = new int[1];
            FloatingActionButton fab2 = findViewById(R.id.contactActionButton);
            fab2.setVisibility(View.GONE);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    pos[0] = i;
                    if (!clickedFlag) {
                        fab.setVisibility(View.VISIBLE);
                        view.setBackgroundColor(getResources().getColor(R.color.lightPrimary));
                        String phoneNum = contactAdapter.getItem(i).toString();

                        clickedFlag = true;
                    } else {
                        fab.setVisibility(View.GONE);
                        view.setBackgroundColor(Color.WHITE);
                        clickedFlag = false;

                    }

                }
            });

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    forwardMessages(pos[0]);
                    Contact contact = contactAdapter.getItem(pos[0]);
                    Intent intent = new Intent(ContactActivity.this, ChatActivity.class);
                    intent.putExtra("contactName", contact.getName());
                    intent.putExtra("phoneNumber", contact.getPhoneNumber());
                    startActivity(intent);
                }
            });

        } else {
            FloatingActionButton fab2 = findViewById(R.id.sendActionButton);
            fab2.setVisibility(View.GONE);
            FloatingActionButton fab = findViewById(R.id.contactActionButton);
            fab.setVisibility(View.GONE);

            // initiate chat with friend by clicking on contact number
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Contact contact = contactAdapter.getItem(i);
                    Intent intent = new Intent(ContactActivity.this, ChatActivity.class);
                    intent.putExtra("contactName", contact.getName());
                    intent.putExtra("phoneNumber", contact.getPhoneNumber());
                    startActivity(intent);
                }
            });

        }

    }

    private void forwardMessages(int position) {
        Contact contact = contactAdapter.getItem(position);
        final String contactNumber = contact.getPhoneNumber();
        DatabaseReference reference = mDatabaseRef.child(USERS).child(mUserPhoneNumber)
                .child(CONTACTS).child(contactNumber).child(CHAT_ID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String[] chatId = new String[1];
                chatId[0] = dataSnapshot.getValue(String.class);
                if (!TextUtils.isEmpty(chatId[0])) {
                    // forward all messages in the selectedMessages List
                    for (int i = 0; i < selectedMessages.size(); i++) {
                        sendMessageToContact(contactNumber, chatId, selectedMessages.get(i));
                    }

                    // empty the selected messages list
                    for (int i = 0; i < selectedMessages.size(); i++) {
                        selectedMessages.remove(i);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void sendMessageToContact(final String contactPhoneNumber, final String[] chatId, final ChatMessage chatMessage) {
        DatabaseReference reference = mDatabaseRef.child(USERS).child(mUserPhoneNumber)
                .child(CONTACTS).child(contactPhoneNumber).child(CHAT_ID);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chatId[0] = dataSnapshot.getValue(String.class);
                //Chat id is only assigned once when contact's chat id is null
                if (TextUtils.isEmpty(chatId[0])) {
                    // set a chat id against this contact number
                    chatId[0] = mDatabaseRef.child(CHATS).push().getKey();
                    mDatabaseRef.child(CHATS).child(chatId[0]).setValue("");

                    // set chat id for both users if they are chatting for the first time
                    mDatabaseRef.child(USERS).child(mUserPhoneNumber).child(CONTACTS)
                            .child(contactPhoneNumber).child(CHAT_ID).setValue(chatId);

                    mDatabaseRef.child(USERS).child(contactPhoneNumber).child(CONTACTS)
                            .child(mUserPhoneNumber).child(CHAT_ID).setValue(chatId);

                }

                mTime = getTime();
                // setup a friendly message object and push it to the DB
                String messageID = mDatabaseRef.child(CHATS).child(chatId[0]).push().getKey();
                ChatMessage friendlyMessage = null;
                if (!TextUtils.isEmpty(chatMessage.getText())) {
                    String message = chatMessage.getText();
                    friendlyMessage = new ChatMessage(messageID, message, mUsername, null, mTime, "", "");
                    mDatabaseRef.child(CHATS).child(chatId[0]).child(messageID).setValue(friendlyMessage);

                    mDatabaseRef.child(USERS).child(mUserPhoneNumber).child(CONTACTS).child(contactPhoneNumber)
                            .child(LATEST_MESSAGE).setValue(friendlyMessage);

                    mDatabaseRef.child(USERS).child(contactPhoneNumber).child(CONTACTS).child(mUserPhoneNumber)
                            .child(LATEST_MESSAGE).setValue(friendlyMessage);
                }

                if (!TextUtils.isEmpty(chatMessage.getPhotoUrl())) {
                    Uri selectedImageUri = Uri.parse(chatMessage.getPhotoUrl());
                    StorageReference photoRef = FirebaseStorage.getInstance().getReference()
                            .child(CHAT_PHOTOS).child(chatMessage.getPhotoUrl());
                    // upload file to Firebase Storage
                    photoRef.putFile(selectedImageUri).addOnSuccessListener
                            (ContactActivity.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                    Log.d(TAG, "photo added: " + downloadUrl);
                                    assert downloadUrl != null;
                                    String messageID = mDatabaseRef.child(CHATS).child(chatId[0]).push().getKey();
                                    ChatMessage friendlyMessage = new ChatMessage(messageID, null, mUsername,
                                            downloadUrl.toString(), mTime, "", "");
                                    mDatabaseRef.child(CHATS).child(chatId[0]).child(messageID).setValue(friendlyMessage);
                                }
                            });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public String getTime() {
        return DateFormat.getDateTimeInstance().format(new Date());
    }


    // implementing childEventListener callback methods to update database
    private void attachDatabaseReadListener() {
        // contactAdapter.clear();
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

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) menuItem.getActionView();

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

        return true;
    }


}


