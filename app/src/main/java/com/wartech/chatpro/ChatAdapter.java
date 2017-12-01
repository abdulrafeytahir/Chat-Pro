package com.wartech.chatpro;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ChatAdapter extends ArrayAdapter<ChatMessage> {
    public ChatAdapter(Context context, int resource, List<ChatMessage> objects) {
        super(context, resource, objects);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_message, parent, false);
        }

        // Initialize message item views
        ImageView photoImageView = convertView.findViewById(R.id.photoImageView);
        TextView messageTextView = convertView.findViewById(R.id.messageTextView);
        TextView authorTextView = convertView.findViewById(R.id.nameTextView);
        TextView timeTextView = convertView.findViewById(R.id.timeTextView);

        // get current message object
        ChatMessage message = getItem(position);

        // if there is an image in the message, hide messageTextView and display photo
        // else hide photoImageView and display text
        if(message != null) {
            boolean isPhoto = message.getPhotoUrl() != null;
            if (isPhoto) {
                messageTextView.setVisibility(View.GONE);
                photoImageView.setVisibility(View.VISIBLE);

                Picasso.with(photoImageView.getContext())
                        .load(message.getPhotoUrl())
                        .into(photoImageView);

            } else {
                messageTextView.setVisibility(View.VISIBLE);
                photoImageView.setVisibility(View.GONE);
                messageTextView.setText(message.getText());
            }
            authorTextView.setText(message.getSenderName());

            timeTextView.setText(message.getTime());

        }

        return convertView;
    }
}
