package com.ninadelou.mediaplayer.Data.Access;

import android.provider.BaseColumns;

public class FavoriteData implements BaseColumns {
    public static final String TABLE = "favorite";

    public static final String COLUMN_FAVPLAY = "id_favplay";
    public static final int NUM_COLUMN_FAVPLAY = 0;
    public static final String COLUMN_FAVLONG = "favlongitude";
    public static final int NUM_COLUMN_FAVLONG = 1;
    public static final String COLUMN_FAVLAT = "favlatitude";
    public static final int NUM_COLUMN_FAVLAT = 2;
}