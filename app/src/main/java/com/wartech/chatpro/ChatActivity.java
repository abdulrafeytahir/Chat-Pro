package com.wartech.chatpro;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.Manifest;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.wartech.chatpro.ChatProConstants.CHATS;
import static com.wartech.chatpro.ChatProConstants.CHAT_ID;
import static com.wartech.chatpro.ChatProConstants.CHAT_PHOTOS;
import static com.wartech.chatpro.ChatProConstants.CONTACTS;
import static com.wartech.chatpro.ChatProConstants.IS_DELETE_FOR_RECEIVER;
import static com.wartech.chatpro.ChatProConstants.IS_DELETE_FOR_SENDER;
import static com.wartech.chatpro.ChatProConstants.LATEST_MESSAGE;
import static com.wartech.chatpro.ChatProConstants.MEDIA_SHARED;
import static com.wartech.chatpro.ChatProConstants.USERNAME;
import static com.wartech.chatpro.ChatProConstants.USERS;
import static com.wartech.chatpro.ChatProConstants.USER_DETAILS;
import static com.wartech.chatpro.ContactActivity.clickedFlag;
import static com.wartech.chatpro.SignupActivity.mUserPhoneNumber;


public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "Chats";

    private boolean deleteButoonPressed = false;
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 200;
    private static final int MY_PERMISSIONS_REQUEST_SHARE_EXTERNAL = 101;
    private static final int RC_PHOTO_PICKER = 2;
    public static String contactPhoneNumber;

    private ChatAdapter mMessageAdapter;
    private ListView mMessageListView;
    private EditText mMessageEditText;
    private Button mSendButton;

    public static String mUsername;
    private String mTime = null;
    private String mChatId;

    private DatabaseReference mDatabaseRef;
    private ChildEventListener mChildEventListener;
    private StorageReference mChatPhotosStorageReference;

    private Menu menuu;
    Integer counter = new Integer(0);
    public static ArrayList<ChatMessage> selectedMessages;
    private int sdk = android.os.Build.VERSION.SDK_INT;
    private static int pos_index = 0;

    public ChatActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setTitle(intent.getStringExtra("contactName"));

        // setChatActioBar(intent.getStringExtra("contactName"), intent.getStringExtra("contactImage"));

        selectedMessages = new ArrayList<>();

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

        mMessageListView = findViewById(R.id.messageListView);
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
                if (mMessageListView.isItemChecked(position)) {
                    pos_index = position;
                    selectedMessages.add(mMessageAdapter.getItem(position));
                    counter = counter + 1;

                } else {
                    counter = counter - 1;
                    if(deleteButoonPressed){
                        deleteButoonPressed = false;
                        selectedMessages.remove(position);
                    }
                }


                if (counter > 1) {

                    menuu.clear();
                    MenuInflater Mymenu2 = mode.getMenuInflater();
                    Mymenu2.inflate(R.menu.menu, menuu);

                }
                if (counter == 1) {
                    boolean isPhoto = mMessageAdapter.getItem(position).getPhotoUrl() != null;
                    boolean isText = mMessageAdapter.getItem(position).getText() != null;
                    if (isPhoto) {
                        menuu.clear();
                        MenuInflater Mymenu2 = mode.getMenuInflater();
                        Mymenu2.inflate(R.menu.imageselected, menuu);
                    }
                    if (isText) {
                        menuu.clear();
                        MenuInflater Mymenu2 = mode.getMenuInflater();
                        Mymenu2.inflate(R.menu.singletextselect, menuu);
                    }

                }

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
                menu.clear();
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.main_menu, menu);

                return ChatActivity.super.onPrepareOptionsMenu(menu);
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_forward:
                        Intent i = new Intent(ChatActivity.this, ContactActivity.class);
                        i.putExtra("isComingFromChatActivity", "1");
                        startActivity(i);

                        break;

                    case R.id.delete_message:
                        deleteMessages();
                        mode.getMenu().clear();
                        deleteButoonPressed = true;
                        mMessageListView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
                        mMessageListView.setBackgroundColor(Color.WHITE);
                        mMessageListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
                        counter = 0;
                        Toast.makeText(ChatActivity.this, "Messages Deleted", Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.cmenu_copy:
                        copyToClipBoard();
                        mMessageListView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
                        mMessageListView.setBackgroundColor(Color.WHITE);
                        mMessageListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
                        break;

                    case R.id.cmenu_share_ext_image:
                        getShareExternalPermission();
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

    private void setChatActioBar(String contactName, String contactImage) {
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);

        Log.d(TAG, contactName + " " + contactImage);
        CircleImageView actionBarImageView = (CircleImageView) findViewById(R.id.action_bar_icon);
        TextView actionBarTitle = (TextView) findViewById(R.id.title_text);
        actionBarTitle.setText(contactName);
        Picasso.with(this)
                .load(contactImage)
                .into(actionBarImageView);

        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.chat_action_bar_layout, null);

        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);

    }

    private void copyToClipBoard() {

        String CopyText = mMessageAdapter.getItem(pos_index).getText();

        if (CopyText.length() != 0) {
            if (sdk < Build.VERSION_CODES.JELLY_BEAN) {

                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(CopyText);
                Toast.makeText(getApplicationContext(), "Text Copied to Clipboard", Toast.LENGTH_SHORT).show();

            } else {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Clip", CopyText);
                Toast.makeText(getApplicationContext(), "Text Copied to Clipboard", Toast.LENGTH_SHORT).show();
                clipboard.setPrimaryClip(clip);
            }
        } else {
            Toast.makeText(getApplicationContext(), "Nothing to Copy", Toast.LENGTH_SHORT).show();

        }


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
                String message = mMessageEditText.getText().toString();
                String messageID = mDatabaseRef.child(CHATS).child(mChatId).push().getKey();
                ChatMessage friendlyMessage = new ChatMessage(messageID, message, mUsername, null, mTime, "", "");
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
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
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
                            final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            Log.d(TAG, "photo added: " + downloadUrl);
                            assert downloadUrl != null;
                            String messageID = mDatabaseRef.child(CHATS).child(mChatId).push().getKey();
                            ChatMessage friendlyMessage = new ChatMessage(messageID, null, mUsername,
                                    downloadUrl.toString(), mTime, "", "");
                            mDatabaseRef.child(CHATS).child(mChatId).child(messageID).setValue(friendlyMessage);
                            mMessageAdapter.add(friendlyMessage);

                            // adding media shared
                            mDatabaseRef.child(USERS).child(mUserPhoneNumber).child(CONTACTS).child(contactPhoneNumber)
                                    .child(MEDIA_SHARED).push().setValue(downloadUrl.toString());

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
                    if (message.getSenderName().equals(mUsername)) {
                        Log.d(TAG, mUsername + ", " + message.getSenderName() + ", " + message.isDeleteForSender());
                        if (TextUtils.isEmpty(message.isDeleteForSender())) {
                            mMessageAdapter.add(message);
                        }
                    } else {
                        if (TextUtils.isEmpty(message.isDeleteForReceiver())) {
                            mMessageAdapter.add(message);
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
        // ReminderUtilities.scheduleChatReminder(ChatActivity.this);
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onPrepareOptionsMenu(menu);
    }

    private void deleteMessages() {
        for (int i = 0; i < selectedMessages.size(); i++) {
            Log.d(TAG, "position size " + selectedMessages.size());
            ChatMessage message = selectedMessages.get(i);
            String messageID = message.getMesssage_id();
            if (message.getSenderName().equals(mUsername)) {
                mDatabaseRef.child(CHATS).child(mChatId).child(messageID).child(IS_DELETE_FOR_SENDER).setValue("true");
            } else {
                mDatabaseRef.child(CHATS).child(mChatId).child(messageID).child(IS_DELETE_FOR_RECEIVER).setValue("true");
            }
            counter--;
        }
        if (counter == 0) {
            Toast.makeText(ChatActivity.this, "Messages Deleted", Toast.LENGTH_SHORT).show();
        }
        for (int i = 0; i < selectedMessages.size(); i++) {
            mMessageAdapter.remove(selectedMessages.get(i));
        }
        for (int i = 0; i < selectedMessages.size(); i++) {
            selectedMessages.remove(i);
        }
    }

    private void getShareExternalPermission() {
        if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission
                (ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_SHARE_EXTERNAL);


        } else {
            shareImagesExternal();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SHARE_EXTERNAL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    shareImagesExternal();
                }

            }
        }

    }

    private void shareImagesExternal() {
        for (int i = 0; i < selectedMessages.size(); i++) {
            if (!TextUtils.isEmpty(selectedMessages.get(i).getPhotoUrl())) {
                Uri bmpUri = Uri.parse(selectedMessages.get(i).getPhotoUrl());
                // Construct a ShareIntent with link to image
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                shareIntent.setType("image/*");
                // Launch sharing dialog for image
                startActivity(Intent.createChooser(shareIntent, "Share Image"));

            } else {
                Toast.makeText(this, "Failed to share Image", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (clickedFlag) {
            clickedFlag = false;
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
}
