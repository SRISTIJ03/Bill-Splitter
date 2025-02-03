package com.sristi.billsplitter;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {
    private BillSplitterDBHelper dbHelper;
    private ArrayList<String> historyList = new ArrayList<>();
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        dbHelper = new BillSplitterDBHelper(this);
        ListView historyListView = findViewById(R.id.historyListView);

        // Correct instantiation of the adapter
        adapter = new HistoryAdapter(this, historyList);
        historyListView.setAdapter(adapter);

        loadExpenseHistory();
    }

    private void loadExpenseHistory() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT e.description, e.amount, m.name FROM expenses e " +
                "JOIN members m ON e.payer_id = m.id " +
                "ORDER BY e.id DESC";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() == 0) {
            Toast.makeText(this, "No past bills found.", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                String description = cursor.getString(0);
                double amount = cursor.getDouble(1);
                String payerName = cursor.getString(2);
                String historyItem = "Description: " + description + "\nAmount: ₹" + amount + "\nPaid by: " + payerName;
                historyList.add(historyItem);
            }
            adapter.notifyDataSetChanged();  // Refresh list after loading data
        }
        cursor.close();
    }
}
