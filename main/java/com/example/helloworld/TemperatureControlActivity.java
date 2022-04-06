package com.example.helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class TemperatureControlActivity extends AppCompatActivity {

    DbAdapter dbAdapter;  // Db adapter variable for the activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_control);

        // Load the outside temperature and fill it in
        WeatherAdapter weatherAdapter = new WeatherAdapter();
        EditText etOutsideTemp = (EditText) findViewById(R.id.etOutsideTemp);
        etOutsideTemp.setText(String.valueOf(weatherAdapter.outsideTemp));
    }

    public void btnSendToCarClick(View view) {
        // TODO: Log the temperatures in a database

    }
}