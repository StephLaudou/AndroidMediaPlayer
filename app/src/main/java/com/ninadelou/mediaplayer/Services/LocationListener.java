package com.ninadelou.mediaplayer.Services;

import android.location.Location;
import android.os.Bundle;

public class LocationListener implements android.location.LocationListener {
    private static final String LOG_TAG = LocationListener.class.getSimpleName();
    private LocationSensorService mLocationSensorService;

    public LocationListener(LocationSensorService service){
        this.mLocationSensorService = service;
    }

    public void onLocationChanged(Location location) {
        //Log.d(LOG_TAG,location.toString());
        //Log.d(LOG_TAG, "Latitude " + location.getLatitude() + " et longitude " + location.getLongitude());
        //Log.d(LOG_TAG, "provider :  " + location.getProvider());
        //Log.d(LOG_TAG, "latitude :  " + location.getLatitude());
        //Log.d(LOG_TAG, "longitude :  " + location.getLongitude());

        //A chaque changement de position, je teste la nouvelle position
        this.mLocationSensorService.updateCurrentLocation(location);
        this.mLocationSensorService.testFavoriteLocation();

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {}
    public void onProviderEnabled(String provider) {}
    public void onProviderDisabled(String provider) {}


}
