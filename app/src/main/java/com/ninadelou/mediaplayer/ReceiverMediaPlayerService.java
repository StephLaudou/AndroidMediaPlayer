package com.ninadelou.mediaplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ninadelou.mediaplayer.Services.MediaPlayerService;


public class ReceiverMediaPlayerService extends BroadcastReceiver {
    private static final String LOG_TAG = ReceiverMediaPlayerService.class.getSimpleName();

    private MediaPlayerService mediaService;
    private int playId;
    private String playPause;

    public ReceiverMediaPlayerService(MediaPlayerService mediaPlayerService) {
        this.mediaService = mediaPlayerService;
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction() == MainActivity.ACTION_PLAYBYID) {
            Log.d(LOG_TAG, "OnReceice Media Player PlayId");
            playId = intent.getIntExtra("PLAYID", 0);
            this.mediaService.playMusicByIndex(playId);
        }

        if(intent.getAction()== MainActivity.ACTION_PLAYPAUSE){
            Log.d(LOG_TAG, "OnReceice Media Player PlayPause");
            playPause = intent.getStringExtra("PLAYPAUSE");
            this.mediaService.togglePlayPause(playPause);

        }

        if (intent.getAction() == MainActivity.ACTION_SAVEDLOCATION){
            Log.d(LOG_TAG, "OnReceice Media Player SavedLocation");
            this.mediaService.saveFavoriteMusic(playId);
        }


        if (intent.getAction() == MainActivity.ACTION_PLAYFAVORITEMUSIC){
            Log.d(LOG_TAG, "OnReceice Media Player PlayFavoriteMusic");
            this.mediaService.playFavoriteMusic();
        }

        if (intent.getAction() == MainActivity.ACTION_PLAYNEXTMUSIC){
            Log.d(LOG_TAG, "OnReceice Media Player PlayNextMusic");
            this.mediaService.playNextMusic();
        }

        if (intent.getAction() == MainActivity.ACTION_PLAYPREVIOUSMUSIC){
            Log.d(LOG_TAG, "OnReceice Media Player PlayNextMusic");
            this.mediaService.playPreviousMusic();
        }


    }
}
