package com.example.helloworld;

import java.util.Random;

public class WeatherAdapter {
    int outsideTemp;

    public WeatherAdapter() {
        // Get the outside temperature
        // Use a random number reasonable for MI
        Random r = new Random();
        outsideTemp = r.ints(1, 60, 95).findFirst().getAsInt();

        // TODO: Get the outside temperature from AWS
    }
}
