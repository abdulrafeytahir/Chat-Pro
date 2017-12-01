package com.wartech.chatpro;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.R.attr.data;
import static com.wartech.chatpro.R.id.photoImageView;
import static com.wartech.chatpro.R.id.profilePicImageView;

public class ContactAdapter extends ArrayAdapter<Contact> {
    public ContactAdapter(Context context, int resource, List<Contact> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_contact, parent, false);
        }

        TextView nameTextView = convertView.findViewById(R.id.contactNameTextView);
        TextView phoneNumberTextView = convertView.findViewById(R.id.contactPhoneNumberTextView);

        Contact contact = getItem(position);

        nameTextView.setText(contact.getName());
        phoneNumberTextView.setText(contact.getPhoneNumber());

        if (!TextUtils.isEmpty(contact.getImageURL())) {
            CircleImageView profilePicImageView = convertView.findViewById(R.id.profilePicImageView);
            Picasso.with(profilePicImageView.getContext())
                    .load(contact.getImageURL())
                    .into(profilePicImageView);

        }

        return convertView;
    }
}