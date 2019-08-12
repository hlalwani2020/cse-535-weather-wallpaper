package com.example.wallpaper_weather;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesHelper {

    private static final String PREFERENCES_NAME = "weatherWallpaper";
    private static final String PREFERENCE_REFRESH_RATE = "refreshRate";
    private static final String PREFERENCE_ZIP_CODE = "zipCode";

    public static int getRefreshRateInMinutes() {
        return getSharedPreferences().getInt(PREFERENCE_REFRESH_RATE,
                1 // 1 min
        );
    }

    public static void setRefreshRate(int refreshRateInMinutes) {
        SharedPreferences.Editor editor = getEditor();
        editor.putInt(PREFERENCE_REFRESH_RATE, refreshRateInMinutes);
        editor.commit();
    }

    public static String getZipCode() {
        return getSharedPreferences().getString(PREFERENCE_ZIP_CODE, "");
    }

    public static void setZipCode(String zipCode) {
        SharedPreferences.Editor editor = getEditor();
        editor.putString(PREFERENCE_ZIP_CODE, zipCode);
        editor.commit();
    }

    private static SharedPreferences.Editor getEditor() {
        return getSharedPreferences().edit();
    }

    private static SharedPreferences getSharedPreferences() {
        return App.getAppContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

}
