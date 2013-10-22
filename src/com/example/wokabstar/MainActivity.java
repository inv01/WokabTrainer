package com.example.wokabstar;

import com.example.wokabstar.TrnrDbHelper.TrnrEntry;

import android.app.ActionBar;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


public class MainActivity  extends android.support.v7.app.ActionBarActivity {

    private ImageButton btnChangeOpt, btnStartTrnr, btnEditDict;
    private TextView txtHello;
    private TrnrDbHelper mDbHelper;
    private SQLiteDatabase db;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            actionBar.setHomeButtonEnabled(false);
        }
    }
    private void init(){
        btnChangeOpt = (ImageButton)findViewById(R.id.btnChangeOpt);
        btnStartTrnr = (ImageButton)findViewById(R.id.btnStartTrnr);
        btnEditDict = (ImageButton)findViewById(R.id.btnEditDict);
        txtHello = (TextView)findViewById(R.id.txtHello);
    }
    
    protected void onResume(){
       super.onResume();
       mDbHelper = new TrnrDbHelper(this);
       db = mDbHelper.getReadableDatabase();
       txtHello.setText(getResources().getString(R.string.hello_world) + "\n"
               + getTotalRowsNum() + " " + getResources().getString(R.string.ma_to_remember) + ", \n" 
               + getCompletedRowsNum() + " " + getResources().getString(R.string.ma_completed) + ", \n"
               + getRepeatRowsNum() + " " + getResources().getString(R.string.ma_to_repeat));
       db.close();
    }
    
    public int getTotalRowsNum(){
        String select = "SELECT "+TrnrEntry._ID+ " FROM " + TrnrEntry.TABLE_TDICT;
        Cursor c = db.rawQuery(select, null);
        int i = c.getCount();
        c.close();
        return i;
    }

    public int getCompletedRowsNum(){
        String select = "SELECT "+TrnrEntry._ID + " FROM " + TrnrEntry.TABLE_TDICT + " WHERE " + 
                TrnrEntry.COLUMN_NAME_STATE + "='1'";
        Cursor c = db.rawQuery(select, null);
        int i = c.getCount();
        c.close();
        return i;
    }

    public int getRepeatRowsNum(){
        String select = "SELECT "+TrnrEntry._ID+ " FROM " + TrnrEntry.TABLE_TDICT + " WHERE " + 
                TrnrEntry.COLUMN_NAME_STATE + "='0'";
        Cursor c = db.rawQuery(select, null);
        int i = c.getCount();
        c.close();
        return i;
    }
    public void onClickEditDict(View v) {
        Intent intent = new Intent(this, WorkOnDictActivity.class);
        startActivity(intent);
    }

    public void onClickChangeOpt(View v) {
    }

    public void onClickStartTrnr(View v) {
        Intent intent = new Intent(this, TrainingActivity.class);
        startActivity(intent);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
