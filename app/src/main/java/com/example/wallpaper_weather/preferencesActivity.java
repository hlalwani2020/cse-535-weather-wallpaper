package com.example.wallpaper_weather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class preferencesActivity extends AppCompatActivity {

    EditText edtTxtRefreshRate;
    EditText edtTxtZipCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        edtTxtRefreshRate = (EditText)findViewById(R.id.edtTxtRefreshRate);
        edtTxtZipCode = (EditText)findViewById(R.id.edtTxtZipCode);
    }

    public void applySettings(View view) {
        //startActivity(new Intent(this, MainActivity.class) );
        applyAndReturn();
    }

    @Override
    public void onBackPressed() {
        applyAndReturn();
    }

    private void applyAndReturn(){
        Intent intent = new Intent();
        intent.putExtra("REFRESH_RATE", edtTxtRefreshRate.getText().toString());
        intent.putExtra("ZIP_CODE", edtTxtZipCode.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }
}
