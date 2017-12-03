package com.wartech.chatpro;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.firebase.storage.UploadTask;
import com.wartech.chatpro.sync.ReminderUtilities;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.R.attr.mode;
import static android.R.attr.x;
import static com.wartech.chatpro.ChatProConstants.CHATS;
import static com.wartech.chatpro.ChatProConstants.CHAT_ID;
import static com.wartech.chatpro.ChatProConstants.CHAT_PHOTOS;
import static com.wartech.chatpro.ChatProConstants.CONTACTS;
import static com.wartech.chatpro.ChatProConstants.LATEST_MESSAGE;
import static com.wartech.chatpro.ChatProConstants.USERNAME;
import static com.wartech.chatpro.ChatProConstants.USERS;
import static com.wartech.chatpro.ChatProConstants.USER_DETAILS;
import static com.wartech.chatpro.SignupActivity.mUserPhoneNumber;


public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "Chats";

    public static final int DEFAULT_MSG_LENGTH_LIMIT = 200;
    private static final int RC_PHOTO_PICKER = 2;
    private String contactPhoneNumber;

    private ChatAdapter mMessageAdapter;
    private EditText mMessageEditText;
    private Button mSendButton;

    private String mUsername;
    private String mTime = null;
    private String mChatId;

    private DatabaseReference mDatabaseRef;
    private ChildEventListener mChildEventListener;
    private StorageReference mChatPhotosStorageReference;

    private Menu menuu;
    Integer counter = new Integer(0);
    private ArrayList<String> deleteMessages = new ArrayList<>();

    public ChatActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        setTitle(intent.getStringExtra("contactName"));

        if (intent.hasExtra("phoneNumber")) {
            contactPhoneNumber = intent.getStringExtra("phoneNumber");
        } else if (intent.hasExtra("notification phone number")) {
            contactPhoneNumber = intent.getStringExtra("notification phone number");
        }


        // Get database references from Firebase
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mChatPhotosStorageReference = FirebaseStorage.getInstance().getReference().child(CHAT_PHOTOS);

        // get username
        getUserName();

        final ListView mMessageListView = findViewById(R.id.messageListView);
        ImageButton mPhotoPickerButton = findViewById(R.id.photoPickerButton);
        mMessageEditText = findViewById(R.id.messageEditText);
        mSendButton = findViewById(R.id.sendButton);

        // Initialize message ListView and its adapter
        final ArrayList<ChatMessage> friendlyMessages = new ArrayList<>();
        mMessageAdapter = new ChatAdapter(this, R.layout.item_message, friendlyMessages);
        mMessageListView.setAdapter(mMessageAdapter);

        mMessageListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);

        mMessageListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                ArrayList<Integer> positions = new ArrayList<Integer>();
                if (mMessageListView.isItemChecked(position)) {
                    positions.add(position);
                    counter = counter + 1;

                } else {
                    counter = counter - 1;
                }


                if (counter > 1) {

                    menuu.clear();
                    MenuInflater Mymenu2 = mode.getMenuInflater();
                    Mymenu2.inflate(R.menu.menu, menuu);


                } else if (counter == 1) {
                    boolean isPhoto = mMessageAdapter.getItem(position).getPhotoUrl() != null;
                    boolean isText = mMessageAdapter.getItem(position).getText() != null;
                    if (isPhoto) {
                        menuu.clear();
                        MenuInflater Mymenu2 = mode.getMenuInflater();
                        Mymenu2.inflate(R.menu.imageselected, menuu);
                    } else if(isText){
                        menuu.clear();
                        MenuInflater Mymenu2 = mode.getMenuInflater();
                        Mymenu2.inflate(R.menu.singletextselect, menuu);
                    }

                }

                deleteMessages.add(friendlyMessages.get(position).getMessageID());
                mode.setTitle(counter.toString());


            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {

                MenuInflater Mymenu1 = mode.getMenuInflater();
                Mymenu1.inflate(R.menu.singletextselect, menu);
                menuu = menu;

                return true;


            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.cmenu_forward:
//                        Intent i = new Intent(ChatActivity.this, Settings.class);
//                        startActivity(i);
                        Toast.makeText(ChatActivity.this, "Forward", Toast.LENGTH_SHORT).show();

                        break;
                    case R.id.cmenu_del:

                        Toast.makeText(ChatActivity.this, "Delete", Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        break;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                counter = 0;
                mMessageListView.clearChoices();
                mMessageAdapter.notifyDataSetChanged();
            }
        });

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RC_PHOTO_PICKER);
            }
        });

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Generate chat key before sending a message
                setChatKeyAndSendMessage();

                getTime();

            }
        });

        attachMessageReadListener();

    }

    public void setChatKeyAndSendMessage() {
        DatabaseReference reference = mDatabaseRef.child(USERS).child(mUserPhoneNumber)
                .child(CONTACTS).child(contactPhoneNumber).child(CHAT_ID);
        reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mChatId = dataSnapshot.getValue(String.class);
                //Chat id is only assigned once when contact's chat id is null
                if (TextUtils.isEmpty(mChatId)) {
                    // set a chat id against this contact number
                    mChatId = mDatabaseRef.child(CHATS).push().getKey();
                    mDatabaseRef.child(CHATS).child(mChatId).setValue("");

                    // set chat id for both users if they are chatting for the first time
                    mDatabaseRef.child(USERS).child(mUserPhoneNumber).child(CONTACTS)
                            .child(contactPhoneNumber).child(CHAT_ID).setValue(mChatId);

                    mDatabaseRef.child(USERS).child(contactPhoneNumber).child(CONTACTS)
                            .child(mUserPhoneNumber).child(CHAT_ID).setValue(mChatId);

                }

                mTime = getTime();
                // setup a friendly message object and push it to the DB
                String messageID = mDatabaseRef.child(CHATS).child(mChatId).push().getKey();
                String message = mMessageEditText.getText().toString();
                ChatMessage friendlyMessage = new ChatMessage(messageID, message, mUsername, null, mTime);
                mDatabaseRef.child(CHATS).child(mChatId).child(messageID).setValue(friendlyMessage);

                mDatabaseRef.child(USERS).child(mUserPhoneNumber).child(CONTACTS).child(contactPhoneNumber)
                        .child(LATEST_MESSAGE).setValue(friendlyMessage);

                mDatabaseRef.child(USERS).child(contactPhoneNumber).child(CONTACTS).child(mUserPhoneNumber)
                        .child(LATEST_MESSAGE).setValue(friendlyMessage);
                // Clear input box
                mMessageEditText.setText("");

                if (mChildEventListener == null) {
                    // attach message read listener
                    attachMessageReadListener();
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

    private void getUserName() {
        DatabaseReference reference = mDatabaseRef.child(USERS).child(mUserPhoneNumber).child(USER_DETAILS).child(USERNAME);
        reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.getValue(String.class);
                if (!TextUtils.isEmpty(username)) {
                    mUsername = username;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // on activity result method for when user picks a photo
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            // get a reference to store file at chat_photos/<FILENAME>
            StorageReference photoRef = mChatPhotosStorageReference.child(selectedImageUri.getLastPathSegment());
            // upload file to Firebase Storage
            photoRef.putFile(selectedImageUri).addOnSuccessListener
                    (this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            Log.d(TAG, "photo added: " + downloadUrl);
                            assert downloadUrl != null;
                            ChatMessage friendlyMessage =
                                    new ChatMessage(null, mUsername, downloadUrl.toString(), mTime);
                            mDatabaseRef.child("chats").child(mChatId).push().setValue(friendlyMessage);
                            mMessageAdapter.add(friendlyMessage);
                        }
                    });
        }
    }

    // implementing childEventListener callback methods to update database
    private void attachMessageReadListener() {
        // listener to check if chat ID exists against this contact number
        if (mChildEventListener == null) {
            mMessageAdapter.clear();
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    ChatMessage message = dataSnapshot.getValue(ChatMessage.class);
                    mMessageAdapter.add(message);
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
            DatabaseReference reference = mDatabaseRef.child(USERS).child(mUserPhoneNumber)
                    .child(CONTACTS).child(contactPhoneNumber).child(CHAT_ID);
            reference.keepSynced(true);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mChatId = dataSnapshot.getValue(String.class);
                    if (!TextUtils.isEmpty(mChatId)) {
                        mDatabaseRef.child(CHATS).child(mChatId).keepSynced(true);
                        mDatabaseRef.child(CHATS).child(mChatId).addChildEventListener(mChildEventListener);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }


    @Override
    public void onPause() {
        // remove up the listeners if the activity is paused
        super.onPause();
        if (mChildEventListener != null && !TextUtils.isEmpty(mChatId)) {
            mDatabaseRef.child(CHATS).child(mChatId).removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
        //  ReminderUtilities.scheduleChatReminder(ChatActivity.this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // ReminderUtilities.haltJob();
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
                mMessageAdapter.setFilterText(newText);
                mMessageAdapter.getFilter().filter(newText);
                return true;
            }
        });

        return true;
    }

}
