package com.sristi.billsplitter;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class AddMembersActivity extends AppCompatActivity {
    private BillSplitterDBHelper dbHelper;
    private ArrayList<String> membersList = new ArrayList<>();
    private MembersAdapter adapter;
    private String selectedMember = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_members);

        dbHelper = new BillSplitterDBHelper(this);

        EditText nameInput = findViewById(R.id.memberName);
        Button addButton = findViewById(R.id.addMemberButton);
        Button deleteButton = findViewById(R.id.deleteMemberButton);
        ListView membersListView = findViewById(R.id.membersListView);

        adapter = new MembersAdapter(this, membersList);
        membersListView.setAdapter(adapter);

        loadMembersFromDB();  // Load existing members

        // Add member
        addButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            if (!name.isEmpty()) {
                addMemberToDB(name);
                membersList.add(name);
                adapter.notifyDataSetChanged();
                nameInput.setText("");
            }
        });

        // Select member for deletion
        membersListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            selectedMember = membersList.get(position);
            Toast.makeText(this, "Selected: " + selectedMember, Toast.LENGTH_SHORT).show();
        });

        // Delete member
        deleteButton.setOnClickListener(v -> {
            if (selectedMember != null) {
                deleteMemberFromDB(selectedMember);
                membersList.remove(selectedMember);
                adapter.notifyDataSetChanged();
                selectedMember = null;
                Toast.makeText(this, "Member deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Select a member to delete", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMemberToDB(String name) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        db.insert("members", null, values);
    }

    private void deleteMemberFromDB(String name) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("members", "name = ?", new String[]{name});
    }

    private void loadMembersFromDB() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM members", null);
        while (cursor.moveToNext()) {
            membersList.add(cursor.getString(0));
        }
        cursor.close();
    }
}
