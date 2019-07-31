package com.example.wallpaper_weather;

import androidx.appcompat.app.AppCompatActivity;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;

public class MainActivity extends AppCompatActivity {
    ImageButton androidThumbsUpButton;
    ImageButton androidThumbsDownButton;
    ImageButton androidSettingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        androidThumbsUpButton = (ImageButton)findViewById(R.id.image_button_thumbsup);
        androidThumbsUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v1) {

            }
        });

        androidThumbsDownButton = (ImageButton)findViewById(R.id.image_button_thumbsdown);
        androidThumbsDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v2) {

            }
        });
        androidSettingsButton = (ImageButton)findViewById(R.id.image_button_settings);
        androidSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v3) {

            }
        });

        setWallpaper();

    }

    public void setWallpaper() {
        // Locate ImageView in activity_main.xml
        //ImageView mywallpaper = (ImageView) findViewById(R.id.wallpaper);

        // Attach image into ImageView
        //mywallpaper.setImageResource(R.drawable.wallpaper);

        // Retrieve a WallpaperManager
        WallpaperManager myWallpaperManager = WallpaperManager
                .getInstance(MainActivity.this);

        try {
            // Change the current system wallpaper
            myWallpaperManager.setResource(R.drawable.wallpaper);

            Bitmap wallpaperbmap = BitmapFactory.decodeResource(getResources(), R.drawable.wallpaper);
            myWallpaperManager.setBitmap(wallpaperbmap);

            // Show a toast message on successful change
            Toast.makeText(MainActivity.this,
                    "Wallpaper successfully changed", Toast.LENGTH_SHORT)
                    .show();

        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
    }
}
