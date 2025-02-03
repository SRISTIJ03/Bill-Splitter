package com.sristi.billsplitter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class OwesAdapter extends ArrayAdapter<String> {
    public OwesAdapter(Context context, ArrayList<String> owesList) {
        super(context, 0, owesList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String oweInfo = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.owe_item, parent, false);
        }

        TextView oweTextView = convertView.findViewById(R.id.oweTextView);
        oweTextView.setText(oweInfo);

        return convertView;
    }
}
