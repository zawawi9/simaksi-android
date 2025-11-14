package com.zawww.e_simaksi.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "app_prefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_ACCESS_TOKEN = "accessToken";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    
    private static final String KEY_REFRESH_TOKEN = "refreshToken";
    
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(String accessToken, String userId, String userEmail, String refreshToken) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.apply();
    }

    public void logoutUser() {
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getAccessToken() {
        return pref.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return pref.getString(KEY_REFRESH_TOKEN, null);
    }

    public void updateAccessToken(String newAccessToken) {
        editor.putString(KEY_ACCESS_TOKEN, newAccessToken);
        editor.apply();
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, null);
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, null);
    }

    public void refreshSession(com.zawww.e_simaksi.api.SupabaseAuth.AuthCallback callback) {
        String refreshToken = getRefreshToken();
        if (refreshToken == null) {
            callback.onError("No refresh token available. Please login again.");
            return;
        }

        com.zawww.e_simaksi.api.SupabaseAuth.refreshAccessToken(refreshToken, new com.zawww.e_simaksi.api.SupabaseAuth.AuthCallback() {
            @Override
            public void onSuccess(String newAccessToken, String userId, String newRefreshToken) {
                // The refresh call in SupabaseAuth doesn't return the email, so we reuse the existing one.
                String email = getUserEmail();
                createLoginSession(newAccessToken, userId, email, newRefreshToken);
                callback.onSuccess(newAccessToken, userId, newRefreshToken);
            }

            @Override
            public void onError(String errorMessage) {
                // If refresh fails, the user needs to log in again.
                logoutUser(); // Clear session
                callback.onError(errorMessage);
            }
        });
    }
}