package com.example.recipetracker.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager — stores the current username in SharedPreferences.
 *
 * Use this to get the username when saving a recipe,
 * and to check if the current user can edit a recipe.
 *
 * Usage:
 *   SessionManager session = new SessionManager(this);
 *   String user = session.getUsername();   // get current user
 *   session.setUsername("Alice");          // set on login
 *   session.clear();                       // on logout
 */
public class SessionManager {

    private static final String PREF_NAME = "RecipeBookSession";
    private static final String KEY_USERNAME = "username";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setUsername(String username) {
        prefs.edit().putString(KEY_USERNAME, username).apply();
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "Guest");
    }

    public boolean isLoggedIn() {
        return !getUsername().equals("Guest");
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
