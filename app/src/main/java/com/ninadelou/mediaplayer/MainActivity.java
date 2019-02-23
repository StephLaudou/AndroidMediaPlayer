package com.ninadelou.mediaplayer;

import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.ninadelou.mediaplayer.Data.Access.DataHandler;
import com.ninadelou.mediaplayer.Data.PlayItem;
import com.ninadelou.mediaplayer.Services.ApiService;
import com.ninadelou.mediaplayer.Services.LocationSensorService;
import com.ninadelou.mediaplayer.Services.MediaPlayerService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, MediaPlayer.OnCompletionListener {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    //private static final int PERMISSIONS_REQUEST_CODE = 101;
    public static final String ACTION_SAVEDLOCATION = "com.ninadelou.geoloc.SAVEDLOCATION";
    public static final String ACTION_PLAYBYID = "com.ninadelou.mediaplayer.PlayById";
    public static final String ACTION_CURRENTPLAY = "com.ninadelou.mediaplayer.CurrentPlay";
    public static final String ACTION_PLAYPAUSE = "com.ninadelou.mediaplayer.PlayPause";
    public static final String ACTION_PLAYFAVORITEMUSIC = "com.ninadelou.mediaplayer.PlayFavoriteMusic";
    public static final String ACTION_PLAYNEXTMUSIC = "com.ninadelou.mediaplayer.PlayNextMusic";
    public static final String ACTION_PLAYPREVIOUSMUSIC = "com.ninadelou.mediaplayer.PlayPreviousMusic";
    public static final String ACTION_ENDDOWNLOAD = "com.ninadelou.mediaplayer.EndDownload";


    private DataHandler mDataHandler;
    private ListView musicLV;
    private List<MusicFile> musicList;
    private ImageButton bPlay;
    private ImageButton bNext;
    private ImageButton bPrevious;
    private Button bSave;
    private String mPlayListName;
    private static int mPlayListId = -1;
    private ArrayList<PlayItem> playList;
    private ReceiverMainActivity receiverMainActivity;
    private static MediaPlayerService.MP_STATE currentPlayerStatus;
    private static String CurrentPlayName;
    private TextView tvCurrentPlay;
    private int playId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(LOG_TAG,"OnCreate");

        //TODO : gestion version android : startfoe=reground pour seviece et notification

        mDataHandler = new DataHandler(this);


        //Affichage de la liste des mp3
        musicLV = (ListView) findViewById(R.id.music_listView);

        // Référencement des view
        bPlay = (ImageButton) findViewById(R.id.buttonPlay);
        bSave = (Button) findViewById(R.id.buttonSave);
        bNext = (ImageButton) findViewById(R.id.buttonNext);
        bPrevious = (ImageButton) findViewById(R.id.buttonPrevious);


        Intent intent = getIntent();
        mPlayListName = intent.getStringExtra("playlist");
        mPlayListId = intent.getIntExtra("playlistid", -1);

        /////////// ACTIVATION DU BROADCASTRECEIVER
        this.receiverMainActivity = new ReceiverMainActivity(this);
        // Enregistrement du receiver auprès du localBroadcastManager
        LocalBroadcastManager.getInstance(this).registerReceiver(this.receiverMainActivity,new IntentFilter(MainActivity.ACTION_CURRENTPLAY));
        LocalBroadcastManager.getInstance(this).registerReceiver(this.receiverMainActivity,new IntentFilter(MainActivity.ACTION_ENDDOWNLOAD));
        Log.d(LOG_TAG,"receiverMainActivity registered");

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG,"OnStart");

        if(mPlayListId > -1) {
            mDataHandler.open();
            playList = mDataHandler.getPlayList();
            mDataHandler.close();

            if (!playList.isEmpty()) {

                ArrayAdapter<PlayItem> adapter = new ArrayAdapter<PlayItem>(this, android.R.layout.simple_list_item_1, playList);
                musicLV.setAdapter(adapter);

                Intent intent = new Intent(this, MediaPlayerService.class);
                //intent.putExtra("ACTION","Start");
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent);
                } else {
                    startService(intent);
                }


            } // Else do nothing
        } else {
            Intent intent = new Intent(this, ApiService.class);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        }

        //REQUEST PERMISSION  pour geolocalisation BEGIN
        /*ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.FOREGROUND_SERVICE},
                PERMISSIONS_REQUEST_CODE);*/

        //REQUEST PERMISSION pour geolocalisation - END


        //Association de listener d'évènement à des view
        bPlay.setOnClickListener(this);
        musicLV.setOnItemClickListener(this);
        bSave.setOnClickListener(this);
        bNext.setOnClickListener(this);
        bPrevious.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        String playpause = "";

        switch (v.getId()) {
            //Bouton Save Location and Music
            case R.id.buttonSave:
                Intent intent = new Intent(ACTION_SAVEDLOCATION);
                intent.putExtra("PLAYID",playId);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            break;
            //Bouton Play / Pause
            case R.id.buttonPlay:
            // on lance le morceau en cours, sinon on prend le premier de la liste
                if (currentPlayerStatus == MediaPlayerService.MP_STATE.PLAYING){
                    playpause = "pause";
                } else if (currentPlayerStatus == MediaPlayerService.MP_STATE.PAUSED){
                    playpause = "play";
                }
                sendBroadcastPlayPause(playpause);
            break;

            case R.id.buttonNext:
                Intent intentNext = new Intent(MainActivity.ACTION_PLAYNEXTMUSIC);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intentNext);
                break;

            case R.id.buttonPrevious:
                Intent intentPrev = new Intent(MainActivity.ACTION_PLAYPREVIOUSMUSIC);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intentPrev);
                break;

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        playId = playList.get(position).getId();
        sendBroadcastPlayById();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //TODO playNext();
    }
    public void sendBroadcastPlayById(){
        Intent intent = new Intent(ACTION_PLAYBYID);
        intent.putExtra("PLAYID",playId);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d(LOG_TAG,"send broadcast play by id ");
    }

    public void sendBroadcastPlayPause(String PlayPause){
        Intent intent = new Intent(ACTION_PLAYPAUSE);
        if(PlayPause == "pause"){
            intent.putExtra("PLAYPAUSE","pause");
        }
        if(PlayPause == "play"){
            intent.putExtra("PLAYPAUSE","play");
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d(LOG_TAG,"send broadcast play pause");
    }

    public void updateMediaPlayerStatus(MediaPlayerService.MP_STATE mpstatus, String playname){

        this.currentPlayerStatus = mpstatus;
        this.CurrentPlayName = playname;
        tvCurrentPlay = (TextView) findViewById(R.id.currentPlay);
        tvCurrentPlay.setText(this.CurrentPlayName);
        if (currentPlayerStatus == MediaPlayerService.MP_STATE.PLAYING) {
            bPlay.setImageResource(android.R.drawable.ic_media_pause);
        }
        if (currentPlayerStatus == MediaPlayerService.MP_STATE.PAUSED){
            bPlay.setImageResource(android.R.drawable.ic_media_play);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG,"OnStop");
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG,"OnPause");
        /*if (mediaPlayer != null) {
            mediaPlayer.pause();
        }*/

    }
    //TODO bloquer la rotation automatique de l'écran

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG,"OnResume");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG,"OnDestroy");
        stopService(new Intent(this,LocationSensorService.class));
        Intent intentApi = new Intent(this, ApiService.class);
        stopService(intentApi);
        Intent intentMP = new Intent(this, MediaPlayerService.class);
        stopService(intentMP);
    }




    //Demande permision, verif dispo capteur et démarrage des services
    /*@Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        boolean isLocationProviderAvailable = false;
        boolean isAccelerometerAvailable =false ;
        SensorManager sensorManager = null;

        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length == 3) {
                    if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                        // FOREGROUND_SERVICE only available from API 28.
                        Log.d(LOG_TAG, "Overriding FOREGROUND_SERVICE permission");
                        grantResults[2] = PackageManager.PERMISSION_GRANTED;
                    }

                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                            grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                            grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                            Log.i(LOG_TAG, "Authorizations granted");
                            isLocationProviderAvailable = true;
                    } else if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        Log.e(LOG_TAG, "ACCESS_COARSE_LOCATION not granted");
                    } else if(grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                        Log.e(LOG_TAG, "ACCESS_FINE_LOCATION not granted");
                    } else if(grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                        Log.e(LOG_TAG, "FOREGROUND_SERVICE not granted");
                    } else  {
                        Log.e(LOG_TAG, "Unknown authorization not granted");
                    }
                } else {
                    Log.e(LOG_TAG, "Bad response format to authorization request");
                }
                break;

            default:
                Log.e(LOG_TAG, "Authorization request not answered");
                break;
        }

        //Check Sensor availability
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            isAccelerometerAvailable = true;
        }
        else {
            Log.i(LOG_TAG, "No Accelerometer sensor");
        }

        if (isLocationProviderAvailable || isAccelerometerAvailable) {
            Intent intent = new Intent(this, LocationSensorService.class);
            if (isLocationProviderAvailable){
                intent.putExtra("activateLocation", "true");
            }
            if (isAccelerometerAvailable){
                intent.putExtra("activateAccelerometer", "true");
            }
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        }
    }*/


}
