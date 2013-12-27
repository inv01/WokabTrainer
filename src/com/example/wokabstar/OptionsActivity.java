package com.example.wokabstar;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

public class OptionsActivity extends android.support.v7.app.ActionBarActivity {

    private Spinner sp_wordLevel, sp_numwords;
    private CheckBox checkBox;
    private RadioButton rb1, rb2;
    
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
        ((TextView)findViewById(R.id.txtNumber_to_repeat)).setTypeface(font);
        ((TextView)findViewById(R.id.txtLevel_prompt)).setTypeface(font);
        
        sp_wordLevel = (Spinner) findViewById(R.id.spinner1);
        sp_numwords = (Spinner) findViewById(R.id.spinner2);
        checkBox = (CheckBox) findViewById(R.id.checkbox_strickt);
        rb1 = (RadioButton) findViewById(R.id.rb1);
        rb2 = (RadioButton) findViewById(R.id.rb2);
        rb1.setTypeface(font);
        rb2.setTypeface(font);
        rb2.setEnabled(getIntent().getBooleanExtra("isEnoughWordsForSelfCheck", false));
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this); 
        checkBox.setChecked(prefs.getBoolean("strikt_level", false));
        sp_wordLevel.setSelection(prefs.getInt("word_level", 0));
        sp_numwords.setSelection(prefs.getInt("word_number", 0));
        rb1.setChecked(prefs.getBoolean("mode_learn", true));
        rb2.setChecked(!prefs.getBoolean("mode_learn", false));
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options, menu);
        return true;
    }

    protected void onPause(){
        super.onPause();
        SharedPreferences app_preferences = 
                PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = app_preferences.edit();
        editor.putBoolean("strikt_level", checkBox.isChecked());
        editor.putBoolean("mode_learn", rb1.isChecked());
        editor.putInt("word_level", (int) sp_wordLevel.getSelectedItemId());
        editor.putInt("word_number", (int) sp_numwords.getSelectedItemId());
        editor.commit();
    }
}
