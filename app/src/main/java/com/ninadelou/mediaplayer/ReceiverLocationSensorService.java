package com.ninadelou.mediaplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ninadelou.mediaplayer.Services.LocationSensorService;

public class ReceiverLocationSensorService extends BroadcastReceiver {
    private static final String LOG_TAG = ReceiverLocationSensorService.class.getSimpleName();

    private LocationSensorService locSenService;

    public ReceiverLocationSensorService(LocationSensorService service) {
        this.locSenService = service;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction() == MainActivity.ACTION_SAVEDLOCATION) {
            Log.i(LOG_TAG, "Intent received : " + intent.getAction());
            int playId = intent.getIntExtra("PLAYID", 0);
            this.locSenService.saveFavoriteLocation(playId);
        } else {
            Log.w(LOG_TAG, "Unknown intent received : " + intent.getAction());
        }
    }
}

