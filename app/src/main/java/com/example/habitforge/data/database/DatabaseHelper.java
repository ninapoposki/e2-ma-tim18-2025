package com.example.habitforge.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "HabitForge.db";
    public static final int DB_VERSION = 2;

    public static final String T_USERS = "users";
    public static final String T_TASKS = "tasks";
    public static final String T_CATEGORIES = "categories";


    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Kreiranje users tabele
        db.execSQL("CREATE TABLE " + T_USERS + " (" +
                "id TEXT PRIMARY KEY, " + // Firebase UID ili generisan ID
                "email TEXT NOT NULL UNIQUE, " +
                "username TEXT NOT NULL UNIQUE, " +
                "avatar_url TEXT" + // moze biti null ako user nema avatar
                ")");
        // Kreiranje categories tabele
        db.execSQL("CREATE TABLE " + T_CATEGORIES + " (" +
                "id TEXT PRIMARY KEY, " +
                "name TEXT NOT NULL UNIQUE, " +
                "color TEXT NOT NULL UNIQUE" +
                ")");

        // Kreiranje tasks tabele
        db.execSQL("CREATE TABLE " + T_TASKS + " (" +
                "id TEXT PRIMARY KEY, " +
                "user_id TEXT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "description TEXT, " +
                "category_id TEXT, " +
                "task_type TEXT, " +
                "recurring_interval INTEGER, " +
                "recurrence_unit TEXT, " +
                "recurring_start INTEGER, " +
                "recurring_end INTEGER, " +
                "execution_time INTEGER, " +
                "difficulty TEXT, " +
                "priority TEXT, " +
                "xp INTEGER, " +
                "status TEXT, " +
                "FOREIGN KEY(user_id) REFERENCES " + T_USERS + "(id) ON DELETE CASCADE," +
                "FOREIGN KEY(category_id) REFERENCES " + T_CATEGORIES + "(id) ON DELETE SET NULL"+
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + T_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + T_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + T_CATEGORIES);
        onCreate(db);
    }
}
