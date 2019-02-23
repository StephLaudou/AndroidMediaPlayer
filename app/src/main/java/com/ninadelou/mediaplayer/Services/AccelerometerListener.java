package com.ninadelou.mediaplayer.Services;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

public class AccelerometerListener implements SensorEventListener {
    private final String LOG_TAG = AccelerometerListener.class.getSimpleName();

    private LocationSensorService locationSensorService;
    private long lastUpdate;

    public AccelerometerListener(LocationSensorService locationSensorService) {
    this.locationSensorService = locationSensorService;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Log.d(LOG_TAG, "X :  " + event.values[0]);
        //Log.d(LOG_TAG, "Y :  " + event.values[1]);
        //Log.d(LOG_TAG, "Z :  " + event.values[2]);

        long actualTime = event.timestamp; //get the event's timestamp
        float value = event.values[0];
        if(actualTime - lastUpdate > 2000000000) { // = 2s
            //Log.d(TAG, "" + value);
            if (Math.abs(value)>15){ // = 15 m.s-2
                Log.d(LOG_TAG, "Seuil accéléromètre X dépassé");
                this.locationSensorService.notifyMP();
                this.lastUpdate = actualTime;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


}
