package com.wartech.chatpro;

import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.R.attr.phoneNumber;
import static com.wartech.chatpro.ChatProConstants.CONTACTS;
import static com.wartech.chatpro.ChatProConstants.MEDIA_SHARED;
import static com.wartech.chatpro.ChatProConstants.PHONE_NUMBER;
import static com.wartech.chatpro.ChatProConstants.PROFILE_PIC_URI;
import static com.wartech.chatpro.ChatProConstants.STATUS;
import static com.wartech.chatpro.ChatProConstants.USERNAME;
import static com.wartech.chatpro.ChatProConstants.USERS;
import static com.wartech.chatpro.SignupActivity.mUserPhoneNumber;


public class DisplayContactDetailsActivity extends AppCompatActivity {

    ArrayList<String> mediaSharedUris;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_contact_details);
        mediaSharedUris = new ArrayList<>();

        Intent intent = getIntent();
        String username = intent.getStringExtra(USERNAME);
        String phoneNumber = intent.getStringExtra(PHONE_NUMBER);
        String imageURL = intent.getStringExtra(PROFILE_PIC_URI);
        String status = intent.getStringExtra(STATUS);


        setTitle(username);

        TextView nameTextView = findViewById(R.id.user_name);
        nameTextView.setText(username);

        TextView statusTextView = findViewById(R.id.status);
        statusTextView.setText(status);

        TextView phoneTextView = findViewById(R.id.phone_number);
        phoneTextView.setText(phoneNumber);


        if (!TextUtils.isEmpty(imageURL)) {
            ImageView imageView = findViewById(R.id.user_profile_pic);
            Picasso.with(imageView.getContext())
                    .load(imageURL)
                    .into(imageView);


        }

        ImageView mediSharedOne = findViewById(R.id.media_shared_one);
        ImageView mediSharedTwo = findViewById(R.id.media_shared_two);
        ImageView mediSharedThree = findViewById(R.id.media_shared_three);
        TextView mediaCount = findViewById(R.id.media_count);
        ImageView mediaIcon = findViewById(R.id.media_icon);

        LinearLayout linearLayout = findViewById(R.id.media_images);

        getMediaFromUser(phoneNumber);
        getMediaFromContact(phoneNumber);

        int count = mediaSharedUris.size();
        Log.d("Chats", String.valueOf(count));
        if(count == 0){
            mediaCount.setText(String.valueOf(count));
            linearLayout.setVisibility(View.GONE);
        } else {
            mediaCount.setText(String.valueOf(count));
            linearLayout.setVisibility(View.VISIBLE);

            for(int i = 0 ; i < mediaSharedUris.size(); i++) {
                if(i == 0){
                    Picasso.with(mediSharedOne.getContext())
                            .load(mediaSharedUris.get(i))
                            .into(mediSharedOne);
                }
                if(i == 1){
                    Picasso.with(mediSharedTwo.getContext())
                            .load(mediaSharedUris.get(i))
                            .into(mediSharedTwo);
                }
                if(i == 2){
                    Picasso.with(mediSharedThree.getContext())
                            .load(mediaSharedUris.get(i))
                            .into(mediSharedThree);
                }

            }
        }

    }

    private void getMediaFromContact(String phoneNumber) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child(USERS).child(phoneNumber).child(CONTACTS).child(mUserPhoneNumber)
                .child(MEDIA_SHARED).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

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


    private void getMediaFromUser(String phoneNumber) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child(USERS).child(mUserPhoneNumber).child(CONTACTS).child(phoneNumber)
                .child(MEDIA_SHARED).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String imageUri = dataSnapshot.getValue(String.class);
                if(!TextUtils.isEmpty(imageUri)) {
                    mediaSharedUris.add(imageUri);
                    Log.d("Chats", "image uri: " + imageUri);
                    Log.d("Count", "count: " + String.valueOf(mediaSharedUris.size()));
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
}
