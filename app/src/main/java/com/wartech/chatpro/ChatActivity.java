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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

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

import static com.wartech.chatpro.SignupActivity.mUserPhoneNumber;


public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "Chats";

    public static final int DEFAULT_MSG_LENGTH_LIMIT = 200;
    private static final int RC_PHOTO_PICKER = 2;
    private String contactPhoneNumber;

    private ChatAdapter mMessageAdapter;
    private EditText mMessageEditText;
    private Button mSendButton;

    public static String mUsername;
    private String mTime = null;
    private String mChatId;

    private DatabaseReference mDatabaseRef;
    private ChildEventListener mChildEventListener;
    private StorageReference mChatPhotosStorageReference;

    public ChatActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();

        if(intent.hasExtra("phoneNumber")) {
            contactPhoneNumber = intent.getStringExtra("phoneNumber");
        } else if (intent.hasExtra("notification phone number")) {
            contactPhoneNumber = intent.getStringExtra("notification phone number");
        }


        // Get database references from Firebase
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mChatPhotosStorageReference = FirebaseStorage.getInstance().getReference().child("chat_photos");

        getUserName();

        ListView mMessageListView = findViewById(R.id.messageListView);
        ImageButton mPhotoPickerButton = findViewById(R.id.photoPickerButton);
        mMessageEditText = findViewById(R.id.messageEditText);
        mSendButton = findViewById(R.id.sendButton);

        // Initialize message ListView and its adapter
        List<ChatMessage> friendlyMessages = new ArrayList<>();
        mMessageAdapter = new ChatAdapter(this, R.layout.item_message, friendlyMessages);
        mMessageListView.setAdapter(mMessageAdapter);


        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
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
        mDatabaseRef.child("users").child(mUserPhoneNumber).child("contacts").child(contactPhoneNumber)
                .child("chat_id").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mChatId = dataSnapshot.getValue(String.class);
                //Chat id is only assigned once when contact's chat id is null
                if (TextUtils.isEmpty(mChatId)) {
                    // set a chat id against this contact number
                    mChatId = mDatabaseRef.child("chats").push().getKey();
                    mDatabaseRef.child("chats").child(mChatId).setValue("");

                    // set chat id for both users if they are chatting for the first time
                    mDatabaseRef.child("users")
                            .child(mUserPhoneNumber).child("contacts")
                            .child(contactPhoneNumber).child("chat_id").setValue(mChatId);

                    mDatabaseRef.child("users")
                            .child(contactPhoneNumber).child("contacts")
                            .child(mUserPhoneNumber).child("chat_id").setValue(mChatId);

                }

                mTime = getTime();
                // setup a friendly message object and push it to the DB
                ChatMessage friendlyMessage = new ChatMessage(mMessageEditText.getText().toString(),
                        mUsername, null, mTime);
                mDatabaseRef.child("chats").child(mChatId).push().setValue(friendlyMessage);
                // Clear input box
                mMessageEditText.setText("");

                if (mChildEventListener == null) {
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
        mDatabaseRef.child("users").child(mUserPhoneNumber).child("user_details").child("username")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String username = dataSnapshot.getValue(String.class);
                        if (!TextUtils.isEmpty(username)) {
                            mUsername = username;
                        } else {
                            mUsername = "Anonymous";
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
        if (mChildEventListener == null) {
            mMessageAdapter.clear();
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    ChatMessage friendlyMessage = dataSnapshot.getValue(ChatMessage.class);
                    mMessageAdapter.add(friendlyMessage);
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

            // listener to check if chat ID exists against this contact number
            mDatabaseRef.child("users").child(mUserPhoneNumber).child("contacts").child(contactPhoneNumber)
                    .child("chat_id").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mChatId = dataSnapshot.getValue(String.class);
                    if (!TextUtils.isEmpty(mChatId)) {
                        mDatabaseRef.child("chats").child(mChatId).addChildEventListener(mChildEventListener);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void detatchMessageReadListener() {
        // removing child listener
        if (mChildEventListener != null && !TextUtils.isEmpty(mChatId)) {
            mDatabaseRef.child("chats").child(mChatId).removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    @Override
    public void onPause() {
        // remove up the listeners if the activity is paused
        super.onPause();
        detatchMessageReadListener();
     //  ReminderUtilities.scheduleChatReminder(ChatActivity.this);
    }

    @Override
    public void onResume() {
        super.onResume();
       // ReminderUtilities.haltJob();
    }
}
