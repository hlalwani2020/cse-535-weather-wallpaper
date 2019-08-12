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
import android.location.LocationProvider;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity implements LocationListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSION_STORAGE_CODE = 100;
    private static final int PERMISSION_WALLPAPER_CODE = 101;

    private static final int NOTIFICATION_ID_WEATHER_RETRIEVED = 0;
    private static final int NOTIFICATION_ID_WALLPAPER_SET = 1;

    private static final String SERVER_IP = "192.168.43.249";
    private static final String SERVER_DOWNLAD_URL = "http://" + SERVER_IP + "/weather/";

    private int random = 0;

    private ImageView ivWallpaper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivWallpaper = findViewById(R.id.iv_wallpaper);

        ImageButton thumbsUpButton = findViewById(R.id.image_button_thumbsup);
        thumbsUpButton.setOnClickListener(v1 -> {
        });

        ImageButton thumbsDownButton = findViewById(R.id.image_button_thumbsdown);
        thumbsDownButton.setOnClickListener(v2 -> {
        });

        ImageButton settingsButton = findViewById(R.id.image_button_settings);
        settingsButton.setOnClickListener(v -> PreferencesActivity.launch(MainActivity.this));

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            //permission is denied, request it.
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            requestPermissions(permissions, PERMISSION_STORAGE_CODE);
        }
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            //permission is denied, request it.
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions(permissions, PERMISSION_STORAGE_CODE);
        }
        if (checkSelfPermission(Manifest.permission.SET_WALLPAPER) == PackageManager.PERMISSION_DENIED) {
            //permission is denied, request it.
            String[] permissions = {Manifest.permission.SET_WALLPAPER};
            requestPermissions(permissions, PERMISSION_WALLPAPER_CODE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        fetchLocationWithPermissionCheck();
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopListeningToLocationUpdates();
    }

    private void fetchLocationWithPermissionCheck() {
        Log.i(TAG, "fetchLocationWithPermissionCheck");
        MainActivityPermissionsDispatcher.fetchLocationWithPermissionCheck(MainActivity.this);
    }

    private void stopListeningToLocationUpdates() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.removeUpdates(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(MainActivity.this, requestCode, grantResults);
    }

    @SuppressLint("MissingPermission")
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION})
    void fetchLocation() {
        Log.i(TAG, "fetchLocation");
        long refreshRateInMin = PreferencesHelper.getRefreshRateInMinutes();
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                refreshRateInMin * 1000L,
                0,
                this);
    }

    // Annotate a method which explains why the permission/s is/are needed.
    // It passes in a `PermissionRequest` object which can continue or abort the current permission
    @OnShowRationale({Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION})
    void showRationaleForLocation(PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.permission_location_rationale)
                .setPositiveButton(R.string.button_allow, (dialog, button) -> request.proceed())
                .setNegativeButton(R.string.button_deny, (dialog, button) -> request.cancel())
                .show();
    }

    // Annotate a method which is invoked if the user doesn't grant the permissions
    @OnPermissionDenied({Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION})
    void showDeniedForLocation() {
        Snackbar.make(ivWallpaper, R.string.permission_location_rationale, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.button_allow,
                        v -> fetchLocationWithPermissionCheck()
                )
                .show();
    }

    // Annotates a method which is invoked if the user
    // chose to have the device "never ask again" about a permission
    @OnNeverAskAgain({Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION})
    void showNeverAskForLocation() {
        Snackbar.make(ivWallpaper, R.string.permission_location_neverask, Snackbar.LENGTH_INDEFINITE)
                .show();
    }

    public void setWallpaper(String imagePath) {
        // Locate ImageView in activity_main.xml
        //ImageView mywallpaper = (ImageView) findViewById(R.id.wallpaper);

        // Attach image into ImageView
        //mywallpaper.setImageResource(R.drawable.wallpaper);

        // Retrieve a WallpaperManager
        WallpaperManager myWallpaperManager = WallpaperManager.getInstance(MainActivity.this);

        try {
            // Change the current system wallpaper
            Bitmap wallpaperBitmap = BitmapFactory.decodeFile(imagePath);
            if (wallpaperBitmap == null) {
                Toast.makeText(MainActivity.this,
                        "Wallpaper not found", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            //Bitmap wallpaperBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.wallpaper);
            myWallpaperManager.setBitmap(wallpaperBitmap);

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

    public void getImageFromServer(String weatherType) {
        Random rnd = new Random();
        random = rnd.nextInt(15) + 1;
        String downloadURL = SERVER_DOWNLAD_URL + weatherType + "/" + random + ".jpg";

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadURL));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE |
                DownloadManager.Request.NETWORK_WIFI);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, random + ".jpg");

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
        Log.e("Image Download", "Getting file " + downloadURL);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged provider: " + location.getProvider()
                + " coordinates: [" + location.getLatitude() + "," + location.getLongitude() + "]");
        JSONWeatherTask task = new JSONWeatherTask();
        task.execute(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (status == LocationProvider.AVAILABLE) {
            onProviderEnabled(provider);
        } else {
            onProviderDisabled(provider);
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i(TAG, "onProviderEnabled provider: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i(TAG, "onProviderDisabled provider: " + provider);
    }

    private class JSONWeatherTask extends AsyncTask<String, Void, Weather> {

        @Override
        protected Weather doInBackground(String... params) {
            String latitude = params[0];
            String longitude = params[1];
            String data = ((new WeatherHttpClient()).getWeatherData(latitude, longitude));

            try {
                Weather weather = JSONWeatherParser.getWeather(data);

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
}
