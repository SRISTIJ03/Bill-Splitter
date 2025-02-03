package com.sristi.billsplitter;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TrackOwesActivity extends AppCompatActivity {
    private static final String TAG = "TrackOwesActivity";
    private BillSplitterDBHelper dbHelper;
    private ArrayList<String> owesList = new ArrayList<>();
    private OwesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_owes);

        dbHelper = new BillSplitterDBHelper(this);
        ListView owesListView = findViewById(R.id.owesListView);
        adapter = new OwesAdapter(this, owesList);
        owesListView.setAdapter(adapter);

        Button settleButton = findViewById(R.id.settleButton);
        settleButton.setOnClickListener(v -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("DELETE FROM expenses");
            db.execSQL("DELETE FROM expense_members");
            owesList.clear();
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "All balances settled!", Toast.LENGTH_SHORT).show();
        });

        calculateOwes();
    }

    private void calculateOwes() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Ensure we have a safe way to retrieve members
        Cursor membersCursor = null;
        Cursor paidCursor = null;
        Cursor owedCursor = null;

        try {
            // Track total paid and owed for each member
            Map<Integer, MemberFinances> memberFinancesMap = new HashMap<>();

            // Retrieve members safely
            membersCursor = db.rawQuery("SELECT id, name FROM members", null);
            while (membersCursor.moveToNext()) {
                int memberId = membersCursor.getInt(0);
                String memberName = membersCursor.getString(1);

                // Ensure we create a MemberFinances object for every member
                memberFinancesMap.put(memberId, new MemberFinances(memberName));
            }

            // Calculate total paid by each member
            paidCursor = db.rawQuery(
                    "SELECT payer_id, SUM(amount) as total_paid " +
                            "FROM expenses " +
                            "GROUP BY payer_id", null);

            while (paidCursor.moveToNext()) {
                int payerId = paidCursor.getInt(0);
                double totalPaid = paidCursor.getDouble(1);

                // Null-safe update
                MemberFinances finances = memberFinancesMap.get(payerId);
                if (finances != null) {
                    finances.paidAmount = totalPaid;
                } else {
                    Log.w(TAG, "No member found for payer ID: " + payerId);
                }
            }

            // Calculate amounts owed by each member
            owedCursor = db.rawQuery(
                    "SELECT member_id, SUM(share) as total_owed " +
                            "FROM expense_members " +
                            "GROUP BY member_id", null);

            while (owedCursor.moveToNext()) {
                int memberId = owedCursor.getInt(0);
                double totalOwed = owedCursor.getDouble(1);

                // Null-safe update
                MemberFinances finances = memberFinancesMap.get(memberId);
                if (finances != null) {
                    finances.owedAmount = totalOwed;
                } else {
                    Log.w(TAG, "No member found for member ID: " + memberId);
                }
            }

            // Clear previous list and populate with updated information
            owesList.clear();
            for (MemberFinances finances : memberFinancesMap.values()) {
                String formattedInfo = formatMemberFinances(finances);
                owesList.add(formattedInfo);
            }

            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            Log.e(TAG, "Error calculating owes", e);
            Toast.makeText(this, "Error calculating balances", Toast.LENGTH_SHORT).show();
        } finally {
            // Ensure cursors are closed
            if (membersCursor != null) membersCursor.close();
            if (paidCursor != null) paidCursor.close();
            if (owedCursor != null) owedCursor.close();
        }
    }

    private String formatMemberFinances(MemberFinances finances) {
        // Format the financial information with clear breakdown
        return String.format(Locale.getDefault(),
                "%s\nPaid: ₹%.2f | To Pay: ₹%.2f | Net Balance: ₹%.2f",
                finances.name,
                finances.paidAmount,
                finances.owedAmount,
                finances.paidAmount - finances.owedAmount
        );
    }

    // Inner class to track member's financial details
    private static class MemberFinances {
        String name;
        double paidAmount;
        double owedAmount;

        MemberFinances(String name) {
            this.name = name;
            this.paidAmount = 0.0;
            this.owedAmount = 0.0;
        }
    }
}