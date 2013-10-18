package com.example.wokabstar;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class TrnrDbHelper extends SQLiteOpenHelper {
    
    /* Inner class that defines the table contents */
    public static abstract class TrnrEntry implements BaseColumns {
        public static final String TABLE_NAME = "TDict";
        public static final String COLUMN_NAME_ARTIKEL = "art";
        public static final String COLUMN_NAME_IN_WORD = "in_word";
        public static final String COLUMN_NAME_OUT_WORD = "out_word";
        public static final String COLUMN_NAME_STATE = "state";
        public static final String COLUMN_NAME_LEVEL = "level";
    }
    
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 5;
    public static final String DATABASE_NAME = "Trnr.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String CHAR_TYPE = " CHAR";
    private static final String INT_TYPE = " INT";
    private static final String COMMA_SEP = ",";
    
    private static final String SQL_CREATE_DICT =
        "CREATE TABLE " + TrnrEntry.TABLE_NAME + " (" +
        TrnrEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
        TrnrEntry.COLUMN_NAME_ARTIKEL + CHAR_TYPE + "(1)" + COMMA_SEP +
        TrnrEntry.COLUMN_NAME_IN_WORD + TEXT_TYPE + COMMA_SEP +
        TrnrEntry.COLUMN_NAME_OUT_WORD + TEXT_TYPE + COMMA_SEP +
        TrnrEntry.COLUMN_NAME_STATE + INT_TYPE + COMMA_SEP +
        TrnrEntry.COLUMN_NAME_LEVEL + INT_TYPE +
        " )";

    private static final String SQL_DELETE_DICT =
        "DROP TABLE IF EXISTS " + TrnrEntry.TABLE_NAME;

    public TrnrDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_DICT);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_DICT);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
