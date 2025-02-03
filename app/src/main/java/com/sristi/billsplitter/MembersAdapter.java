package com.sristi.billsplitter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class MembersAdapter extends ArrayAdapter<String> {
    public MembersAdapter(Context context, ArrayList<String> members) {
        super(context, 0, members);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the member name at this position
        String memberName = getItem(position);

        // If we don't have a recycled view, inflate a new one
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.member_item, parent, false);
        }

        // Set the member name to the TextView
        TextView memberTextView = convertView.findViewById(R.id.memberNameTextView);
        memberTextView.setText(memberName);

        return convertView;
    }
}
