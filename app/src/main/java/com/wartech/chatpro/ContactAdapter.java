package com.wartech.chatpro;

import android.content.Context;
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
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


public class ContactAdapter extends ArrayAdapter<Contact> implements Filterable {

    private TextView nameTextView, statusTextView;
    private CircleImageView profilePicImageView;
    private Context context;
    private ArrayList<Contact> contactsList;
    private CustomFilter filter;
    private ArrayList<Contact> filterList;
    private String filterText;
    private Boolean flag = true, flag2 = true;

    public ContactAdapter(Context con, int resource, ArrayList<Contact> objects) {
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
    public Contact getItem(int position) {
        return contactsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return contactsList.indexOf(getItem(position));
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_contact, null);

        }
        nameTextView = convertView.findViewById(R.id.contactNameTextView);
        statusTextView = convertView.findViewById(R.id.contactPhoneNumberTextView);

        if (!TextUtils.isEmpty(contactsList.get(position).getImageURL())) {
            profilePicImageView = convertView.findViewById(R.id.profilePicImageView);
            Picasso.with(profilePicImageView.getContext())
                    .load(contactsList.get(position).getImageURL())
                    .into(profilePicImageView);

        }

        if (filterText != null) {

            String itemValue = contactsList.get(position).getmStatus();
            int startPos = itemValue.toLowerCase(Locale.US).indexOf(filterText.toLowerCase(Locale.US));
            int endPos = startPos + filterText.length();

            Spannable spannable;
            if (startPos != -1) // This should always be true, just a sanity check
            {
                spannable = new SpannableString(itemValue);
                ColorStateList blueColor = new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.rgb(0,170,250)});
                TextAppearanceSpan highlightSpan = new TextAppearanceSpan(null, Typeface.BOLD, -1, blueColor, null);

                spannable.setSpan(highlightSpan, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                statusTextView.setText(spannable);

                if (!flag2) {
                    nameTextView.setText(contactsList.get(position).getName());
                    flag2 = true;
                }

            } else {
                flag = false;

            }

            itemValue = contactsList.get(position).getName();

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
                    statusTextView.setText(contactsList.get(position).getmStatus());
                    flag = true;
                }

            } else {

                flag2 = false;
                nameTextView.setText(contactsList.get(position).getName());
            }

        } else {
            statusTextView.setText(contactsList.get(position).getmStatus());
            nameTextView.setText(contactsList.get(position).getName());
        }

        return convertView;

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
                ArrayList<Contact> filters = new ArrayList<>();

                for (int i = 0; i < filterList.size(); i++) {
                    if (filterList.get(i).getName().toUpperCase().contains(constraint) ||
                            filterList.get(i).getPhoneNumber().toUpperCase().contains(constraint)) {
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

            contactsList = (ArrayList<Contact>) results.values;
            notifyDataSetChanged();


        }
    }

}



