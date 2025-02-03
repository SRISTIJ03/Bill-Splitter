package com.sristi.billsplitter;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

public class SplitBillsActivity extends AppCompatActivity {
    private BillSplitterDBHelper dbHelper;
    private ArrayList<String> membersList = new ArrayList<>();
    private Spinner payerSpinner;
    private ListView membersListView;
    private EditText descriptionInput, amountInput;
    private RadioGroup splitTypeRadioGroup;
    private RadioButton evenSplitRadio, unevenSplitRadio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_split_bills);

        dbHelper = new BillSplitterDBHelper(this);
        payerSpinner = findViewById(R.id.payerSpinner);
        membersListView = findViewById(R.id.membersListView);
        descriptionInput = findViewById(R.id.descriptionInput);
        amountInput = findViewById(R.id.amountInput);
        splitTypeRadioGroup = findViewById(R.id.splitTypeRadioGroup);
        evenSplitRadio = findViewById(R.id.evenSplitRadio);
        unevenSplitRadio = findViewById(R.id.unevenSplitRadio);
        Button splitButton = findViewById(R.id.splitButton);

        loadMembers();
        setupPayerSpinner();
        setupMembersListView();

        splitButton.setOnClickListener(v -> splitBill());
    }

    private void loadMembers() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, name FROM members", null);
        while (cursor.moveToNext()) {
            membersList.add(cursor.getString(1)); // Add names to the list
        }
        cursor.close();
    }

    private void setupPayerSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, membersList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        payerSpinner.setAdapter(adapter);
    }

    private void setupMembersListView() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, membersList);
        membersListView.setAdapter(adapter);
        membersListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    private void splitBill() {
        // Validate inputs
        String description = descriptionInput.getText().toString().trim();
        if (description.isEmpty()) {
            descriptionInput.setError("Description is required");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountInput.getText().toString());
            if (amount <= 0) {
                throw new NumberFormatException("Amount must be positive");
            }
        } catch (NumberFormatException e) {
            amountInput.setError("Please enter a valid positive amount");
            return;
        }

        int payerPosition = payerSpinner.getSelectedItemPosition();
        // Note: Assuming member IDs are index+1
        int payerId = payerPosition + 1;

        // Prepare list of participants (excluding or including payer based on selection)
        SparseBooleanArray checked = membersListView.getCheckedItemPositions();
        ArrayList<Integer> participantPositions = new ArrayList<>();
        for (int i = 0; i < membersListView.getCount(); i++) {
            if (checked.get(i)) {
                participantPositions.add(i);
            }
        }

        if (participantPositions.isEmpty()) {
            Toast.makeText(this, "Please select at least one participant", Toast.LENGTH_SHORT).show();
            return;
        }

        // Determine split type: Even or Uneven
        int selectedSplitType = splitTypeRadioGroup.getCheckedRadioButtonId();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Start a transaction for database operations
        db.beginTransaction();
        try {
            // Insert expense record
            ContentValues expenseValues = new ContentValues();
            expenseValues.put("description", description);
            expenseValues.put("amount", amount);
            expenseValues.put("payer_id", payerId);
            long expenseId = db.insert("expenses", null, expenseValues);

            if (selectedSplitType == R.id.evenSplitRadio) {
                performEvenSplit(db, expenseId, amount, participantPositions, payerPosition);
            } else if (selectedSplitType == R.id.unevenSplitRadio) {
                performUnevenSplit(db, expenseId, amount, participantPositions, payerPosition);
            }

            // Mark transaction as successful
            db.setTransactionSuccessful();
            Toast.makeText(this, "Bill Split Successfully!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error splitting bill: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            // End the transaction
            db.endTransaction();
        }

        // Clear input fields after successful split
        descriptionInput.setText("");
        amountInput.setText("");
        membersListView.clearChoices();
    }

    private void performEvenSplit(SQLiteDatabase db, long expenseId, double amount,
                                  ArrayList<Integer> participantPositions, int payerPosition) {
        // Determine number of payers (include or exclude payer based on selection)
        int numberOfPayers = participantPositions.size();
        double share = amount / numberOfPayers;

        for (int pos : participantPositions) {
            ContentValues splitValues = new ContentValues();
            splitValues.put("expense_id", expenseId);
            splitValues.put("member_id", pos + 1); // Assuming IDs start from 1
            splitValues.put("share", share);
            db.insert("expense_members", null, splitValues);
        }
    }

    private void performUnevenSplit(SQLiteDatabase db, long expenseId, double amount,
                                    ArrayList<Integer> participantPositions, int payerPosition) {
        // Create a dialog to input percentages
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Payment Percentages");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 10);

        Map<Integer, EditText> editTextMap = new HashMap<>();
        for (int pos : participantPositions) {
            TextView label = new TextView(this);
            label.setText(membersList.get(pos) + " (%):");
            layout.addView(label);

            EditText percentageInput = new EditText(this);
            percentageInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            percentageInput.setHint("e.g., 25");
            layout.addView(percentageInput);
            editTextMap.put(pos, percentageInput);
        }
        builder.setView(layout);

        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dlg -> {
            Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            okButton.setOnClickListener(view -> {
                // Validate percentages
                double totalPercentage = 0;
                Map<Integer, Double> percentages = new HashMap<>();
                boolean valid = true;

                for (Map.Entry<Integer, EditText> entry : editTextMap.entrySet()) {
                    String input = entry.getValue().getText().toString().trim();
                    if (input.isEmpty()) {
                        entry.getValue().setError("Required");
                        valid = false;
                        continue;
                    }

                    try {
                        double perc = Double.parseDouble(input);
                        if (perc < 0 || perc > 100) {
                            entry.getValue().setError("Percentage must be between 0 and 100");
                            valid = false;
                            continue;
                        }
                        percentages.put(entry.getKey(), perc);
                        totalPercentage += perc;
                    } catch (NumberFormatException e) {
                        entry.getValue().setError("Invalid number");
                        valid = false;
                    }
                }

                // Additional validation
                if (!valid) {
                    Toast.makeText(SplitBillsActivity.this, "Please correct errors", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Ensure total percentage is 100
                if (Math.abs(totalPercentage - 100) > 0.001) {
                    Toast.makeText(SplitBillsActivity.this,
                            String.format(Locale.getDefault(),
                                    "Total percentage must equal 100. Currently %.2f",
                                    totalPercentage),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Calculate and store each member's share
                for (Map.Entry<Integer, Double> entry : percentages.entrySet()) {
                    double share = amount * (entry.getValue() / 100.0);
                    ContentValues splitValues = new ContentValues();
                    splitValues.put("expense_id", expenseId);
                    splitValues.put("member_id", entry.getKey() + 1); // Assuming IDs start from 1
                    splitValues.put("share", share);
                    db.insert("expense_members", null, splitValues);
                }

                Toast.makeText(SplitBillsActivity.this, "Bill Split Unevenly Successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });
        dialog.show();
    }
}