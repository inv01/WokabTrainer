package com.example.wokabstar;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.example.wokabstar.TrnrDbHelper.TrnrEntry;

import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar.LayoutParams;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TrainingActivity extends android.support.v7.app.ActionBarActivity {

    private TrnrDbHelper mDbHelper;
    private SQLiteDatabase db;
    DictWord currentWord;
    TextView tv1, tv2;
    LinearLayout line1, line2, lineArt;
    Button btnNotNoun;
    TreeMap<String, DictWord> tm;
    Iterator<DictWord> dictItr;
    
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
        case R.id.menu_ShowResult : onClickShowResult(btnNotNoun);
            return true;
        case R.id.menu_Recycle : //;
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    public void init(){
        lineArt = (LinearLayout)findViewById(R.id.linearLayout_TransLettersLine1);
        line1 = (LinearLayout)findViewById(R.id.linearLayout_TransLettersLine2);
        line2 = (LinearLayout)findViewById(R.id.linearLayout_TransLettersLine3);
        btnNotNoun = (Button)findViewById(R.id.btnNotNoun);
        btnNotNoun.setPaintFlags(btnNotNoun.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        tv1 = (TextView)findViewById(R.id.txtTargetWord);
        tv2 = (TextView)findViewById(R.id.txtResultWord);
        Typeface font = Typeface.createFromAsset(getAssets(), "alpha_echo.ttf");
        tv1.setTypeface(font);
        tv2.setTypeface(font);
        //http://mobile.tutsplus.com/tutorials/android/customize-android-fonts/
    }

    public boolean isRightSymbolPicked(String string){
        int index = tv2.getText().toString().indexOf("*");
        return string.equals("" + currentWord.getForeignWord().charAt(index));
    }
    
    public boolean isSimilarSymbolsLeft(String string) {
        int index = tv2.getText().toString().indexOf("*");
        if (index > -1)
        return currentWord.getForeignWord()
                .substring(index).indexOf(string) > -1;
        return false;
    }
    
    public void onClickLetter(Button btn){
        if (isRightSymbolPicked(btn.getText().toString())){
            StringBuilder resultWord = new StringBuilder(tv2.getText().toString());
            int index = resultWord.indexOf("*");
            resultWord.setCharAt(index, btn.getText().charAt(0));
            tv2.setText(resultWord);

            if (!isSimilarSymbolsLeft(btn.getText().toString())){
                btn.setEnabled(false);
            }
        } else mistakeReact();
    }
    
    public void showTargetLetters(){
        line1.removeAllViews();
        line2.removeAllViews();

        String mixWord = getOrderedSymbols(currentWord.getForeignWord().toUpperCase());
        int len = mixWord.length();
        for(int i = 0; i < len; i++){
            Button btn = new Button(this);
            btn.setText("" + mixWord.charAt(i));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.setMargins(5, 0, 0, 0);
            btn.setLayoutParams(params);
            btn.setBackgroundResource(R.drawable.btn_art_draw);
            btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickLetter((Button) v);
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
        mDbHelper = new TrnrDbHelper(this);
        db = mDbHelper.getWritableDatabase();
        selectWordsToRepeat(10);
        currentWord = getTargetWord();
        reflectTargetWord(currentWord.getForeignWord());
     }
    
    public String getOrderedSymbols(String string){
        Set<Character> resultSet = new TreeSet<Character>();
        for (int i = 0; i < string.length(); i++) {
            resultSet.add(Character.valueOf(string.charAt(i)));
        }
        String s = "";
        Iterator<Character> i = resultSet.iterator();
        while (i.hasNext())s += i.next();

        return s;
    }
    public void reflectTargetWord(String tword){
        tv1.setText(currentWord.getTranslation().toUpperCase());
        String s = "";
        for (int i = 0; i < currentWord.getForeignWord().length(); i++)s += "*";
        tv2.setText(s);
        showTargetLetters();
    }
     
    public void selectWordsToRepeat(int count) {
        String sql = "SELECT * FROM " + TrnrEntry.TABLE_TDICT + " WHERE state < 4 order by state LIMIT " + count;
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
            }while(!c.moveToNext());
        }
        c.close();
        Collection<DictWord> colection = tm.values();
        dictItr = colection.iterator();
    }
    
    
    
    public DictWord getTargetWord(){
        DictWord tword = new DictWord();
        /*String sql = "SELECT * FROM " + TrnrEntry.TABLE_TDICT + " WHERE state=0 LIMIT 1";
        Cursor c = db.rawQuery(sql, null);
        if(c.getCount() > 0){
            c.moveToFirst();
            tword = new DictWord(c.getInt(c.getColumnIndex(TrnrEntry._ID)));
            tword.setForeignWord((c.getString(c.getColumnIndex(TrnrEntry.COLUMN_NAME_IN_WORD))).toUpperCase());
            tword.setArt(c.getString(c.getColumnIndex(TrnrEntry.COLUMN_NAME_ARTIKEL)).charAt(0));
            tword.setLevel(c.getInt(c.getColumnIndex(TrnrEntry.COLUMN_NAME_LEVEL)));
            tword.setTranslation((c.getString(c.getColumnIndex(TrnrEntry.COLUMN_NAME_OUT_WORD))).toUpperCase());
            tword.setState(c.getInt(c.getColumnIndex(TrnrEntry.COLUMN_NAME_STATE)));
        } else { tword = new DictWord();}
        c.close();*/
        
        /*for(Entry<String, DictWord> entry : tm.entrySet()) {
            tword = entry.getValue();
          }*/
        if(dictItr.hasNext()){
            tword = dictItr.next(); 
            }
        return tword;
    }
    
    public void onClickArt(View v){
        for (int i = 0; i < lineArt.getChildCount(); i++ ){
            Button btn = (Button) lineArt.getChildAt(i);
            btn.setEnabled(false);
            if (getArtFromBtnText(btn.getText().toString())
                   .equals("" + currentWord.getArt())){
                btn.setBackgroundResource(R.drawable.ic_green);
              }
        }
        boolean isNotNoun =((Character) currentWord.getArt()).equals((Character) TrnrEntry.TYPE_ADJECTIVE) 
                || ((Character) currentWord.getArt()).equals((Character) TrnrEntry.TYPE_OTHER) 
                || ((Character) currentWord.getArt()).equals((Character) TrnrEntry.TYPE_VERB); 
        if (isNotNoun) btnNotNoun.setBackgroundResource(R.drawable.ic_green);

        Button btn = (Button)v;
        if (getArtFromBtnText(btn.getText().toString())
                .equals("" + currentWord.getArt())
                || (isNotNoun && btn.equals(btnNotNoun))){
          currentWord.setState(1);
        } else {
            mistakeReact();
            btn.setBackgroundResource(R.drawable.ic_orange);
        }
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
    }
    
    protected void onPause(){
        super.onPause();
        db.close();
    }

    public void onClickShowResult(View v){
        String art = "";
        switch(currentWord.getArt()){
        case TrnrEntry.TYPE_MASCULINE: art = getResources().getString(R.string.mArt); break;
        case TrnrEntry.TYPE_FEMININE: art = getResources().getString(R.string.fArt); break;
        case TrnrEntry.TYPE_NEUTRAL: art = getResources().getString(R.string.nArt); break;
        }
        tv2.setText((!art.equals("") ? (art + " "): "")
                + currentWord.getForeignWord());
        currentWord.setState(0);
        //disable all buttons
        for(LinearLayout line : new LinearLayout[]{line1,line2,lineArt})
            for(int i=0; i < line.getChildCount(); i++){
                line.getChildAt(i).setEnabled(false);
                line.getChildAt(i).setBackgroundResource(R.drawable.btn_art_draw);
            }
    }
    
    public void onRemoveClick(View v){
        if (currentWord.get_id() < 0) return;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.alert_confirm_title));
        alertDialogBuilder
            .setMessage(getResources().getString(R.string.ma_remove_alert_message))
            .setCancelable(false)
            .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    mDbHelper.onRemoveRecord(db, currentWord.get_id());
                    //
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
    
}
