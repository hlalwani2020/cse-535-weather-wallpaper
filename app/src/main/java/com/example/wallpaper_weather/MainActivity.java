package com.example.wallpaper_weather;

import androidx.appcompat.app.AppCompatActivity;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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
		JSONWeatherTask task = new JSONWeatherTask();
        task.execute(new String[]{"lat=33.41&lon=-111.91"});
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

    private class JSONWeatherTask extends AsyncTask<String, Void, Weather> {

        @Override
        protected Weather doInBackground(String... params) {
            Weather weather = new Weather();
            String data = ( (new WeatherHttpClient()).getWeatherData(params[0]));

            try {
                weather = JSONWeatherParser.getWeather(data);

                // Let's retrieve the icon
               // weather.iconData = ( (new WeatherHttpClient()).getImage(weather.currentCondition.getIcon()));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return weather;

        }
        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);
            ((TextView) findViewById(R.id.tempTxtView)).setText("" + Math.round(((weather.temperature.getTemp() - 273.15)*9/5)+32) + "°F");
/*            if (weather.iconData != null && weather.iconData.length > 0) {
                Bitmap img = BitmapFactory.decodeByteArray(weather.iconData, 0, weather.iconData.length);
                imgView.setImageBitmap(img);
            }*/

            ((TextView) findViewById(R.id.cityTxtView)).setText(weather.location.getCity() + "," + weather.location.getCountry());
            ((TextView) findViewById(R.id.longTxtView)).setText("" + weather.location.getLongitude() + "°");
            ((TextView) findViewById(R.id.latTxtView)).setText("" + weather.location.getLatitude() + "°");
            ((TextView) findViewById(R.id.condTxtView)).setText(weather.currentCondition.getCondition() + "(" + weather.currentCondition.getDescr() + ")");
            ((TextView) findViewById(R.id.humidTxtView)).setText("" + weather.currentCondition.getHumidity() + "%");
            //((TextView) findViewById(R.id.pressTxtView)).setText("" + weather.currentCondition.getPressure() + " hPa");
            //((TextView) findViewById(R.id.windSpdTxtView)).setText("" + weather.wind.getSpeed() + " mps");
            //((TextView) findViewById(R.id.windDegTxtView)).setText("" + weather.wind.getDeg() + "°");
            //((TextView) findViewById(R.id.rainTxtView)).setText("" + weather.rain.getTime() + " " + weather.rain.getAmmount());
        }
    }
}
