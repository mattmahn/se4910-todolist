package com.mahnke.todolist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TodoDatabaseHelper extends SQLiteOpenHelper {

    public static final String TABLE_TODO = "todo";
    public static final String COL_ID = "_id";
    public static final String COL_SUMMARY = "summary";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_DATETIME = "notify_time";
    public static final String COL_STATUS = "status";
    public static final String COL_PRIORITY = "priority";
    public static final String[] ALL_COLS =
            {COL_ID, COL_SUMMARY, COL_DESCRIPTION, COL_DATETIME, COL_STATUS, COL_PRIORITY};

    private static final String DB_CREATE =
            "CREATE TABLE " + TABLE_TODO + "(" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_SUMMARY + " TEXT NOT NULL, " +
            COL_DESCRIPTION + " TEXT NOT NULL, " +
            COL_DATETIME + " INTEGER, " +
            COL_STATUS + " INTEGER, " +
            COL_PRIORITY + " INTEGER" +
            ");";
    private static final String DB_NAME = "todotable.db";
    private static final int DB_VERSION = 22;

    public TodoDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TodoDatabaseHelper.class.getName(),
              "Upgrading from DB version " + oldVersion + " to " + newVersion +
              ", will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODO);
        onCreate(db);
    }
}
