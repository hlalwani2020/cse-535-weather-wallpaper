package com.example.wallpaper_weather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int NOTIFICATION_ID_WEATHER_RETRIEVED = 0;
    private static final int NOTIFICATION_ID_WALLPAPER_SET = 1;
    private static final int PERMESSION_STORAGE_CODE = 100;
    private static final int PERMESSION_INTERNET_CODE = 101;
    private static final int PERMESSION_WALLPAPER_CODE = 102;
    private static final int INTENT_REQUEST_CODE_1 = 1;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 101;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    @SuppressWarnings("PointlessArithmeticExpression")
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    ImageButton androidThumbsUpButton;
    ImageButton androidThumbsDownButton;
    ImageButton androidSettingsButton;

    public String lonc;
    public String latc;
    private long mRefreshRate = 10 * 1000;
    private int mZipCode = 85281;
    Timer timer;
    Weather weather;

    final String serverIP = "192.168.43.249";
    private final String serverDownladURL = "http://" + serverIP + "/weather/";

    int random = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        androidThumbsUpButton = findViewById(R.id.image_button_thumbsup);
        androidThumbsUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v1) {

            }
        });

        androidThumbsDownButton = findViewById(R.id.dislike_button);
        androidThumbsDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromServer(weather.currentCondition.getCondition());
                setWallpaper(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + random + ".jpg");
            }

        });
        androidSettingsButton = findViewById(R.id.image_button_settings);
        androidSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v3) {
                //Intent intent = new Intent(this, preferencesActivity.class);
                //startActivity(intent);
                openPreferencesUI(v3);
            }
        });

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            //permission is denied, request it.
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            requestPermissions(permissions, PERMESSION_STORAGE_CODE);
        }
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            //permission is denied, request it.
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions(permissions, PERMESSION_STORAGE_CODE);
        }
        if (checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED) {
            //permission is denied, request it.
            String[] permissions = {Manifest.permission.INTERNET};
            requestPermissions(permissions, PERMESSION_INTERNET_CODE);
        }
        if (checkSelfPermission(Manifest.permission.SET_WALLPAPER) == PackageManager.PERMISSION_DENIED) {
            //permission is denied, request it.
            String[] permissions = {Manifest.permission.SET_WALLPAPER};
            requestPermissions(permissions, PERMESSION_WALLPAPER_CODE);
        }

        timer = new Timer("WeatherTimer");
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION_PERMISSION);
        } else {
            fetchLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        boolean isGPSEnabled = false;
        try {
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        boolean isNetworkEnabled = false;
        try {
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (!isGPSEnabled && !isNetworkEnabled) {
            Toast.makeText(MainActivity.this, "GPS/Network Permission Required.", Toast.LENGTH_LONG).show();
        } else {
            Location location = null;

            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, MainActivity.this);
                Log.d(TAG, "isNetworkEnabled");
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    updateLocationAndWallpaper(location);
                }
            }

            // if GPS Enabled get lat/long using GPS Services
            if (isGPSEnabled) {
                if (location == null) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, MainActivity.this);
                    Log.d(TAG, "GPS Enabled");
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        updateLocationAndWallpaper(location);
                    }
                }
            }

            if (location == null) {
                Log.d(TAG, "location is still null");
                Toast.makeText(MainActivity.this,
                        "Location is still null. Will be handled when onLocationchange is called",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateLocationAndWallpaper(@NonNull Location location) {
        latc = String.valueOf(location.getLatitude());
        lonc = String.valueOf(location.getLongitude());
        Log.i(TAG, "lat: " + latc + ", lon: " + lonc);
        JSONWeatherTask task = new JSONWeatherTask();
        task.execute("lat=" + latc + "&lon=" + lonc);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
            } else {
                Toast.makeText(this, "Location permission Required!!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void openPreferencesUI(View view) {
        Intent intent = new Intent(this, preferencesActivity.class);
        startActivityForResult(intent, INTENT_REQUEST_CODE_1);
    }

    public void setWallpaper(String imagePath) {
        // Locate ImageView in activity_main.xml
        //ImageView mywallpaper = (ImageView) findViewById(R.id.wallpaper);

        // Attach image into ImageView
        //mywallpaper.setImageResource(R.drawable.wallpaper);

        // Retrieve a WallpaperManager
        WallpaperManager myWallpaperManager = WallpaperManager
                .getInstance(MainActivity.this);

        try {
            // Change the current system wallpaper
            Bitmap wallpaperbmap = BitmapFactory.decodeFile(imagePath);
            if (wallpaperbmap == null) {
                Toast.makeText(MainActivity.this,
                        "Wallpaper not found", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            //Bitmap wallpaperbmap = BitmapFactory.decodeResource(getResources(), R.drawable.wallpaper);
            myWallpaperManager.setBitmap(wallpaperbmap);

            // Show a toast message on successful change
            Toast.makeText(MainActivity.this,
                    "Wallpaper successfully changed", Toast.LENGTH_SHORT)
                    .show();

            showWallPaperNotification();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showWallPaperNotification() {
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
                .setSmallIcon(R.drawable.thumbs_up2)
                .setContentTitle("New Wallpaper Set!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID_WALLPAPER_SET, builder.build());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);

        // The returned result data is identified by requestCode.
        // The request code is specified in startActivityForResult(intent, INTENT_REQUEST_CODE_1); method.
        // This request code is set by startActivityForResult(intent, INTENT_REQUEST_CODE_1) method.
        if (requestCode == INTENT_REQUEST_CODE_1) {
            if (resultCode == RESULT_OK) {
                String strRefreshRate = dataIntent.getStringExtra("REFRESH_RATE");
                String strZipCode = dataIntent.getStringExtra("ZIP_CODE");
                if (!strRefreshRate.isEmpty()) {
                    try {
                        mRefreshRate = (long) (Double.parseDouble(strRefreshRate) * 1000 * 60);
                        timer.cancel();
                        timer = new Timer("WeatherTimer");
                        TimerTask timerTask = new TimerTask() {
                            @Override
                            public void run() {

                            }
                        };
                        timer.schedule(timerTask, 1000, mRefreshRate);
                    } catch (Exception e) {
                        Log.e("preference", e.getMessage());
                    }
                }
                if (!strZipCode.isEmpty())
                    mZipCode = Integer.parseInt(strZipCode);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        updateLocationAndWallpaper(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private class JSONWeatherTask extends AsyncTask<String, Void, Weather> {

        @Override
        protected Weather doInBackground(String... params) {
            String data = ((new WeatherHttpClient()).getWeatherData(params[0]));

            try {
                weather = JSONWeatherParser.getWeather(data);

                getImageFromServer(weather.currentCondition.getCondition());
                return weather;

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);
            setWallpaper(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + random + ".jpg");

            String deleteCurrent = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + random + ".jpg";
            File downloadFile = new File(deleteCurrent);

            if (downloadFile.exists()) {
                downloadFile.delete();
            }

/*            if (weather.iconData != null && weather.iconData.length > 0) {
                Bitmap img = BitmapFactory.decodeByteArray(weather.iconData, 0, weather.iconData.length);
                imgView.setImageBitmap(img);
            }*/
            if (weather.location != null) {
                ((TextView) findViewById(R.id.tempTxtView)).setText("" + Math.round(((weather.temperature.getTemp() - 273.15) * 9 / 5) + 32) + "°F");
                ((TextView) findViewById(R.id.cityTxtView)).setText(weather.location.getCity() + "," + weather.location.getCountry());
                Address address = getAddressFromLocation(weather.location.getLatitude(), weather.location.getLongitude());
                if (address != null)
                    ((TextView) findViewById(R.id.stateTxtView)).setText(address.getAdminArea());
                else
                    ((TextView) findViewById(R.id.stateTxtView)).setText("Unidentified");
                ((TextView) findViewById(R.id.longTxtView)).setText("" + weather.location.getLongitude() + "°");
                ((TextView) findViewById(R.id.latTxtView)).setText("" + weather.location.getLatitude() + "°");
                ((TextView) findViewById(R.id.condTxtView)).setText(weather.currentCondition.getCondition() + "(" + weather.currentCondition.getDescr() + ")");
                ((TextView) findViewById(R.id.humidTxtView)).setText("" + weather.currentCondition.getHumidity() + "%");
                //((TextView) findViewById(R.id.pressTxtView)).setText("" + weather.currentCondition.getPressure() + " hPa");
                //((TextView) findViewById(R.id.windSpdTxtView)).setText("" + weather.wind.getSpeed() + " mps");
                //((TextView) findViewById(R.id.windDegTxtView)).setText("" + weather.wind.getDeg() + "°");
                //((TextView) findViewById(R.id.rainTxtView)).setText("" + weather.rain.getTime() + " " + weather.rain.getAmmount());
                showWeatherNotification();
            }
        }

        private void showWeatherNotification() {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "1")
                    .setSmallIcon(R.drawable.thumbs_up2)
                    .setContentTitle("Weather Retrieved!")
                    .setContentText("Wohoo! Weather is here")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);

            notificationManager.notify(NOTIFICATION_ID_WEATHER_RETRIEVED, builder.build());
        }

        private Address getAddressFromLocation(final double latitude, final double longitude) {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                List<Address> addressList = geocoder.getFromLocation(
                        latitude, longitude, 1);
                if (addressList != null && addressList.size() > 0) {
                    return addressList.get(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    public void getImageFromServer(String weatherType) {

        Random rnd = new Random();
        random = rnd.nextInt(15) + 1;
        String downladURL = serverDownladURL + weatherType + "/" + random + ".jpg";

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downladURL));

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE |
                DownloadManager.Request.NETWORK_WIFI);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, random + ".jpg");

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
        Log.e("Image Download", "Getting file " + downladURL);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
