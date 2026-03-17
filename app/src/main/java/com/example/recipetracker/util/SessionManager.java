package com.example.recipetracker.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager — Manages user session data using SharedPreferences
 */
public class SessionManager {

    private static final String PREF_NAME = "recipe_tracker_session";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    /**
     * Save user session
     */
    public void createSession(String username) {
        editor.putString(KEY_USERNAME, username);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    /**
     * Get current username
     */
    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, "User");
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Logout user
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }

    /**
     * Set default username if not set
     */
    public void ensureUsername() {
        if (getUsername().equals("User")) {
            createSession("User");
        }
    }
}

