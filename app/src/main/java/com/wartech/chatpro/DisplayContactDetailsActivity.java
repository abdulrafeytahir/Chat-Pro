package com.wartech.chatpro;

import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.R.attr.phoneNumber;
import static com.wartech.chatpro.ChatProConstants.PHONE_NUMBER;
import static com.wartech.chatpro.ChatProConstants.PROFILE_PIC_URI;
import static com.wartech.chatpro.ChatProConstants.STATUS;
import static com.wartech.chatpro.ChatProConstants.USERNAME;


public class DisplayContactDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_contact_details);

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

    }


}
