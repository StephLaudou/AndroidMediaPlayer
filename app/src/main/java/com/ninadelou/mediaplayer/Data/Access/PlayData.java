package com.ninadelou.mediaplayer.Data.Access;

import android.provider.BaseColumns;

public class PlayData implements BaseColumns {
    public static final String TABLE = "playlist";

    public static final String COLUMN_ID = "id_play";
    public static final int NUM_COLUMN_ID = 0;
    public static final String COLUMN_NAME = "name";
    public static final int NUM_COLUMN_NAME = 1;
    public static final String COLUMN_AUTHOR = "author";
    public static final int NUM_COLUMN_AUTHOR = 2;
    public static final String COLUMN_RECORD = "record";
    public static final int NUM_COLUMN_RECORD = 3;
    public static final String COLUMN_URL = "url";
    public static final int NUM_COLUMN_URL = 4;
    public static final String COLUMN_FILE = "file";
    public static final int NUM_COLUMN_FILE = 5;
}