package com.example.habitforge.application.session;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.habitforge.application.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SessionManager {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveSession(FirebaseUser firebaseUser) {
        editor.putString(KEY_USER_ID, firebaseUser.getUid());
        editor.putString(KEY_EMAIL, firebaseUser.getEmail());
        editor.apply();
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.contains(KEY_USER_ID);
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }
}
