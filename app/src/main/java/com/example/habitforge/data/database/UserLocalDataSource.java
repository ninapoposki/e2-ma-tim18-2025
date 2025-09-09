package com.example.habitforge.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.example.habitforge.application.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserLocalDataSource {
    private final DatabaseHelper dbHelper;

    public UserLocalDataSource(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    // --- INSERT ---
    public void insertUser(User u) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id", u.getUserId());
        cv.put("email", u.getEmail());
        cv.put("username", u.getUsername());
        cv.put("avatar_url", u.getAvatarUrl());
        db.insert(DatabaseHelper.T_USERS, null, cv);
        db.close();
    }

    // --- GET BY EMAIL ---
    public User getUserByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.T_USERS, null,
                "email = ?", new String[]{email},
                null, null, null);
        User user = null;
        if (c != null && c.moveToFirst()) {
            user = cursorToUser(c);
            c.close();
        }
        db.close();
        return user;
    }

    // --- GET BY ID ---
    public User getUserById(String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.T_USERS, null,
                "id = ?", new String[]{userId},
                null, null, null);
        User user = null;
        if (c != null && c.moveToFirst()) {
            user = cursorToUser(c);
            c.close();
        }
        db.close();
        return user;
    }

    // --- GET ALL USERS ---
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.T_USERS, null,
                null, null, null, null, null);
        if (c != null && c.moveToFirst()) {
            do {
                users.add(cursorToUser(c));
            } while (c.moveToNext());
            c.close();
        }
        db.close();
        return users;
    }

    // --- UPDATE USER ---
    public void updateUser(User u) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("email", u.getEmail());
        cv.put("username", u.getUsername());
        cv.put("avatar_url", u.getAvatarUrl());
        db.update(DatabaseHelper.T_USERS, cv,
                "id = ?", new String[]{u.getUserId()});
        db.close();
    }

    // --- DELETE bez callback-a ---
    public void deleteUser(String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.T_USERS, "id = ?", new String[]{userId});
        db.close();
    }

    // --- DELETE sa callback-om ---
    public void deleteUser(String userId, OnCompleteListener<Void> callback) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.T_USERS, "id = ?", new String[]{userId});
        db.close();
        if (callback != null) {
            callback.onComplete(Tasks.forResult(null));
        }
    }

    // --- HELPER ---
    private User cursorToUser(Cursor c) {
        User u = new User();
        u.setUserId(c.getString(c.getColumnIndexOrThrow("id")));
        u.setEmail(c.getString(c.getColumnIndexOrThrow("email")));
        u.setUsername(c.getString(c.getColumnIndexOrThrow("username")));
        u.setAvatarUrl(c.getString(c.getColumnIndexOrThrow("avatar_url")));
        return u;
    }
}
