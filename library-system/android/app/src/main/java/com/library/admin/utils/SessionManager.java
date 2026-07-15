package com.library.admin.utils;

import android.content.Context;
import android.content.SharedPreferences;

// Manages the logged-in admin's session data stored on the device
// All login info is saved here after and read from here throughout the app
public class SessionManager {

    private static final String PREF_NAME   = "library_prefs";
    private static final String KEY_TOKEN   = "token";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_NAME    = "fullName";
    private static final String KEY_ROLE    = "role";
    private static final String KEY_STATUS  = "status";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager (Context context) {
        prefs   = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor  = prefs.edit();
    }

    // Saves all login data after a succesfull login response
    public void saveSession(String token, String userId, String fullName,
                            String role, String status) {
        editor.putString(KEY_TOKEN,    token);
        editor.putString(KEY_USER_ID,  userId);
        editor.putString(KEY_NAME,     fullName);
        editor.putString(KEY_STATUS,    status);
        editor.apply();
    }

    // Returns true if a token exists - used to skip login screen if already logged in
    public boolean isLoggedIn() {
        return prefs.getString(KEY_TOKEN, null) != null;
    }

    public String getToken()    { return prefs.getString(KEY_TOKEN,   null); }
    public String getUserId()   { return prefs.getString(KEY_USER_ID, null); }
    public String getFullName() { return prefs.getString(KEY_NAME,    null); }
    public String getRole()     { return prefs.getString(KEY_ROLE,    null); }
    public String getStatus()   { return prefs.getString(KEY_STATUS,  null); }

    // Clears all saved session data - called on logout
    public void clearSession() {
        editor.clear().apply();
    }

}
