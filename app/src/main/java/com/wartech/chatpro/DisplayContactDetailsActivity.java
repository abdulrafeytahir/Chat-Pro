package com.wartech.chatpro;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class DisplayContactDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_contact_details);

        Intent intent = getIntent();

        TextView contactName = findViewById(R.id.contactName);
        TextView contactNumber = findViewById(R.id.contactNumber);

        contactName.setText(intent.getStringExtra("name"));
        contactNumber.setText(intent.getStringExtra("phoneNumber"));
    }
}
