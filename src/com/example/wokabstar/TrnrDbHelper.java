package com.example.wokabstar;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class TrnrDbHelper extends SQLiteOpenHelper {
    
    /* Inner class that defines the table contents */
    public static abstract class TrnrEntry implements BaseColumns {
        public static final String TABLE_TDICT = "TDict";
        public static final String COLUMN_NAME_ARTIKEL = "art";
        public static final String COLUMN_NAME_IN_WORD = "in_word";
        public static final String COLUMN_NAME_OUT_WORD = "out_word";
        public static final String COLUMN_NAME_STATE = "state";
        public static final String COLUMN_NAME_LEVEL = "level";
        
        public static final char TYPE_MASCULINE = 'm';
        public static final char TYPE_FEMININE = 'f';
        public static final char TYPE_NEUTRAL = 'n';
        public static final char TYPE_ADJECTIVE = 'a';
        public static final char TYPE_VERB = 'v';
        public static final char TYPE_OTHER = 'o';
        
        public static final int LEVEL_A1 = 0;
        public static final int LEVEL_A2 = 1;
        public static final int LEVEL_B1 = 2;
        public static final int LEVEL_B2 = 3;
        public static final int LEVEL_C1 = 4;
        public static final int LEVEL_C2 = 5;
    }
    
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 5;
    public static final String DATABASE_NAME = "Trnr.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String CHAR_TYPE = " CHAR";
    private static final String INT_TYPE = " INT";
    private static final String COMMA_SEP = ",";
    
    private static final String SQL_CREATE_DICT =
        "CREATE TABLE " + TrnrEntry.TABLE_TDICT + " (" +
        TrnrEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
        TrnrEntry.COLUMN_NAME_ARTIKEL + CHAR_TYPE + "(1)" + COMMA_SEP +
        TrnrEntry.COLUMN_NAME_IN_WORD + TEXT_TYPE + COMMA_SEP +
        TrnrEntry.COLUMN_NAME_OUT_WORD + TEXT_TYPE + COMMA_SEP +
        TrnrEntry.COLUMN_NAME_STATE + INT_TYPE + COMMA_SEP +
        TrnrEntry.COLUMN_NAME_LEVEL + INT_TYPE +
        " )";

    private static final String SQL_DELETE_DICT =
        "DROP TABLE IF EXISTS " + TrnrEntry.TABLE_TDICT;

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

    public String[] getWordsMatchingQuery(SQLiteDatabase db, String in_word){
        if (in_word.length() < 3) { return new String[] {};}
        String select = "SELECT "+ TrnrEntry._ID + ", "
                + TrnrEntry.COLUMN_NAME_ARTIKEL + ", " + TrnrEntry.COLUMN_NAME_IN_WORD + ", "
                + TrnrEntry.COLUMN_NAME_OUT_WORD + ", " + TrnrEntry.COLUMN_NAME_LEVEL 
                + " FROM " + TrnrEntry.TABLE_TDICT
                + " WHERE " + TrnrEntry.COLUMN_NAME_IN_WORD + " LIKE '" + in_word + "%'";
        Cursor cursor = db.rawQuery(select, null);
        if(cursor.getCount() > 0)
        {
            String[] str = new String[cursor.getCount()];
            int i = 0;
            while (cursor.moveToNext()){
                 str[i] = cursor.getString(cursor.getColumnIndex(TrnrEntry.COLUMN_NAME_IN_WORD));
                 i++;
             }
            return str;
        }
        else {return new String[] {};}
    }
    
    public void onRemoveRecord(SQLiteDatabase db, int selectedID){
        db.execSQL("DELETE FROM " + TrnrEntry.TABLE_TDICT + " WHERE " + TrnrEntry._ID + "=" + selectedID);
    }
}
