package com.example.wokabstar;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;
import static com.example.wokabstar.StarUtility.getOrdSymbols;
import static com.example.wokabstar.StarUtility.getNextDictWord;

import com.example.wokabstar.TrnrDbHelper.DbEn;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class SelfCheckActivity extends android.support.v7.app.ActionBarActivity {
    private DictWord currentWord;
    private TextView txtTargetWord;
    private TreeMap<String, DictWord> tm;
    private Iterator<DictWord> dictItr;
    private Typeface btnFont, baseFont,acFont;
    private RadioButton rb1;
    private RadioGroup rgOptions;
    private Drawable baseBG;
    private int last_id;
    private static final boolean isCurApiVersionG15 = android.os.Build.VERSION.SDK_INT  >= android.os.Build.VERSION_CODES.JELLY_BEAN;
    
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
    
    public void init(){
        txtTargetWord = (TextView) findViewById(R.id.txtTargetWord);
        rb1 = (RadioButton) findViewById(R.id.rb1);
        rgOptions = (RadioGroup) findViewById(R.id.rgOptions);
        baseFont = rb1.getTypeface();
        acFont = Typeface.createFromAsset(getAssets(), "alpha_echo.ttf");
        btnFont = Typeface.createFromAsset(getAssets(), "Chantelli_Antiqua.ttf");
        baseBG = rb1.getBackground();
        ((TextView) findViewById(R.id.txtIknowThisWord)).setTypeface(btnFont);
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

    public void onClickExcludeFromTraining(View v){
        currentWord.setState(currentWord.getState() + 10);
        setWordToRepeat();
    }
    
    public void setWordToRepeat(){
        currentWord = getNextDictWord(dictItr);
        if (currentWord.getForeignWord().equals("")) {
            onBackPressed();
            return;
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
            rb.setBackgroundColor(getResources().getColor(R.color.orange_color));
        }
        for (int i = 0; i < rgOptions.getChildCount(); i++){
            RadioButton rbi = (RadioButton) rgOptions.getChildAt(i);
            if (rbi.getText().equals(currentWord.getTranslation())){
                rbi.setBackgroundColor(getResources().getColor(R.color.green_color));
            }
        }
    }
    
    public void mistakeReact() {
        currentWord.setState(0);
        ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE))
        .vibrate(500);
    }
    @TargetApi(16)
    public void reflectTargetWord(){
        TreeMap<String, DictWord> op_tm = StarUtility.getOptionsForDictWord(currentWord, 
                new TrnrDbHelper(this), getApplicationContext(), 
                getResources().getString(R.string.ta_nowords_alert_message));
        if (op_tm.size() < 3) {
            onBackPressed();
        }
        
        String symb = getOrdSymbols(currentWord.getForeignWord());
        StarUtility.setFont(symb, acFont, "alpha_echo", baseFont, (new Object[] {txtTargetWord}));
        
        switch(currentWord.getArt()){
        case DbEn.TYPE_MASCULINE: 
            txtTargetWord.setShadowLayer(2, 1, 1,
                getResources().getColor(R.color.blue_shadow));
            break;
        case DbEn.TYPE_FEMININE: 
            txtTargetWord.setShadowLayer(2, 1, 1,
                getResources().getColor(R.color.pink_shadow));
            break;
        case DbEn.TYPE_NEUTRAL: 
            txtTargetWord.setShadowLayer(2, 1, 1,
                getResources().getColor(R.color.gold_shadow));
            break;
        default:
            txtTargetWord.setShadowLayer(0, 0, 0, 0);
            break;
        }
        String art = StarUtility.getUIArt(currentWord.getArt(),this);
        if(art.length() > 0) art += " ";
        txtTargetWord.setText(art + currentWord.getForeignWord().toUpperCase(Locale.getDefault()));
        Iterator<DictWord> op_itr = op_tm.values().iterator();
        DictWord dw1 = getNextDictWord(op_itr);
        DictWord dw2 = getNextDictWord(op_itr);
        DictWord dw3 = getNextDictWord(op_itr);
        String strOptions = currentWord.getTranslation() + dw1.getTranslation() + dw2.getTranslation() + 
                dw3.getTranslation();
        symb = getOrdSymbols(strOptions);
        StarUtility.setFont(symb, btnFont, "Chantelli_Antiqua", baseFont, (new Object[] {rgOptions}));
        //Min + (int)(Math.random() * ((Max - Min) + 1))
        String opts[] = new String[]{currentWord.getTranslation(),dw1.getTranslation(),
                dw2.getTranslation(),dw3.getTranslation()};
        int rand_move = (int)(Math.random() * 4);
        int j=0;
        for (int i = rand_move; i < 4; i++){
            RadioButton rb = (RadioButton) rgOptions.getChildAt(i);
            if (isCurApiVersionG15){rb.setBackground(baseBG);
            }else {rb.setBackgroundColor(getResources().getColor(R.color.base_color));}
            rb.setChecked(false);
            rb.setText(opts[j++]);
        }
        for (int i = 0; i < rand_move; i++){
            RadioButton rb = (RadioButton) rgOptions.getChildAt(i);
            if (isCurApiVersionG15){rb.setBackground(baseBG);
            }else {rb.setBackgroundColor(getResources().getColor(R.color.base_color));}
            rb.setChecked(false);
            rb.setText(opts[j++]);
        }
    }

    public void onPause(){
        super.onPause();
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(app_preferences.getBoolean("strat_again", false)) last_id = 0;
        SharedPreferences.Editor editor = app_preferences.edit();
        editor.putInt("last_id", last_id);
        editor.commit();
        StarUtility.updateWordState(new TrnrDbHelper(this), tm);
    }
}
