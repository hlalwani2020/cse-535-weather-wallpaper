package com.example.wallpaper_weather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PreferencesActivity extends AppCompatActivity {

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, PreferencesActivity.class);
        activity.startActivity(intent);
    }

    private EditText edtTxtRefreshRate;
    private EditText edtTxtZipCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        edtTxtRefreshRate = findViewById(R.id.edtTxtRefreshRate);
        edtTxtRefreshRate.setText(String.valueOf(PreferencesHelper.getRefreshRateInMinutes()));

        edtTxtZipCode = findViewById(R.id.edtTxtZipCode);
        edtTxtZipCode.setText(PreferencesHelper.getZipCode());
    }

    public void applySettings(View view) {
        //startActivity(new Intent(this, MainActivity.class) );
        applyAndReturn();
    }

    @Override
    public void onBackPressed() {
        applyAndReturn();
    }

    private void applyAndReturn() {
        int refreshRateInMinutes = -1;
        if (edtTxtRefreshRate.getText() != null) {
            refreshRateInMinutes = Integer.parseInt(String.valueOf(edtTxtRefreshRate.getText()));
        }
        if (refreshRateInMinutes > 0) {
            PreferencesHelper.setRefreshRate(refreshRateInMinutes);
            Toast.makeText(this, R.string.refresh_rate_updated_successfully, Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, R.string.invalid_refresh_rate, Toast.LENGTH_LONG).show();
        }

        if (edtTxtZipCode.getText() != null && edtTxtZipCode.getText().toString().length() == 6) {
            PreferencesHelper.setZipCode(edtTxtZipCode.getText().toString());
            Toast.makeText(this, R.string.zip_code_updated_successfully, Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, R.string.invalid_refresh_rate, Toast.LENGTH_LONG).show();
        }
    }

}
