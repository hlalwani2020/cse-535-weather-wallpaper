package com.example.wallpaper_weather;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherHttpClient {

    private static final String TAG = WeatherHttpClient.class.getSimpleName();

    private static final String APPID = "5c4603c3256574c3a45cc9fe7a1055a4";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather?";
    private static final String IMG_URL = "http://openweathermap.org/img/w/";

    public String getWeatherData(String latitude, String longitude) {
        HttpURLConnection con = null;
        InputStream is = null;

        try {
            con = (HttpURLConnection) (new URL(BASE_URL + "lat=" + latitude
                    + "&lon=" + longitude + "&APPID=" + APPID)).openConnection();
            //con = (HttpURLConnection) (new URL("https://samples.openweathermap.org/data/2.5/weather?q=London,uk&appid=5c4603c3256574c3a45cc9fe7a1055a4")).openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.connect();

            // Let's read the response
            StringBuffer buffer = new StringBuffer();
            is = con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = br.readLine()) != null)
                buffer.append(line + "rn");

            is.close();
            con.disconnect();
            return buffer.toString();
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Throwable t) {
                Log.e(TAG, "Error while closing input stream.");
                t.printStackTrace();
            }
            try {
                if (con != null) {
                    con.disconnect();
                }
            } catch (Throwable t) {
                Log.e(TAG, "Error while disconnecting.");
                t.printStackTrace();
            }
        }

        return null;

    }
}