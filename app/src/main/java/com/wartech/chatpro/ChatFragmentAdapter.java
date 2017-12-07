package com.wartech.chatpro;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.wartech.chatpro.ChatProConstants.PHONE_NUMBER;
import static com.wartech.chatpro.ChatProConstants.PROFILE_PIC_URI;
import static com.wartech.chatpro.ChatProConstants.STATUS;
import static com.wartech.chatpro.ChatProConstants.USERNAME;


public class ChatFragmentAdapter extends ArrayAdapter<chatFragmentContact> implements Filterable {

    private TextView nameTextView, messageTextView;
    private CircleImageView profilePicImageView;
    private Context context;
    private ArrayList<chatFragmentContact> contactsList;
    private CustomFilter filter;
    private ArrayList<chatFragmentContact> filterList;
    private String filterText;
    private Boolean flag = true, flag2 = true;
    private int messageLength = 20;

    public ChatFragmentAdapter(Context con, int resource, ArrayList<chatFragmentContact> objects) {
        super(con, resource, objects);
        context = con;
        contactsList = objects;
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
        return contactsList.size();
    }

    @Override
    public chatFragmentContact getItem(int position) {
        return contactsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return contactsList.indexOf(getItem(position));
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_chat_fragment, null);

        }
        nameTextView = convertView.findViewById(R.id.chatNameTextView);
        messageTextView = convertView.findViewById(R.id.chatMessageTextView);

        profilePicImageView = convertView.findViewById(R.id.profilePicImageView);
        if (!TextUtils.isEmpty(contactsList.get(position).getmImageURL())) {
            Picasso.with(profilePicImageView.getContext())
                    .load(contactsList.get(position).getmImageURL())
                    .into(profilePicImageView);

        }

        profilePicImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), DisplayContactDetailsActivity.class);
                intent.putExtra(USERNAME, contactsList.get(position).getmName());
                intent.putExtra(PHONE_NUMBER, contactsList.get(position).getmPhoneNumber());
                intent.putExtra(PROFILE_PIC_URI, contactsList.get(position).getmImageURL());
                intent.putExtra(STATUS, contactsList.get(position).getmStatus());
                getContext().startActivity(intent);
            }
        });

        LinearLayout chatContact = convertView.findViewById(R.id.chat_contact);
        chatContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra("contactName", contactsList.get(position).getmName());
                intent.putExtra("phoneNumber", contactsList.get(position).getmPhoneNumber());
                intent.putExtra("contactImage", contactsList.get(position).getmImageURL());
                getContext().startActivity(intent);
            }
        });

        TextView timeTextView = convertView.findViewById(R.id.chatTimeTextView);
        timeTextView.setText(contactsList.get(position).getmTime());

        if (filterText != null) {
            String itemValue = contactsList.get(position).getmLatestMessage();
            int startPos = itemValue.toLowerCase(Locale.US).indexOf(filterText.toLowerCase(Locale.US));
            int endPos = startPos + filterText.length();

            Spannable spannable;
            if (startPos != -1) // This should always be true, just a sanity check
            {
                spannable = new SpannableString(itemValue);
                ColorStateList blueColor = new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.rgb(0,170,250)});
                TextAppearanceSpan highlightSpan = new TextAppearanceSpan(null, Typeface.BOLD, -1, blueColor, null);

                spannable.setSpan(highlightSpan, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                messageTextView.setText(spannable);

                if (!flag2) {
                    nameTextView.setText(contactsList.get(position).getmName());
                    flag2 = true;
                }

            } else {
                flag = false;

            }

            itemValue = contactsList.get(position).getmName();

            startPos = itemValue.toLowerCase(Locale.US).indexOf(filterText.toLowerCase(Locale.US));
            endPos = startPos + filterText.length();

            if (startPos != -1) // This should always be true, just a sanity check
            {
                spannable = new SpannableString(itemValue);
                ColorStateList blueColor = new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.rgb(0,170,250)});
                TextAppearanceSpan highlightSpan = new TextAppearanceSpan(null, Typeface.BOLD, -1, blueColor, null);

                spannable.setSpan(highlightSpan, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                nameTextView.setText(spannable);

                if (!flag) {
                    truncateMessageAndDisplay(contactsList.get(position).getmLatestMessage());
                    flag = true;
                }

            } else {

                flag2 = false;
                nameTextView.setText(contactsList.get(position).getmName());
            }

        } else {
            truncateMessageAndDisplay(contactsList.get(position).getmLatestMessage());
            nameTextView.setText(contactsList.get(position).getmName());
        }

        return convertView;

    }

    private void truncateMessageAndDisplay(String message) {
        if(message.length() > messageLength) {
            message = message.substring(0, messageLength) + "...";
            messageTextView.setText(message);
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
                ArrayList<chatFragmentContact> filters = new ArrayList<>();

                for (int i = 0; i < filterList.size(); i++) {
                    if (filterList.get(i).getmName().toUpperCase().contains(constraint) ||
                            filterList.get(i).getmLatestMessage().toUpperCase().contains(constraint)) {
                        filters.add(filterList.get(i));

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

            contactsList = (ArrayList<chatFragmentContact>) results.values;
            notifyDataSetChanged();


        }
    }

}



