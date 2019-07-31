package com.example.wallpaper_weather;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherHttpClient {

    private static String BASE_URL = "https://api.openweathermap.org/data/2.5/weather?";
    private static String IMG_URL = "http://openweathermap.org/img/w/";
    private static String APPID = "5c4603c3256574c3a45cc9fe7a1055a4";

    public String getWeatherData(String location) {
        HttpURLConnection con = null;
        InputStream is = null;

        try {
            con = (HttpURLConnection) (new URL(BASE_URL + location + "&APPID=" + APPID)).openConnection();
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
                is.close();
            } catch (Throwable t) {
            }
            try {
                con.disconnect();
            } catch (Throwable t) {
            }
        }

        return null;

    }
}