package com.example.wokabstar;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

public class OptionsActivity extends android.support.v7.app.ActionBarActivity {

    private Spinner sp_wordLevel, sp_numwords, sp_languages;
    private CheckBox checkBox, artCheckBox;
    private RadioButton rb1, rb2, rb3, rb4;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        init();
    }
    
    public void init(){
        Typeface font = Typeface.createFromAsset(getAssets(), "Chantelli_Antiqua.ttf");
        ((TextView)findViewById(R.id.txtStriktLevel)).setTypeface(font);
        ((TextView)findViewById(R.id.txtTrnrArt)).setTypeface(font);
        ((TextView)findViewById(R.id.txtNumber_to_repeat)).setTypeface(font);
        ((TextView)findViewById(R.id.txtLevel_prompt)).setTypeface(font);
        ((TextView)findViewById(R.id.txtImpMode)).setTypeface(font);
        ((TextView)findViewById(R.id.txtLanguage)).setTypeface(font);
        
        sp_wordLevel = (Spinner) findViewById(R.id.spinner1);
        sp_numwords = (Spinner) findViewById(R.id.spinner2);
        sp_languages = (Spinner) findViewById(R.id.spinner3);
        
        checkBox = (CheckBox) findViewById(R.id.checkbox_strickt);
        artCheckBox = (CheckBox) findViewById(R.id.checkbox_art);
        
        rb1 = (RadioButton) findViewById(R.id.rb1);
        rb2 = (RadioButton) findViewById(R.id.rb2);
        rb1.setTypeface(font);
        rb2.setTypeface(font);
        rb2.setEnabled(getIntent().getBooleanExtra("isEnoughWordsForSelfCheck", false));
        
        rb3 = (RadioButton) findViewById(R.id.rb3);
        rb4 = (RadioButton) findViewById(R.id.rb4);
        rb3.setTypeface(font);
        rb4.setTypeface(font);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this); 
        checkBox.setChecked(prefs.getBoolean("strikt_level", false));
        artCheckBox.setChecked(prefs.getBoolean("no_art_mode", false));
        sp_wordLevel.setSelection(prefs.getInt("word_level", 0));
        sp_numwords.setSelection(prefs.getInt("word_number", 0));
        sp_languages.setSelection(prefs.getInt("trnr_language", 13));
        rb1.setChecked(prefs.getBoolean("mode_learn", true));
        rb2.setChecked(!rb1.isChecked());
        rb3.setChecked(prefs.getBoolean("mode_import", true));
        rb4.setChecked(!rb3.isChecked());
    }
    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    protected void onPause(){
        super.onPause();
        SharedPreferences app_preferences = 
                PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = app_preferences.edit();
        String[] lng_arts = getResources().getStringArray(R.array.lang_articles_arrays);
        editor.putString("lng_arts", lng_arts[(int) sp_languages.getSelectedItemId()]);
        editor.putBoolean("strikt_level", checkBox.isChecked());
        editor.putBoolean("no_art_mode", artCheckBox.isChecked());
        editor.putBoolean("mode_learn", rb1.isChecked());
        editor.putBoolean("mode_import", rb3.isChecked());
        editor.putInt("word_level", (int) sp_wordLevel.getSelectedItemId());
        editor.putInt("word_number", (int) sp_numwords.getSelectedItemId());
        editor.putInt("trnr_language", (int) sp_languages.getSelectedItemId());
        editor.commit();
    }
}
