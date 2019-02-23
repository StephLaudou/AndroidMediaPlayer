package com.ninadelou.mediaplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.ninadelou.mediaplayer.Services.MediaPlayerService;

public class ReceiverMainActivity extends BroadcastReceiver {
    private MainActivity mainActivity;
    private String getPlayName;
    private MediaPlayerService.MP_STATE getMPStatus;
    public ReceiverMainActivity(MainActivity mainActivity) {this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction() == MainActivity.ACTION_CURRENTPLAY) {
            Bundle bd = intent.getExtras();
            if (bd != null) {
                getPlayName = (String) bd.get("PLAYNAME");
                getMPStatus = (MediaPlayerService.MP_STATE) bd.get("MPSTATUS");
            }
            this.mainActivity.updateMediaPlayerStatus(getMPStatus,getPlayName);
        }

        if(intent.getAction() == MainActivity.ACTION_ENDDOWNLOAD) {
            Toast.makeText(mainActivity, R.string.toastdld, Toast.LENGTH_LONG).show();
        }

    }
}
