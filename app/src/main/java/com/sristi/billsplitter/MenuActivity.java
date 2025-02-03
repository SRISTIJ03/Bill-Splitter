package com.sristi.billsplitter;

import android.os.Bundle;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button addMembersButton = findViewById(R.id.addMembersButton);
        Button splitBillsButton = findViewById(R.id.splitBillsButton);
        Button trackOwesButton = findViewById(R.id.trackOwesButton);

        addMembersButton.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, AddMembersActivity.class)));
        splitBillsButton.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, SplitBillsActivity.class)));
        trackOwesButton.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, TrackOwesActivity.class)));
    }
}