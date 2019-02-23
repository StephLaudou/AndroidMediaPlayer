package com.ninadelou.mediaplayer.Data.Access;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ninadelou.mediaplayer.Data.PlayItem;

import java.util.ArrayList;

public class DataHandler {
    private final static int DB_VERSION = 1;
    private final static String DB_NAME = "database.db";
    private static final String LOG_TAG = DataHandler.class.getSimpleName();


    private SQLiteDatabase mDatabase = null;
    private DataHelper mHandler = null;

    public DataHandler(Context pContext) {
        this.mHandler = new DataHelper(pContext, DB_NAME, null,
                DB_VERSION);
    }

    public void open() {
        if(this.mHandler != null) {
            this.mDatabase = this.mHandler.getWritableDatabase();
        } // Else do nothing
    }

    public long addPlayItem(PlayItem item) {
        ContentValues values = new ContentValues();
        values.put(PlayData.COLUMN_ID, item.getId());
        values.put(PlayData.COLUMN_NAME, item.getName());
        values.put(PlayData.COLUMN_AUTHOR, item.getAuthor());
        values.put(PlayData.COLUMN_RECORD, item.getRecord());
        values.put(PlayData.COLUMN_URL, item.getUrl());
        values.put(PlayData.COLUMN_FILE, item.getFile());

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + PlayData.TABLE
                + " WHERE "
                + PlayData.COLUMN_ID
                + "="
                + item.getId(), null);

        if(cursor.moveToFirst()) {
            return mDatabase.update(PlayData.TABLE, values,
                    PlayData.COLUMN_ID + " = " + item.getId(),
                    null);
        } else {
            return mDatabase.insert(PlayData.TABLE, null, values);
        }
    }

    public ArrayList<PlayItem> getPlayList() {
        ArrayList<PlayItem> list = new ArrayList<PlayItem>();

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + PlayData.TABLE, null);

        if (cursor.moveToFirst()) {
            do {
                PlayItem playItem = new PlayItem(cursor.getInt(PlayData.NUM_COLUMN_ID),
                        cursor.getString(PlayData.NUM_COLUMN_NAME),
                        cursor.getString(PlayData.NUM_COLUMN_AUTHOR),
                        cursor.getString(PlayData.NUM_COLUMN_RECORD),
                        cursor.getString(PlayData.NUM_COLUMN_URL),
                        cursor.getString(PlayData.NUM_COLUMN_FILE));
                list.add(playItem);
            } while (cursor.moveToNext());
        } // Else do nothing

        return list;
    }

    public String getPlayFile(int playId) {
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + PlayData.TABLE
                + " WHERE "
                + PlayData.COLUMN_ID
                + "="
                + playId, null);

        if(cursor.moveToFirst()) {
            return cursor.getString(PlayData.NUM_COLUMN_FILE);
        } else {
            return "";
        }
    }

    public String getPlayName(int playId) {
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + PlayData.TABLE
                + " WHERE "
                + PlayData.COLUMN_ID
                + "="
                + playId, null);

        if(cursor.moveToFirst()) {
            return cursor.getString(PlayData.NUM_COLUMN_NAME);
        } else {
            return "";
        }
    }

    public int getNextPlay(int playId) {
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + PlayData.TABLE
                + " WHERE "
                + PlayData.COLUMN_ID
                + " > "
                + playId
                + " Order by "
                + PlayData.COLUMN_ID, null);

        if(cursor.moveToFirst()) {
            return Integer.parseInt(cursor.getString(PlayData.NUM_COLUMN_ID));
        } else {
            return -1;
        }
    }

    public int getPreviousPlay(int playId) {
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + PlayData.TABLE
                + " WHERE "
                + PlayData.COLUMN_ID
                + " < "
                + playId
                + " Order by "
                + PlayData.COLUMN_ID
                + " desc", null);

        if(cursor.moveToFirst()) {
            return Integer.parseInt(cursor.getString(PlayData.NUM_COLUMN_ID));
        } else {
            return -1;
        }
    }

    public void drop() {
        mDatabase.execSQL("delete from " + PlayData.TABLE);
    }

    public void close() {
        if(this.mDatabase != null) {
            this.mDatabase.close();
        } // Else do nothing
    }


    public long addFavorite(int playId,double longitude, double latitute) {
        ContentValues values = new ContentValues();
        values.put(FavoriteData.COLUMN_FAVPLAY, playId);
        values.put(FavoriteData.COLUMN_FAVLONG, longitude);
        values.put(FavoriteData.COLUMN_FAVLAT, latitute);

        Log.i(LOG_TAG, "Deleting existing favorite...");

         mDatabase.execSQL("delete from " + FavoriteData.TABLE);

        Log.i(LOG_TAG, "Inserting new favorite..." + values.toString());

        return mDatabase.insert(FavoriteData.TABLE, null, values);

    }

    public float[] getFavoriteLocation(){
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + FavoriteData.TABLE
                , null);

        if(cursor.moveToFirst()) {
            Log.i(LOG_TAG, "Loading favorite location from db...");
            float longitude = Float.parseFloat(cursor.getString(FavoriteData.NUM_COLUMN_FAVLONG));
            float latitude = Float.parseFloat(cursor.getString(FavoriteData.NUM_COLUMN_FAVLAT));

            return new float[] {longitude, latitude};
        }
        Log.i(LOG_TAG, "No existing favorite...");
        return null;
    }


    public int getFavoritePlayid(){

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + FavoriteData.TABLE
                , null);

        if(cursor.moveToFirst()) {
            return Integer.parseInt(cursor.getString(FavoriteData.NUM_COLUMN_FAVPLAY));
        }

        return -1;
    }


}