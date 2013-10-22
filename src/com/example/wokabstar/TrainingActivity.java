package com.example.wokabstar;

import com.example.wokabstar.TrnrDbHelper.TrnrEntry;

import android.os.Bundle;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class TrainingActivity extends android.support.v7.app.ActionBarActivity {

    private TrnrDbHelper mDbHelper;
    private SQLiteDatabase db;
    DictWord tar_word;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        
        Button btnNotNoun = (Button)findViewById(R.id.btnNotNoun);
        btnNotNoun.setPaintFlags(btnNotNoun.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        
        for(int i = 0; i < 4; i++){
            Button btn = new Button(this);
            btn.setText("A");
            btn.setBackgroundResource(R.drawable.ic_grey);
            
            btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });

            LinearLayout linearLayoutForTargetWord = (LinearLayout)findViewById(R.id.linearLayout_TransLettersLine1);
            linearLayoutForTargetWord.addView(btn);
        }
        for(int i = 0; i < 4; i++){
            Button btn = new Button(this);
            btn.setText("A");
            btn.setBackgroundResource(R.drawable.ic_grey);
            
            btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });

            LinearLayout linearLayoutForTargetWord = (LinearLayout)findViewById(R.id.linearLayout_TransLettersLine2);
            linearLayoutForTargetWord.addView(btn);
        }

    }

    public void showTargetWord(String target_word){
        
    }

    public void showTargetLetters(String target_word){
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.training, menu);
        return true;
    }
    
    protected void onStart(){
        super.onStart();
        mDbHelper = new TrnrDbHelper(this);
        db = mDbHelper.getWritableDatabase();
        tar_word = getTargetWord();
        setTargetWord(tar_word.getIn_word());
        //setResultWord(tar_word.getIn_word());
     }
    
    public void setTargetWord(String tword){
        TextView tv = (TextView)findViewById(R.id.txtInfo);
    }
     
    public DictWord getTargetWord(){
        DictWord tword;
        String sql = "SELECT TOP 1 * FROM " + TrnrEntry.TABLE_TDICT + " WHERE state=0";
        db.rawQuery(sql, null);
        Cursor c = db.rawQuery(sql, null);
        if(c.getCount() > 0){
            c.moveToFirst();
            tword = new DictWord(c.getInt(c.getColumnIndex(TrnrEntry._ID)));
            tword.setIn_word(c.getString(c.getColumnIndex(TrnrEntry.COLUMN_NAME_IN_WORD)));
            tword.setArt(c.getString(c.getColumnIndex(TrnrEntry.COLUMN_NAME_ARTIKEL)).charAt(0));
            tword.setLevel(c.getInt(c.getColumnIndex(TrnrEntry.COLUMN_NAME_LEVEL)));
            tword.setOut_word(c.getString(c.getColumnIndex(TrnrEntry.COLUMN_NAME_OUT_WORD)));
            tword.setState(c.getInt(c.getColumnIndex(TrnrEntry.COLUMN_NAME_STATE)));
        } else { tword = new DictWord();}
        c.close();
        return tword;
    }
    
     protected void onPause(){
         super.onPause();
         db.close();
     }


}
