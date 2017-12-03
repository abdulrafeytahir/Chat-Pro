package com.wartech.chatpro;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.media.CamcorderProfile.get;
import static com.wartech.chatpro.R.id.nameTextView;

public class ChatAdapter extends ArrayAdapter<ChatMessage> implements Filterable {

    private TextView messageTextView, authorTextView, timeTextView;
    ImageView photoImageView;
    private Context context;
    private ArrayList<ChatMessage> chatList;
    private CustomFilter filter;
    private ArrayList<ChatMessage> filterList;
    private String filterText;
    private Boolean flag = true, flag2 = true;

    public ChatAdapter(Context con, int resource, ArrayList<ChatMessage> objects) {
        super(con, resource, objects);
        context = con;
        chatList = objects;
        filterList = objects;
    }

    public String getFilterText() {
        return filterText;
    }

    public void setFilterText(String filterText) {
        this.filterText = filterText;
    }

    @Override
    public int getCount() {
        return chatList.size();
    }

    @Override
    public ChatMessage getItem(int position) {
        return chatList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return chatList.indexOf(getItem(position));
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_message, parent, false);
        }

        // Initialize message item views
        photoImageView = convertView.findViewById(R.id.photoImageView);
        messageTextView = convertView.findViewById(R.id.messageTextView);
        authorTextView = convertView.findViewById(nameTextView);
        timeTextView = convertView.findViewById(R.id.timeTextView);

        timeTextView.setText(chatList.get(position).getTime());


        if (filterText != null) {
            String itemValue = chatList.get(position).getText();
            int startPos = -1;
            int endPos = -1;
            if (!TextUtils.isEmpty(itemValue)) {
                startPos = itemValue.toLowerCase(Locale.US).indexOf(filterText.toLowerCase(Locale.US));
                endPos = startPos + filterText.length();
            }

            Spannable spannable;
            if (startPos != -1) // This should always be true, just a sanity check
            {
                spannable = new SpannableString(itemValue);
                ColorStateList blueColor = new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.rgb(0, 170, 250)});
                TextAppearanceSpan highlightSpan = new TextAppearanceSpan(null, Typeface.BOLD, -1, blueColor, null);

                spannable.setSpan(highlightSpan, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                messageTextView.setText(spannable);

                if (!flag2) {
                    authorTextView.setText(chatList.get(position).getSenderName());
                    flag2 = true;
                }

            } else {
                flag = false;

            }

            itemValue = chatList.get(position).getSenderName();

            startPos = itemValue.toLowerCase(Locale.US).indexOf(filterText.toLowerCase(Locale.US));
            endPos = startPos + filterText.length();
            if (startPos != -1) // This should always be true, just a sanity check
            {
                spannable = new SpannableString(itemValue);
                ColorStateList blueColor = new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.rgb(0, 170, 250)});
                TextAppearanceSpan highlightSpan = new TextAppearanceSpan(null, Typeface.BOLD, -1, blueColor, null);

                spannable.setSpan(highlightSpan, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                authorTextView.setText(spannable);

                if (!flag) {
                    setChatMessage(chatList.get(position));
                    flag = true;
                }

            } else {
                flag2 = false;
                authorTextView.setText(chatList.get(position).getSenderName());
            }

        } else {
            setChatMessage(chatList.get(position));
            authorTextView.setText(chatList.get(position).getSenderName());
        }


        return convertView;
    }

    private void setChatMessage(ChatMessage message) {
        // if there is an image in the message, hide messageTextView and display photo
        // else hide photoImageView and display text
        if (message != null) {
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
        }

    }

    @Override
    public Filter getFilter() {

        if (filter == null) {
            filter = new CustomFilter();
        }

        return filter;
    }

    class CustomFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filterText = constraint.toString();
            FilterResults results = new FilterResults();
            if (constraint != null && constraint.length() > 0) {

                constraint = constraint.toString().toUpperCase();
                ArrayList<ChatMessage> filters = new ArrayList<>();

                for (int i = 0; i < filterList.size(); i++) {
                    String text = filterList.get(i).getText();
                    if (!TextUtils.isEmpty(text)) {
                        if (filterList.get(i).getSenderName().toUpperCase().contains(constraint) ||
                                text.toUpperCase().contains(constraint)) {
                            filters.add(filterList.get(i));

                        }
                    }
                }
                results.count = filters.size();
                results.values = filters;
            } else {
                results.count = filterList.size();
                results.values = filterList;

            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            chatList = (ArrayList<ChatMessage>) results.values;
            notifyDataSetChanged();
        }
    }

}