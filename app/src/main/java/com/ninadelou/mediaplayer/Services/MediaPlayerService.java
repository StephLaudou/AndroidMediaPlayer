package com.ninadelou.mediaplayer.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ninadelou.mediaplayer.Data.Access.DataHandler;
import com.ninadelou.mediaplayer.MainActivity;
import com.ninadelou.mediaplayer.ReceiverMediaPlayerService;

import java.io.File;


public class MediaPlayerService extends Service {
    private static final String LOG_TAG = MediaPlayerService.class.getSimpleName();

    public static enum MP_STATE {
        PLAYING,
        PAUSED,
        STOPPED}


    private ReceiverMediaPlayerService mediaReceiver;
    private DataHandler mDataHandler;
    private MediaPlayer mMediaPlayer = null;
    private int savedMusicId;
    private static String currentPlayName;
    private static int currentPlayId;
    private static MP_STATE currentMPStatus =  MP_STATE.STOPPED;
    private static final int NOTIFICATION_CHANNEL_ID = 101;
    private static final String NOTIFICATION_CHANNEL_NAME = "MEDIA_CHANNEL";


    public MediaPlayerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

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

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG,"MediaPlayerServiceOnStartCommand");

        // ACTIVATION DU BROADCASTRECEIVER
        this.mediaReceiver = new ReceiverMediaPlayerService(this);
        // Enregistrement du receiver auprès du localBroadcastManager
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mediaReceiver,new IntentFilter(MainActivity.ACTION_PLAYBYID));
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mediaReceiver,new IntentFilter(MainActivity.ACTION_PLAYPAUSE));
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mediaReceiver,new IntentFilter(MainActivity.ACTION_SAVEDLOCATION));
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mediaReceiver,new IntentFilter(MainActivity.ACTION_PLAYFAVORITEMUSIC));
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mediaReceiver,new IntentFilter(MainActivity.ACTION_PLAYNEXTMUSIC));
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mediaReceiver,new IntentFilter(MainActivity.ACTION_PLAYPREVIOUSMUSIC));

        Log.d(LOG_TAG,"mediaReceiver registered");



        //Init des Saved Playid
        this.mDataHandler.open();
        int favplayid =  this.mDataHandler.getFavoritePlayid();
        if (favplayid >= 0){
            Log.i(LOG_TAG, "Favorite playid found is : " + favplayid);
            this.saveFavoriteMusic(favplayid);
        }
        this.mDataHandler.close();


        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void playMusicByIndex(int playId){
        Log.d(LOG_TAG,"playMusicByIndex");
        this.mDataHandler.open();
        String filename = this.mDataHandler.getPlayFile(playId);
        String playname = this.mDataHandler.getPlayName(playId);
        this.mDataHandler.close();



        if(filename.length() > 0) {
            try {
                if (mMediaPlayer != null && (currentMPStatus == MP_STATE.PAUSED || currentMPStatus == MP_STATE.PLAYING)) {
                    mMediaPlayer.stop();
                    mMediaPlayer.reset();
                }
                File file = new File(getFilesDir(), filename);
                //this.mMediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.fromFile(file));

                this.mMediaPlayer.setDataSource(getApplicationContext(),
                        Uri.fromFile(file));
                this.mMediaPlayer.prepare();
                this.mMediaPlayer.start();

                updateMediaPlayerStatus(MP_STATE.PLAYING, playname,playId);
                notifyMainActivity();
                Log.d(LOG_TAG, "playing" + filename);

            } catch (Exception ex) {
                Log.e(LOG_TAG, "Unable to play sound");
                Log.e(LOG_TAG,ex.toString());
            }
        } else {
            Log.d(LOG_TAG, "No file found for play: " + playId);
        }

    }

    public void togglePlayPause(String PlayPause ){
            if (PlayPause == "play"){
                this.mMediaPlayer.start();
                updateMediaPlayerStatus(MP_STATE.PLAYING,"",0);
            }

            if (PlayPause == "pause"){
                this.mMediaPlayer.pause();
                updateMediaPlayerStatus(MP_STATE.PAUSED,"",0);
            }
            notifyMainActivity();
    }

    private void notifyMainActivity(){
        Intent intent = new Intent(MainActivity.ACTION_CURRENTPLAY);
        intent.putExtra("MPSTATUS",this.currentMPStatus);
        intent.putExtra("PLAYNAME",this.currentPlayName);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d(LOG_TAG,"send broadcast to Main Activity");

    }

    private void updateMediaPlayerStatus(MP_STATE status, String playName, int  playId) {
        this.currentMPStatus = status;
        if (playName != "") {
            this.currentPlayName = playName;
            this.currentPlayId = playId;
        }
    }


    public void playNextMusic(){
        this.mDataHandler.open();
        int nextPlayid = this.mDataHandler.getNextPlay(currentPlayId);
        this.mDataHandler.close();

        if (nextPlayid != -1) {
            playMusicByIndex(nextPlayid);
        } else {
            Log.d(LOG_TAG, "Fin de la liste");
        }
    }


    public void playPreviousMusic() {
        this.mDataHandler.open();
        int nextPlayid = this.mDataHandler.getPreviousPlay(currentPlayId);
        this.mDataHandler.close();

        if (nextPlayid != -1) {
            playMusicByIndex(nextPlayid);
        } else {
            Log.d(LOG_TAG, "Fin de la liste");
        }
    }

    public void saveFavoriteMusic(int playId){
        this.savedMusicId = playId;
    }

    public void playFavoriteMusic() {
        if(savedMusicId != currentPlayId) {
            playMusicByIndex(savedMusicId);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG,"Media Player Stopped");
        mMediaPlayer.stop();
    }
}
