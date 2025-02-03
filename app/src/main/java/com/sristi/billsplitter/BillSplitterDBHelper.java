package com.sristi.billsplitter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BillSplitterDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "billsplitter.db";
    private static final int DATABASE_VERSION = 1;

    public BillSplitterDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE members (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)");
        db.execSQL("CREATE TABLE expenses (id INTEGER PRIMARY KEY AUTOINCREMENT, description TEXT, amount REAL, payer_id INTEGER)");
        db.execSQL("CREATE TABLE expense_members (id INTEGER PRIMARY KEY AUTOINCREMENT, expense_id INTEGER, member_id INTEGER, share REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS members");
        db.execSQL("DROP TABLE IF EXISTS expenses");
        db.execSQL("DROP TABLE IF EXISTS expense_members");
        onCreate(db);
    }
}