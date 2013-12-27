package com.example.wokabstar;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar.LayoutParams;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wokabstar.TrnrDbHelper.TrnrEntry;

public class TrainingActivity extends android.support.v7.app.ActionBarActivity {

    DictWord currentWord;
    TextView tvTranslate, tvFWord, tvState, btnNotNoun;
    LinearLayout line1, line2, lineArt;
    TreeMap<String, DictWord> tm;
    Iterator<DictWord> dictItr;
    boolean isArtClicked, isWasMistake;
    String adtLongWordSymbs;
    Typeface btnFont, baseFont,acFont;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        init();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    public void init(){
        lineArt = (LinearLayout)findViewById(R.id.linearLayout_TransLettersLine1);
        line1 = (LinearLayout)findViewById(R.id.linearLayout_TransLettersLine2);
        line2 = (LinearLayout)findViewById(R.id.linearLayout_TransLettersLine3);
        btnNotNoun = (TextView)findViewById(R.id.btnNotNoun);
        btnNotNoun.setPaintFlags(btnNotNoun.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        tvTranslate = (TextView)findViewById(R.id.txtTargetWord);
        tvFWord = (TextView)findViewById(R.id.txtResultWord);
        tvState = (TextView)findViewById(R.id.txtShowState);

        baseFont = tvState.getTypeface();
        acFont = Typeface.createFromAsset(getAssets(), "alpha_echo.ttf");
        btnFont = Typeface.createFromAsset(getAssets(), "Chantelli_Antiqua.ttf");
        ((TextView)findViewById(R.id.txtShowResult)).setTypeface(btnFont);
        tvState.setTypeface(btnFont);
    }

    public boolean isRightSymbolPicked(String string){
        int index = tvFWord.getText().toString().indexOf("*");
        return string.equals("" + currentWord.getForeignWord().charAt(index));
    }

    public boolean isSimilarSymbolsLeft(String string) {
        int index = tvFWord.getText().toString().indexOf("*");
        if (index > -1)
        return currentWord.getForeignWord()
                .substring(index).indexOf(string) > -1;
        return false;
    }

    public void onClickLetter(TextView btn){
        if (isRightSymbolPicked(btn.getText().toString())){
            StringBuilder resultWord = new StringBuilder(tvFWord.getText().toString());
            int index = resultWord.indexOf("*");
            resultWord.setCharAt(index, btn.getText().charAt(0));
            tvFWord.setText(resultWord);

            if (!isSimilarSymbolsLeft(btn.getText().toString())){
                if (adtLongWordSymbs.length() > 0){
                    btn.setText("" + adtLongWordSymbs.charAt(0));
                    adtLongWordSymbs = (adtLongWordSymbs.length() > 1) ? 
                                            adtLongWordSymbs.substring(1) : "";
                } else btn.setEnabled(false);
            }
            updateStatus_getNewWord();
        } else mistakeReact();
    }

    public void updateStatus_getNewWord(){
        if((new StringBuilder(tvFWord.getText().toString()))
                .indexOf("*") < 0 && isArtClicked){
            if (!isWasMistake) {
                int newState = currentWord.getState() + 1;
                currentWord.setState(newState);
                int num_req_repeat = 4 - newState;
                tvState.setText("" + num_req_repeat + " " + getResources().getString(R.string.left_number));
            }
            Handler handler = new Handler(); 
            handler.postDelayed(new Runnable() { 
                 public void run() {
                     setWordToRepeat();
                 } 
            }, 1000);
        }
    }

    public void showTargetLetters(){
        line1.removeAllViews();
        line2.removeAllViews();

        String allMixSymbols = StarUtility.getOrderedSymbols(currentWord.getForeignWord().toUpperCase());
        String first8Letters = allMixSymbols;
        adtLongWordSymbs = "";
        if (allMixSymbols.length() > 8){
            adtLongWordSymbs = allMixSymbols.substring(8);
            first8Letters = allMixSymbols.substring(0, 8);
        }
        
        int len = first8Letters.length();
        //Min + (int)(Math.random() * ((Max - Min) + 1))
        int rand_move = (int)(Math.random() * len);
        first8Letters = first8Letters.substring(rand_move) + first8Letters.substring(0, rand_move);
        
        for(int i = 0; i < len; i++){
            TextView btn = new TextView(this);
            btn.setText("" + first8Letters.charAt(i));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.setMargins(5, 0, 0, 0);
            btn.setLayoutParams(params);
            btn.setClickable(true);
            btn.setGravity(17);//center
            btn.setTypeface(btnFont, Typeface.BOLD);
            btn.setBackgroundResource(R.drawable.btn_art_draw);
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickLetter((TextView) v);
                }
            });
            if(len < 4 || i < len/2) line1.addView(btn);
            else line2.addView(btn);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.training, menu);
        return super.onCreateOptionsMenu(menu);
    }

    protected void onStart(){
        super.onStart();
        selectWordsToRepeat();
        setWordToRepeat();
     }

    public void setWordToRepeat(){
        currentWord = getNextDictWord();
        if (currentWord.getForeignWord().length() == 0) {
            onBackPressed();
        }
        reflectTargetWord();
        resetArt();/*depends on reflectTargetWord();*/
        int num_req_repeat = 4 - currentWord.getState();
        tvState.setText("" + num_req_repeat + " " + getResources().getString(R.string.left_number));
        isWasMistake = false;
    }

    public void reflectTargetWord(){
        if (currentWord.getForeignWord().length() == 0) return;
        
        btnFont = Typeface.createFromAsset(getAssets(), "Chantelli_Antiqua.ttf");
        btnFont = StarUtility.setFonts(currentWord, acFont, btnFont, 
                baseFont, (new Object[] {tvTranslate, tvFWord}));
        
        tvTranslate.setText(currentWord.getTranslation().toUpperCase());
        String s = "";
        String fWord = currentWord.getForeignWord();
        for (int i = 0; i < fWord.length(); i++){ 
            s += (Character.valueOf(fWord.charAt(i)) != ' ') ? "*" : " ";
        }
        tvFWord.setText(s);
        showTargetLetters();
    }

    public void selectWordsToRepeat() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this); 
        String levelComparison = (prefs.getBoolean("strikt_level", false)) ? "=" : "<=";
        String word_level = "" + prefs.getInt("word_level", 0);
        int word_number = prefs.getInt("word_number", 0);
        word_number = (word_number > 1) ? word_number * 10 : 
                                        ((word_number == 0) ? 10 : 15);
        String str_word_number = "" + word_number;
        
        String sql = "SELECT * FROM " + TrnrEntry.TABLE_TDICT + 
                " WHERE state < 4 and level " + levelComparison + word_level + " order by state desc LIMIT " + 
                str_word_number;
        
        
        TrnrDbHelper mDbHelper = new TrnrDbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor c = db.rawQuery(sql, null);
        tm = new TreeMap<String, DictWord>();
        if(c.moveToFirst()){
            do{
                DictWord dw = new DictWord(c.getInt(c.getColumnIndex(TrnrEntry._ID)));
                dw.setForeignWord((c.getString(c.getColumnIndex(TrnrEntry.COLUMN_NAME_IN_WORD))).toUpperCase());
                dw.setArt(c.getString(c.getColumnIndex(TrnrEntry.COLUMN_NAME_ARTIKEL)).charAt(0));
                dw.setLevel(c.getInt(c.getColumnIndex(TrnrEntry.COLUMN_NAME_LEVEL)));
                dw.setTranslation((c.getString(c.getColumnIndex(TrnrEntry.COLUMN_NAME_OUT_WORD))).toUpperCase());
                dw.setState(c.getInt(c.getColumnIndex(TrnrEntry.COLUMN_NAME_STATE)));
                tm.put(c.getString(c.getColumnIndex(TrnrEntry._ID)), dw);
            }while(c.moveToNext());
        } else StarUtility.showInfo(getApplicationContext(), 
                getResources().getString(R.string.ta_nowords_alert_message));
        c.close();
        db.close();
        Collection<DictWord> colection = tm.values();
        dictItr = colection.iterator();
    }
    
    public DictWord getNextDictWord(){
        DictWord tword = new DictWord();
        if(dictItr.hasNext()){
            tword = dictItr.next(); 
            }
        return tword;
    }
    
    public void resetArt(){
        for (int i = 0; i < lineArt.getChildCount(); i++ ){
            TextView btn = (TextView) lineArt.getChildAt(i);
            btn.setEnabled(true);
            btn.setBackgroundResource(R.drawable.ic_light_blue);
            btn.setTypeface(btnFont);
        }
        isArtClicked = false;
    }

    public void onClickArt(View v){
        for (int i = 0; i < lineArt.getChildCount(); i++ ){
            TextView btn = (TextView) lineArt.getChildAt(i);
            btn.setEnabled(false);
            btn.setBackgroundResource(R.drawable.ic_grey);
            if (getArtFromBtnText(btn.getText().toString())
                   .equals("" + currentWord.getArt())){
                btn.setBackgroundResource(R.drawable.ic_green);
              }
        }
        boolean isNotNoun =((Character) currentWord.getArt()).equals((Character) TrnrEntry.TYPE_ADJECTIVE) 
                || ((Character) currentWord.getArt()).equals((Character) TrnrEntry.TYPE_OTHER) 
                || ((Character) currentWord.getArt()).equals((Character) TrnrEntry.TYPE_VERB); 
        if (isNotNoun) btnNotNoun.setBackgroundResource(R.drawable.ic_green);

        TextView btn = (TextView)v;
        if (getArtFromBtnText(btn.getText().toString())
                .equals("" + currentWord.getArt())
                || (isNotNoun && btn.equals(btnNotNoun))){
        } else {
            mistakeReact();
            btn.setBackgroundResource(R.drawable.ic_orange);
        }
        isArtClicked = true;
        updateStatus_getNewWord();
    }

    public String getArtFromBtnText(String string){
        if(string.equals(getResources().getString(R.string.mArt))) return "" + TrnrEntry.TYPE_MASCULINE; else 
            if(string.equals(getResources().getString(R.string.fArt))) return "" + TrnrEntry.TYPE_FEMININE; else
                if(string.equals(getResources().getString(R.string.nArt))) return "" + TrnrEntry.TYPE_NEUTRAL; 
                else return "";
    }

    public void mistakeReact() {
        currentWord.setState(0);
        ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE))
        .vibrate(500);
        isWasMistake = true;
    }

    protected void onPause(){
        super.onPause();
        updateWordState();
    }

    public void onClickShowResult(View v){
        String art = "";
        switch(currentWord.getArt()){
        case TrnrEntry.TYPE_MASCULINE: art = getResources().getString(R.string.mArt); break;
        case TrnrEntry.TYPE_FEMININE: art = getResources().getString(R.string.fArt); break;
        case TrnrEntry.TYPE_NEUTRAL: art = getResources().getString(R.string.nArt); break;
        }
        tvFWord.setText((!art.equals("") ? (art + " "): "")
                + currentWord.getForeignWord());
        currentWord.setState(0);
        tvState.setText("4 " + getResources().getString(R.string.left_number));
        //disable all buttons
        for(LinearLayout line : new LinearLayout[]{line1,line2,lineArt})
            for(int i=0; i < line.getChildCount(); i++){
                line.getChildAt(i).setEnabled(false);
                line.getChildAt(i).setBackgroundResource(R.drawable.btn_art_draw);
            }

      //get next word
        Handler handler = new Handler(); 
        handler.postDelayed(new Runnable() { 
             public void run() {
                 setWordToRepeat();
             } 
        }, 2000);
    }

    public void onRemoveClick(View v){
        if (currentWord.get_id() < 0) return;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.alert_confirm_title));
        alertDialogBuilder
            .setMessage(getResources().getString(R.string.ta_skip_alert_message))
            .setCancelable(false)
            .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    currentWord.setState(4);
                    setWordToRepeat();
                }
              })
              .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void updateWordState(){
        TrnrDbHelper mDbHelper = new TrnrDbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Collection<DictWord> colection = tm.values();
        Iterator <DictWord> itr = colection.iterator(); 
        while(itr.hasNext()){
            DictWord tword = itr.next();
            ContentValues values = new ContentValues();
            values.put(TrnrEntry.COLUMN_NAME_STATE, tword.getState());
            String selection = TrnrEntry._ID + " = ?";
            String[] selectionArgs = { String.valueOf(tword.get_id()) };
            db.update(TrnrEntry.TABLE_TDICT, values, selection, selectionArgs);
        }
        db.close();
    }
}
