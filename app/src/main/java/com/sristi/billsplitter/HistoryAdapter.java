package com.sristi.billsplitter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class HistoryAdapter extends ArrayAdapter<String> {

    // Correct constructor accepting Context and ArrayList
    public HistoryAdapter(Context context, ArrayList<String> historyList) {
        super(context, 0, historyList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the history item at the current position
        String historyItem = getItem(position);

        // Inflate the view if it doesn’t exist
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.history_item, parent, false);
        }

        // Set the history item text to the TextView
        TextView historyTextView = convertView.findViewById(R.id.historyTextView);
        historyTextView.setText(historyItem);

        return convertView;
    }
}
