package com.example.wokabstar;

import java.util.Locale;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.annotation.TargetApi;

import com.example.wokabstar.TrnrDbHelper.DbEn;

public class MainActivity  extends android.support.v7.app.ActionBarActivity {

    private ImageButton btnChangeOpt, btnStartTrnr, btnEditDict;
    private TextView txtHello, txtEdit;
    private TrnrDbHelper mDbHelper;
    private SQLiteDatabase db;
    private boolean isEnoughWordsForSelfCheck;
    private int curLng;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }
    @TargetApi(14)
    private void init(){
        btnChangeOpt = (ImageButton)findViewById(R.id.btnChangeOpt);
        btnStartTrnr = (ImageButton)findViewById(R.id.btnStartTrnr);
        btnEditDict = (ImageButton)findViewById(R.id.btnEditDict);
        txtHello = (TextView)findViewById(R.id.txtHello);
        txtEdit = (TextView)findViewById(R.id.txtEditDict);
        txtEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                btnEditDict.setPressed(true);
                btnEditDict.performClick();
            }
        });
        TextView txtStart = (TextView)findViewById(R.id.txtStartTrnr);
        txtStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                btnStartTrnr.setPressed(true);
                btnStartTrnr.performClick();
            }
        });
        TextView txtOpt = (TextView)findViewById(R.id.txtChangeOpt);
        txtOpt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                btnChangeOpt.setPressed(true);
                btnChangeOpt.performClick();
            }
        });
        
        Typeface font = Typeface.createFromAsset(getAssets(), "Chantelli_Antiqua.ttf");
        txtHello.setTypeface(font);
        ((TextView)findViewById(R.id.txtChangeOpt)).setTypeface(font);
        ((TextView)findViewById(R.id.txtEditDict)).setTypeface(font);
        ((TextView)findViewById(R.id.txtStartTrnr)).setTypeface(font);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            actionBar.setHomeButtonEnabled(false);
        }
    }
    
    protected void onResume(){
       super.onResume();
       SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this); 
       curLng = prefs.getInt("trnr_language", 13);
       String edit_dict = getResources().getString(R.string.edit_dict);
       String txt_lng = getResources().getStringArray(R.array.languages_arrays)[curLng];
       txtEdit.setText(edit_dict.replaceAll(" ", " " + txt_lng + " "));
       mDbHelper = new TrnrDbHelper(this);
       db = mDbHelper.getReadableDatabase();
       txtHello.setText(getResources().getString(R.string.hello_world) + "\n"
               + getCurLngTotalRowsNum() + " " + getResources().getString(R.string.ma_to_remember) + ", \n" 
               + getCurLngCompletedRowsNum() + " " + getResources().getString(R.string.ma_completed) + ", \n"
               + getCurLngRowsNumToLearn() + " " + getResources().getString(R.string.ma_to_learn));
       checkPrize();
       isEnoughWordsForSelfCheck = isEnoughWordsForSelfCheck();
       db.close();
    }

    public boolean isEnoughWordsForSelfCheck(){
        String select = "SELECT "+DbEn._ID+ " FROM " + DbEn.TABLE_TDICT + " WHERE " + 
                DbEn.CN_STATE + "='4' limit 4";
        Cursor c = db.rawQuery(select, null);
        int i = c.getCount();
        c.close();
        select = "SELECT "+DbEn._ID+ " FROM " + DbEn.TABLE_TDICT + " limit 4";
        Cursor cj = db.rawQuery(select, null);
        int j = cj.getCount();
        cj.close();
        return j == 4 && i > 0;
    }

    public void checkPrize(){
        ImageView cherry1 = ((ImageView) findViewById(R.id.imgCherry1));
        ImageView cherry2 = ((ImageView) findViewById(R.id.imgCherry2));
        ImageView cherry3 = ((ImageView) findViewById(R.id.imgCherry3));
        cherry1.setVisibility((getCompletedRowsNum()/100 > 0) ? 0 : 4);
        cherry2.setVisibility((getCompletedRowsNum()/500 > 0) ? 0 : 4);
        cherry3.setVisibility((getCompletedRowsNum()/1000 > 0) ? 0 : 4);
    }
    
    public int getCurLngTotalRowsNum(){
        String select = "SELECT "+DbEn._ID+ " FROM " + DbEn.TABLE_TDICT + " WHERE " + DbEn.CN_LNG + "=" + curLng;
        Cursor c = db.rawQuery(select, null);
        int i = c.getCount();
        c.close();
        return i;
    }

    public int getCurLngCompletedRowsNum(){
        String select = "SELECT "+DbEn._ID + " FROM " + DbEn.TABLE_TDICT + " WHERE " + 
                DbEn.CN_STATE + ">='4' and " + DbEn.CN_LNG + "=" + curLng;
        Cursor c = db.rawQuery(select, null);
        int i = c.getCount();
        c.close();
        return i;
    }
    
    public int getCompletedRowsNum(){
        String select = "SELECT "+DbEn._ID + " FROM " + DbEn.TABLE_TDICT + " WHERE " + 
                DbEn.CN_STATE + ">='4'";
        Cursor c = db.rawQuery(select, null);
        int i = c.getCount();
        c.close();
        return i;
    }

    public int getCurLngRowsNumToLearn(){
        String select = "SELECT "+DbEn._ID+ " FROM " + DbEn.TABLE_TDICT + " WHERE " + 
                DbEn.CN_STATE + "<'4' and " + DbEn.CN_LNG + "=" + curLng;
        Cursor c = db.rawQuery(select, null);
        int i = c.getCount();
        c.close();
        return i;
    }
    
    public void showStatistics(){
        db = mDbHelper.getReadableDatabase();
        String sql = 
                "Select m.language , totalWords, newWords, " + 
                        " learnedWords, rememberedWords " +
    " From (Select " + DbEn.CN_LNG + " as language, count(*) totalWords " + 
                " from " + DbEn.TABLE_TDICT + " group by language) m " +
    " left join (Select " + DbEn.CN_LNG + " as language, count(*) newWords " + 
                " from " + DbEn.TABLE_TDICT + " where state < 4 group by language " +
    " ) new_w on m.language = new_w.language " +
    " left join (Select " + DbEn.CN_LNG + " as  language, count(*) rememberedWords " + 
                " from " + DbEn.TABLE_TDICT + " where state > 4 group by language " +
    " ) rem_w  on m.language = rem_w.language " +
    " left join (Select " + DbEn.CN_LNG + " as language, count(*) learnedWords " +
                " from " + DbEn.TABLE_TDICT + " where state = 4 group by language " +
    " ) lrn_w on m.language = lrn_w.language order by m.language";
        Cursor c = db.rawQuery(sql, null);
        if (c.getCount() > 0){
        String[] langs = getResources().getStringArray(R.array.languages_arrays);
        String statistic = "";
        c.moveToFirst();
            do{
                int lng_i = c.getInt(c.getColumnIndex("language"));
                int total_i = c.getInt(c.getColumnIndex("totalWords"));
                int neww_i = c.getInt(c.getColumnIndex("newWords"));
                int lrnw_i = c.getInt(c.getColumnIndex("learnedWords"));
                int remw_i = c.getInt(c.getColumnIndex("rememberedWords"));
                statistic += langs[lng_i].toUpperCase(Locale.getDefault()) + ":\n" +
            "   " + total_i + getResources().getString(R.string.ma_to_remember) + ", \n" +
            "   " + neww_i + getResources().getString(R.string.ma_to_learn) + ", \n" +
            "   " + lrnw_i + getResources().getString(R.string.ma_to_repeat) + ", \n" +
            "   " + remw_i + getResources().getString(R.string.ma_completed) + "; \n";
            }while(c.moveToNext());
            LayoutInflater inflater = LayoutInflater.from(this);
            View view = inflater.inflate(R.layout.alert_long_msg, null);
            TextView textview = (TextView) view.findViewById(R.id.textmsg);
            textview.setText(statistic);
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);  
            alertDialog.setTitle(getResources().getString(R.string.ma_statistic)); 
            alertDialog.setView(view);
            alertDialog.setCancelable(true);
            AlertDialog alert = alertDialog.create();
            alert.show();
        } else {
            StarUtility.showInfo(this, getResources().getString(R.string.edit_dict));
        }
        c.close();
        db.close();
    }
    
    public void onClickEditDict(View v) {
        Intent intent = new Intent(this, WorkOnDictActivity.class);
        startActivity(intent);
    }

    public void onClickChangeOpt(View v) {
        Intent intent = new Intent(this, OptionsActivity.class);
        intent.putExtra("isEnoughWordsForSelfCheck",isEnoughWordsForSelfCheck);
        startActivity(intent);
    }

    public void onClickStartTrnr(View v) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this); 
        boolean trnrMode = prefs.getBoolean("mode_learn", true);
        Intent intent = (trnrMode ) ? new Intent(this, TrainingActivity.class) :
                new Intent(this, SelfCheckActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_stat:
            showStatistics();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
