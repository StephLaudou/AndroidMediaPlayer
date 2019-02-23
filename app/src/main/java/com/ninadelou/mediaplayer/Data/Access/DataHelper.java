package com.ninadelou.mediaplayer.Data.Access;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = DataHelper.class.getSimpleName();

    private static final String SQL_CREATE_TABLE_PLAYLIST = "CREATE TABLE "
            + PlayData.TABLE
            + " ("
            + PlayData.COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + PlayData.COLUMN_NAME
            + " TEXT NOT NULL, "
            + PlayData.COLUMN_AUTHOR
            + " TEXT NOT NULL, "
            + PlayData.COLUMN_RECORD
            + " TEXT NOT NULL, "
            + PlayData.COLUMN_URL
            + " TEXT NOT NULL, "
            + PlayData.COLUMN_FILE
            + " TEXT NOT NULL "
            + ")";

    private static final String SQL_CREATE_TABLE_FAVORITE = "CREATE TABLE "
            + FavoriteData.TABLE
            + " ("
            + FavoriteData.COLUMN_FAVPLAY
            + " INTEGER NOT NULL, "
            + FavoriteData.COLUMN_FAVLONG
            + " REAL NOT NULL, "
            + FavoriteData.COLUMN_FAVLAT
            + " REAL NOT NULL "
            + ")";

    private static final String SQL_DELETE_TABLE_PLAYLIST =
            "DROP TABLE IF EXISTS " + PlayData.TABLE;

    public DataHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                      int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "Creating...");

        Log.d(LOG_TAG, "Query: " + SQL_CREATE_TABLE_PLAYLIST);
        db.execSQL(SQL_CREATE_TABLE_PLAYLIST);

        Log.d(LOG_TAG, "Query: " + SQL_CREATE_TABLE_FAVORITE);
        db.execSQL(SQL_CREATE_TABLE_FAVORITE);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        Log.d(LOG_TAG, "Opening...");

        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "Upgrading...");

        db.execSQL(SQL_DELETE_TABLE_PLAYLIST);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "Downgrading...");

        onUpgrade(db, oldVersion, newVersion);
    }
}