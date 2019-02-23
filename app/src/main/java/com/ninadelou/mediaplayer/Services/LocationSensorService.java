package com.ninadelou.mediaplayer.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ninadelou.mediaplayer.Data.Access.DataHandler;
import com.ninadelou.mediaplayer.MainActivity;
import com.ninadelou.mediaplayer.ReceiverLocationSensorService;

public class LocationSensorService extends Service {
    private static final String LOG_TAG = LocationSensorService.class.getSimpleName();
    private static final int NOTIFICATION_CHANNEL_ID = 101;
    private static final String NOTIFICATION_CHANNEL_NAME = "LOCATION_CHANNEL";
    public static final int DETECTION_RANGE = 10;


    private double savedLatitude;
    private double savedLongitude;
    private double currentLatitude;
    private double currentLongitude;

    private ReceiverLocationSensorService locSenReceiver;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private SensorManager sensorManager = null;
    private Sensor accelerometer;
    private SensorEventListener accelerometerListener;
    private DataHandler mDataHandler;

    public LocationSensorService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG,"LocationSensorServiceOnCreate");
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(Integer.toString(NOTIFICATION_CHANNEL_ID),
                    NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);

            Notification.Builder notificationBuilder = new Notification.Builder(this,
                    Integer.toString(NOTIFICATION_CHANNEL_ID));
            Notification notification = notificationBuilder.build();
            startForeground(NOTIFICATION_CHANNEL_ID, notification);
        } //Else do nothing

        mDataHandler = new DataHandler(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG,"LocationSensorServiceOnStartCommand");
        //Log.d(LOG_TAG,this.toString());

        String activateLocation = intent.getStringExtra("activateLocation");
        if (activateLocation == null) {
            activateLocation = "false";
        }
       // Log.d(LOG_TAG, activateLocation);


        //GEOLOCALISATION - BEGIN
        if (activateLocation.equals("true")){
        // Acquire a reference to the system Location Manager
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            // Define a listener that responds to location updates
            locationListener = new LocationListener(this);
            // Register the listener with the Location Manager to receive location updates
            try {
                if(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
                    updateCurrentLocation(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
                } else if(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
                    updateCurrentLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                } else {Log.d(LOG_TAG,"no last location known");}

                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 0, locationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, locationListener);
                Log.d(LOG_TAG,"LOCATION ACTIVEE");
            } catch (SecurityException ex) {
                Log.e(LOG_TAG, ex.getMessage()) ;       }
        }
        //GEOLOCALISATION - END

        //ACCELEROMETRE - BEGIN
        String activateAccelerometer = intent.getStringExtra("activateAccelerometer");
        if (activateAccelerometer == null) {
            activateAccelerometer = "false";
        }
        if (activateAccelerometer.equals("true")) {
            sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            //J'instancie un listener pour le mettre ensuite en paramètre du registerListener
            accelerometerListener = new AccelerometerListener(this);
            try {
                sensorManager.registerListener(accelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                Log.d(LOG_TAG,"ACCELEROMETRE ACTIVE");
            } catch (Exception ex){
                Log.e(LOG_TAG, ex.getMessage());
             }
            }
        //ACCELEROMETRE - END

        //ACTIVATION DU BROADCASTRECEIVER
        this.locSenReceiver = new ReceiverLocationSensorService(this);
        // Enregistrement du receiver auprès du localBroadcastManager
        LocalBroadcastManager.getInstance(this).registerReceiver(this.locSenReceiver,new IntentFilter(MainActivity.ACTION_SAVEDLOCATION));
        Log.d(LOG_TAG,"locSenReceiver registered");


        //Init des Saved location

        this.mDataHandler.open();
        float[] coord =  this.mDataHandler.getFavoriteLocation();
        Log.d(LOG_TAG, "initSavedLocation" + coord);
        if (coord != null){
            this.savedLatitude = coord[1];
            this.savedLongitude = coord[0];
        }
        this.mDataHandler.close();



        return START_STICKY;
    }


    public void testFavoriteLocation () {
        float[] result = new float[1];
        Location.distanceBetween(this.currentLatitude,this.currentLongitude,this.savedLatitude,this.savedLongitude,result);
        //Log.d(LOG_TAG,new Float(result[0]).toString());

        if (result[0]< DETECTION_RANGE) {
            Log.d(LOG_TAG, "FAVORITE POSITION");
            Intent intent = new Intent(MainActivity.ACTION_PLAYFAVORITEMUSIC);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    public void saveFavoriteLocation (int playId){
        this.savedLatitude = this.currentLatitude;
        this.savedLongitude = this.currentLongitude;

        this.mDataHandler.open();
        this.mDataHandler.addFavorite(playId,savedLongitude,savedLatitude);
        this.mDataHandler.close();

    }

    public void updateCurrentLocation(Location location) {
        this.currentLatitude = location.getLatitude();
        this.currentLongitude = location.getLongitude();
        //Log.d(LOG_TAG,"lat" + currentLatitude);
        //Log.d(LOG_TAG,"long" + currentLongitude);
    }

    public void notifyMP(){
        Intent intent = new Intent(MainActivity.ACTION_PLAYNEXTMUSIC);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG,"LocationSensorServiceOnDestroy");
        //Désactivation de l'écoute des positions
        if(locationListener!=null) {
            if (locationManager == null) {
                locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            }
            locationManager.removeUpdates(locationListener);
            locationManager=null;
            locationListener=null;
        }
        //Désenregistrement du receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locSenReceiver);
        Log.d(LOG_TAG,"locSenReceiver unregistered");

        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
