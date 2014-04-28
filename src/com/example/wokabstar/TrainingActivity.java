package com.example.wokabstar;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
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
import android.support.v7.app.ActionBar.LayoutParams;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.wokabstar.TrnrDbHelper.DbEn;
import static com.example.wokabstar.StarUtility.getNextDictWord;
import static com.example.wokabstar.StarUtility.getOrdSymbols;

public class TrainingActivity extends android.support.v7.app.ActionBarActivity {

    private DictWord currentWord;
    private TextView tvTranslate, tvFWord, tvState, btnNotNoun;
    private LinearLayout line1, line2, lineArt;
    private TreeMap<String, DictWord> tm;
    private Iterator<DictWord> dictItr;
    private boolean isArtClicked, isWasMistake, isNotNoun;
    private String adtLongWordSymbs, uiArts;
    private Typeface btnFont, baseFont,acFont;
    private boolean isArtTrainingOn, isEmptyUiArts;
    
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
        tvState.setTypeface(btnFont);
    }

    public boolean isRightSymbolPicked(String string){
        int index = tvFWord.getText().toString().indexOf("*");
        return string.equals("" + currentWord.getForeignWord().charAt(index));
    }

    public boolean isSimilarSymbolsLeft(String string) {
        int index = tvFWord.getText().toString().indexOf("*");
        if (index > -1) return currentWord.getForeignWord()
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

        String allMixSymbols = getOrdSymbols(currentWord.getForeignWord());
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

    protected void onStart(){
        super.onStart();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this); 
        isArtTrainingOn = !prefs.getBoolean("no_art_mode", false);
        if (isArtTrainingOn){
        uiArts = prefs.getString("lng_arts","der,die,das,nom");
        isEmptyUiArts = (uiArts.length() == 0) ? true : false;
        isArtTrainingOn = !isEmptyUiArts;
        // initialize articles line
        if(isArtTrainingOn){
            lineArt = (LinearLayout)findViewById(R.id.linearLayout_TransLettersLine1);
            StringTokenizer st = new StringTokenizer(uiArts,",",false);
            if(st.countTokens() == 4){
                int i = 0;
                while (st.hasMoreTokens()) {
                    String s = st.nextToken();
                    TextView btn = (TextView) lineArt.getChildAt(i);
                    btn.setTypeface(btnFont);
                    btn.setText(s);
                    if (s.equals(" ")) lineArt.removeViewAt(i--);
                    i++;
                }
            }
        }}
        lineArt.setVisibility((isArtTrainingOn) ? 
                  android.view.View.VISIBLE : android.view.View.INVISIBLE);
        // end initialize articles line
        selectWordsToRepeat();
        setWordToRepeat();
     }

    public void setWordToRepeat(){
        currentWord = getNextDictWord(dictItr);
        if (currentWord.getForeignWord().length() == 0) {
            onBackPressed();
        }
        isNotNoun = StarUtility.isNotNoun(currentWord.getArt());
        reflectTargetWord();
        resetArt();/*depends on reflectTargetWord();*/
        int num_req_repeat = 4 - currentWord.getState();
        tvState.setText("" + num_req_repeat + " " + getResources().getString(R.string.left_number));
        isWasMistake = false;
        tvFWord.setShadowLayer(0, 0, 0, 0);
    }

    public void reflectTargetWord(){
        if (currentWord.getForeignWord().length() == 0) return;
        
        btnFont = Typeface.createFromAsset(getAssets(), "Chantelli_Antiqua.ttf");
        btnFont = StarUtility.setFonts(currentWord, acFont, btnFont, 
                baseFont, (new Object[] {tvTranslate, tvFWord}));
        
        tvTranslate.setText(currentWord.getTranslation().toUpperCase(Locale.getDefault()));
        String s = "";
        for (char c:currentWord.getForeignWord().toCharArray()){ 
            s += (c != ' ') ? "*" : " ";
        }
        tvFWord.setText(s);
        showTargetLetters();
    }

    public static String getSql(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context); 
        String levelComparison = (prefs.getBoolean("strikt_level", false)) ? "=" : "<=";
        String word_level = "" + prefs.getInt("word_level", 0);
        String cur_lang = " and " + DbEn.CN_LNG + "=" + prefs.getInt("trnr_language", 13);
        int word_number = prefs.getInt("word_number", 0);
        word_number = (word_number > 1) ? word_number * 10 : 
                                        ((word_number == 0) ? 10 : 15);
        String str_word_number = "" + word_number;
        
        String sql = "SELECT * FROM " + DbEn.TABLE_TDICT + 
                " WHERE state < 4 and level " + levelComparison + 
                word_level + cur_lang + " order by state desc LIMIT " + 
                str_word_number;
        return sql;
    }
    
    public void selectWordsToRepeat() {
        TrnrDbHelper mDbHelper = new TrnrDbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor c = db.rawQuery(getSql(this), null);
        tm = new TreeMap<String, DictWord>();
        if(c.moveToFirst()){
            do{
                DictWord dw = new DictWord(c.getInt(c.getColumnIndex(DbEn._ID)));
                dw.setForeignWord((c.getString(c.getColumnIndex(DbEn.CN_IN_WORD))).toUpperCase(Locale.getDefault()));
                dw.setArt(c.getString(c.getColumnIndex(DbEn.CN_ARTIKEL)).charAt(0));
                dw.setLevel(c.getInt(c.getColumnIndex(DbEn.CN_LEVEL)));
                dw.setTranslation((c.getString(c.getColumnIndex(DbEn.CN_OUT_WORD))).toUpperCase(Locale.getDefault()));
                dw.setState(c.getInt(c.getColumnIndex(DbEn.CN_STATE)));
                tm.put(c.getString(c.getColumnIndex(DbEn._ID)), dw);
            }while(c.moveToNext());
        } else StarUtility.showInfo(getApplicationContext(), 
                getResources().getString(R.string.ta_nowords_alert_message));
        c.close();
        db.close();
        Collection<DictWord> colection = tm.values();
        dictItr = colection.iterator();
    }
    
    public void resetArt(){
        if (!isArtTrainingOn){
            isArtClicked = true;
            return;
        }
        for (int i = 0; i < lineArt.getChildCount(); i++ ){
            TextView btn = (TextView) lineArt.getChildAt(i);
            btn.setEnabled(true);
            btn.setBackgroundResource(R.drawable.ic_light_blue);
        }
        isArtClicked = false;
    }

    public void onClickArt(View v){
        String curArt = StarUtility.getUIArt(currentWord.getArt(),this);
        if (curArt.equals("")) isNotNoun = true;
        for (int i = 0; i < lineArt.getChildCount(); i++ ){
            TextView btn = (TextView) lineArt.getChildAt(i);
            btn.setEnabled(false);
            btn.setBackgroundResource(R.drawable.ic_grey);
            if (btn.getText().toString().equals(curArt)){
                btn.setBackgroundResource(R.drawable.ic_green);
              }
        }
        
        if (isNotNoun) btnNotNoun.setBackgroundResource(R.drawable.ic_green);

        TextView btn = (TextView)v;
        if (btn.getText().toString().equals(curArt)
                || (isNotNoun && btn.equals(btnNotNoun))){
        } else {
            mistakeReact();
            btn.setBackgroundResource(R.drawable.ic_orange);
        }
        isArtClicked = true;
        updateStatus_getNewWord();
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
        String art = StarUtility.getUIArt(currentWord.getArt(),this);
        if(art.length() > 0)art += " ";
        tvFWord.setText(art + currentWord.getForeignWord());
        switch(currentWord.getArt()){
            case DbEn.TYPE_MASCULINE: 
                tvFWord.setShadowLayer(2, 1, 1,
                    getResources().getColor(R.color.blue_shadow));
                break;
            case DbEn.TYPE_FEMININE: 
                tvFWord.setShadowLayer(2, 1, 1,
                    getResources().getColor(R.color.pink_shadow));
                break;
            case DbEn.TYPE_NEUTRAL: 
                tvFWord.setShadowLayer(2, 1, 1,
                    getResources().getColor(R.color.gold_shadow));
                break;
            default:
                tvFWord.setShadowLayer(0, 0, 0, 0);
                break;
        }
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
            values.put(DbEn.CN_STATE, tword.getState());
            String selection = DbEn._ID + " = ?";
            String[] selectionArgs = { String.valueOf(tword.get_id()) };
            db.update(DbEn.TABLE_TDICT, values, selection, selectionArgs);
        }
        db.close();
    }
}
