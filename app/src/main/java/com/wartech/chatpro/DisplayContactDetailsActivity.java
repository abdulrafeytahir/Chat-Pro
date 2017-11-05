package com.wartech.chatpro;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.wartech.chatpro.data.ChatContract;

import java.net.URI;

import static android.R.attr.data;
import static com.wartech.chatpro.R.id.contactName;
import static com.wartech.chatpro.R.id.contactNumber;

public class DisplayContactDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Uri mUri;
    TextView contactName;
    TextView contactNumber;
    private int DISPLAY_CONTACT_LOADER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_contact_details);

        Intent intent = getIntent();
        mUri = intent.getData();

        if(mUri != null) {
            // calling initLoader method on Loader Manager
            getLoaderManager().initLoader(DISPLAY_CONTACT_LOADER_ID, null, this);
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                ChatContract.UserDetails.COLUMN_PHONE_NUMBER,
                ChatContract.UserDetails.COLUMN_USERNAME
        };

        return new CursorLoader(this, mUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            contactName = findViewById(R.id.contactName);
            contactNumber = findViewById(R.id.contactNumber);
            contactName.setText(data.getString(data.getColumnIndex(ChatContract.UserDetails.COLUMN_USERNAME)));
            contactNumber.setText(data.getString(data.getColumnIndex(ChatContract.UserDetails.COLUMN_PHONE_NUMBER)));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        contactName.setText("");
        contactNumber.setText("");
    }
}
