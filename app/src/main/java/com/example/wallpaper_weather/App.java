package com.example.wallpaper_weather;

import android.app.Application;
import android.content.Context;

public class App extends Application {

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
    }

    public static Context getAppContext() {
        return sContext;
    }
}
