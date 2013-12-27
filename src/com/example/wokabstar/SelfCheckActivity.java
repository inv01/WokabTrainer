package com.example.wokabstar;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

import com.example.wokabstar.TrnrDbHelper.TrnrEntry;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class SelfCheckActivity extends android.support.v7.app.ActionBarActivity {
    private DictWord currentWord;
    private TextView txtTargetWord;
    private TreeMap<String, DictWord> tm;
    private Iterator<DictWord> dictItr;
    private Typeface btnFont, baseFont,acFont;
    private RadioButton rb1, rb2, rb3, rb4;
    private RadioGroup rgOptions;
    private Drawable baseBG;
    private int last_id;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_check);
        init();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.self_check, menu);
        return true;
    }

    public void init(){
        txtTargetWord = (TextView) findViewById(R.id.txtTargetWord);
        rb1 = (RadioButton) findViewById(R.id.rb1);
        rb2 = (RadioButton) findViewById(R.id.rb2);
        rb3 = (RadioButton) findViewById(R.id.rb3);
        rb4 = (RadioButton) findViewById(R.id.rb4);
        rgOptions = (RadioGroup) findViewById(R.id.rgOptions);
        baseFont = rb1.getTypeface();
        acFont = Typeface.createFromAsset(getAssets(), "alpha_echo.ttf");
        btnFont = Typeface.createFromAsset(getAssets(), "Chantelli_Antiqua.ttf");
        baseBG = rb1.getBackground();
        ((TextView) findViewById(R.id.txtMoveBackDesc)).setTypeface(btnFont);
    }
    
    public void onStart(){
        super.onStart();
        tm = StarUtility.getWordsToRepeat(PreferenceManager.getDefaultSharedPreferences(this),
                                            new TrnrDbHelper(this), getApplicationContext(), 
                                            getResources().getString(R.string.ta_nowords_alert_message));
        Collection<DictWord> colection = tm.values();
        dictItr = colection.iterator();
        setWordToRepeat();
    }

    public void onClickMoveToTraining(View v){
        currentWord.setState(0);
        setWordToRepeat();
    }
    
    public void setWordToRepeat(){
        currentWord = StarUtility.getNextDictWord(dictItr);
        if (currentWord.getForeignWord().length() == 0) {
            onBackPressed();
        } else last_id = currentWord.get_id();
        reflectTargetWord();
    }
    
    public void onOptionSelected(View v){
        checkIfRightChoise((RadioButton) v);
        
        Handler handler = new Handler(); 
        handler.postDelayed(new Runnable() { 
             public void run() {
                 setWordToRepeat();
             } 
        }, 2000);
    }

    public void checkIfRightChoise(RadioButton rb){
        if (!rb.getText().equals(currentWord.getTranslation())){
            mistakeReact();
            rb.setBackgroundColor(Color.parseColor("#FFA500"));
        }
        for (int i = 0; i < rgOptions.getChildCount(); i++){
            RadioButton rbi = (RadioButton) rgOptions.getChildAt(i);
            if (rbi.getText().equals(currentWord.getTranslation())){
                rbi.setBackgroundColor(Color.parseColor("#7FFFD4"));
            }
        }
    }
    
    public void mistakeReact() {
        currentWord.setState(0);
        ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE))
        .vibrate(500);
    }
    
    public void reflectTargetWord(){
        TreeMap<String, DictWord> op_tm = StarUtility.getOptionsForDictWord(currentWord, 
                new TrnrDbHelper(this), getApplicationContext(), 
                getResources().getString(R.string.ta_nowords_alert_message));
        if (op_tm.size() < 3) {
            onBackPressed();
        }
        
        String symb = StarUtility.getOrderedSymbols(currentWord.getForeignWord().toUpperCase());
        StarUtility.setFont(symb, acFont, "alpha_echo", baseFont, (new Object[] {txtTargetWord}));
        String art = "";
        switch(currentWord.getArt()){
        case TrnrEntry.TYPE_MASCULINE: art = getResources().getString(R.string.mArt); break;
        case TrnrEntry.TYPE_FEMININE: art = getResources().getString(R.string.fArt); break;
        case TrnrEntry.TYPE_NEUTRAL: art = getResources().getString(R.string.nArt); break;
        }
        txtTargetWord.setText(art + " " + currentWord.getForeignWord().toUpperCase());
        
        Iterator<DictWord> op_itr = op_tm.values().iterator();
        DictWord dw1 = StarUtility.getNextDictWord(op_itr);
        DictWord dw2 = StarUtility.getNextDictWord(op_itr);
        DictWord dw3 = StarUtility.getNextDictWord(op_itr);
        String strOptions = currentWord.getTranslation() + dw1.getTranslation() + dw2.getTranslation() + 
                dw3.getTranslation();
        symb = StarUtility.getOrderedSymbols(strOptions);
        StarUtility.setFont(symb, btnFont, "Chantelli_Antiqua", baseFont, (new Object[] {rgOptions}));
        //Min + (int)(Math.random() * ((Max - Min) + 1))
        String opts[] = new String[]{currentWord.getTranslation(),dw1.getTranslation(),
                dw2.getTranslation(),dw3.getTranslation()};
        int rand_move = (int)(Math.random() * 4);
        int j=0;
        for (int i = rand_move; i < 4; i++){
            RadioButton rb = (RadioButton) rgOptions.getChildAt(i);
            rb.setBackground(baseBG);
            rb.setChecked(false);
            rb.setText(opts[j++]);
        }
        for (int i = 0; i < rand_move; i++){
            RadioButton rb = (RadioButton) rgOptions.getChildAt(i);
            rb.setBackground(baseBG);
            rb.setChecked(false);
            rb.setText(opts[j++]);
        }
    }

    public void onPause(){
        super.onPause();
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = app_preferences.edit();
        editor.putInt("last_id", last_id);
        editor.commit();
        StarUtility.updateWordState(new TrnrDbHelper(this), tm);
    }
}
