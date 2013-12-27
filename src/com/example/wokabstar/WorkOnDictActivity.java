package com.example.wokabstar;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.wokabstar.TrnrDbHelper.TrnrEntry;

public class WorkOnDictActivity extends android.support.v7.app.ActionBarActivity {
    
    private ImageButton btnEdit, btnSearch, btnRemove, btnSave;
    private TextView txtInfo;
    private Spinner sp_wordType, sp_wordLevel;
    private AutoCompleteTextView edtSearchWord, edtOutWord;
    private TrnrDbHelper mDbHelper;
    private SQLiteDatabase db;
    private DictWord selectedWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_on_dict);
        // Show the Up button in the action bar.
        setupActionBar();
        init();
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // If your minSdkVersion is 11 or higher, instead use:
        // getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void init(){
        btnEdit = (ImageButton)findViewById(R.id.btnEdit);
        btnSearch = (ImageButton)findViewById(R.id.btnSearch);
        btnSave = (ImageButton)findViewById(R.id.btnSave);
        txtInfo = (TextView)findViewById(R.id.txtInfo);
        sp_wordType = (Spinner)findViewById(R.id.spinner1);
        sp_wordLevel = (Spinner)findViewById(R.id.spinner2);
        edtSearchWord = (AutoCompleteTextView)findViewById(R.id.edtSearchWord);
        edtOutWord = (AutoCompleteTextView)findViewById(R.id.edtOutWord);
        btnRemove = (ImageButton)findViewById(R.id.btnRemove);
        selectedWord = new DictWord();
        
        Typeface font = Typeface.createFromAsset(getAssets(), "Chantelli_Antiqua.ttf");
        txtInfo.setTypeface(font);
        ((TextView)findViewById(R.id.txtLevelInfo)).setTypeface(font);
        ((TextView)findViewById(R.id.txtTypeInfo)).setTypeface(font);
        ((TextView)findViewById(R.id.txtHint)).setTypeface(font);
        
        edtSearchWord.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                onSearchClick(arg1);
            }
        });
        
        enableAllFields(false);
        btnEdit.setEnabled(false);
        btnSave.setEnabled(false);
        btnRemove.setEnabled(false);
    }

    public DictWord getNewAddedWord(char art, String in_word,
                                 String out_word, int state, int level){
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(TrnrEntry.COLUMN_NAME_ARTIKEL, ""+art);
        values.put(TrnrEntry.COLUMN_NAME_IN_WORD, in_word);
        values.put(TrnrEntry.COLUMN_NAME_OUT_WORD, out_word);
        values.put(TrnrEntry.COLUMN_NAME_STATE, state);
        values.put(TrnrEntry.COLUMN_NAME_LEVEL, level);
        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(
                 TrnrEntry.TABLE_TDICT,
                 null,
                 values);
        if (newRowId == -1){
            System.out.println(getResources().getString(R.string.error_inserting) + art + ", " + in_word 
                    + ", " + out_word + ", " + state + ", " + level);
        }
        DictWord newWord = new DictWord((int) newRowId);
        newWord.setArt(art);
        newWord.setForeignWord(in_word);
        newWord.setTranslation(out_word);
        newWord.setLevel(level);
        newWord.setState(state);
        return newWord;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.work_on_dict, menu);
        return true;
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


    protected void onStart(){
       super.onStart();
       mDbHelper = new TrnrDbHelper(this);
       db = mDbHelper.getWritableDatabase();
       setAdapter();
    }

    public void onSearchClick(View v) {
        enableAllFields(false);
        //check data
        String in_word = this.edtSearchWord.getText().toString();
        if (in_word.equals("")){
            clearFields();
            return;
        }
        //search perform
        String select = "SELECT "+ TrnrEntry._ID + ", "
                + TrnrEntry.COLUMN_NAME_ARTIKEL + ", " + TrnrEntry.COLUMN_NAME_IN_WORD + ", "
                + TrnrEntry.COLUMN_NAME_OUT_WORD + ", " + TrnrEntry.COLUMN_NAME_LEVEL 
                + " FROM " + TrnrEntry.TABLE_TDICT
                + " WHERE " + TrnrEntry.COLUMN_NAME_IN_WORD + " = '" + in_word + "'";
        Cursor c = db.rawQuery(select, null);
        
        if (c.moveToFirst()){
            selectedWord = new DictWord(c.getInt(c.getColumnIndex(TrnrEntry._ID)));
            selectedWord.setForeignWord(c.getString(c.getColumnIndex(TrnrEntry.COLUMN_NAME_IN_WORD)));
            selectedWord.setArt(c.getString(c.getColumnIndex(TrnrEntry.COLUMN_NAME_ARTIKEL)).charAt(0));
            selectedWord.setLevel(c.getInt(c.getColumnIndex(TrnrEntry.COLUMN_NAME_LEVEL)));
            selectedWord.setTranslation(c.getString(c.getColumnIndex(TrnrEntry.COLUMN_NAME_OUT_WORD)));
            edtOutWord.setText(selectedWord.getTranslation());

            switch(selectedWord.getArt()){
                case TrnrEntry.TYPE_MASCULINE: sp_wordType.setSelection(0); break;
                case TrnrEntry.TYPE_FEMININE: sp_wordType.setSelection(1); break;
                case TrnrEntry.TYPE_NEUTRAL: sp_wordType.setSelection(2); break;
                case TrnrEntry.TYPE_VERB: sp_wordType.setSelection(3); break;
                case TrnrEntry.TYPE_ADJECTIVE: sp_wordType.setSelection(4); break;
                case TrnrEntry.TYPE_OTHER: sp_wordType.setSelection(5); break;
                default: assert false : selectedWord.getArt();
            }

            switch(selectedWord.getLevel()){
                case TrnrEntry.LEVEL_A1: sp_wordLevel.setSelection(0); break;
                case TrnrEntry.LEVEL_A2: sp_wordLevel.setSelection(1); break;
                case TrnrEntry.LEVEL_B1: sp_wordLevel.setSelection(2); break;
                case TrnrEntry.LEVEL_B2: sp_wordLevel.setSelection(3); break;
                case TrnrEntry.LEVEL_C1: sp_wordLevel.setSelection(4); break;
                case TrnrEntry.LEVEL_C2: sp_wordLevel.setSelection(5); break;
                default: assert false : selectedWord.getLevel();
            }
            txtInfo.setText(getResources().getString(R.string.work_on_dict_search_inf));
            btnEdit.setEnabled(true);
            btnRemove.setEnabled(true);
            btnSave.setEnabled(false);
        } else {
          selectedWord = new DictWord();
          txtInfo.setText(getResources().getString(R.string.work_on_dict_not_found));
          enableAllFields(true);
          String s = edtSearchWord.getText().toString();
          clearFields();
          edtSearchWord.setText(s);
          btnSave.setEnabled(true);
          btnEdit.setEnabled(false);
          btnRemove.setEnabled(false);
        }
        c.close();
    }
    public void onEditClick(View v){
        if (selectedWord.get_id() < 0) {clearFields();}
        
        String typedWord = edtSearchWord.getText().toString();
        if(!typedWord.equals(selectedWord.getForeignWord())){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(getResources().getString(R.string.alert_confirm_title));
            alertDialogBuilder
                .setMessage(getResources().getString(R.string.work_on_dict_alert_change_word)
                        + " " + selectedWord.getForeignWord() + "->" + typedWord + "?")
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        enableAllFields(true);
                        btnSave.setEnabled(true);
                        btnRemove.setEnabled(true);
                    }
                  })
                  .setNeutralButton("Search "+typedWord, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            onSearchClick(edtSearchWord);
                        }
                   })
                  .setNegativeButton("No",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else {
        
        enableAllFields(true);
        btnSave.setEnabled(true);
        btnRemove.setEnabled(true);
        }
    }
    
    public void onSaveClick(View v){
        //check data
        if (edtSearchWord.getText().toString().equals("") || edtOutWord.getText().toString().equals("")){
            StarUtility.showInfo(getApplicationContext(), getResources().getString(R.string.alert_empty_info));
            return;
        }
        char word_type = 'x';
        switch((int) sp_wordType.getSelectedItemId()){
        case 0: word_type = TrnrEntry.TYPE_MASCULINE; break;
        case 1: word_type = TrnrEntry.TYPE_FEMININE; break;
        case 2: word_type = TrnrEntry.TYPE_NEUTRAL; break;
        case 3: word_type = TrnrEntry.TYPE_VERB; break;
        case 4: word_type = TrnrEntry.TYPE_ADJECTIVE; break;
        case 5: word_type = TrnrEntry.TYPE_OTHER; break;
        default: assert false : sp_wordType.getSelectedItemId();
    }
        int word_level = (int) sp_wordLevel.getSelectedItemId();
        String currentInWord = edtSearchWord.getText().toString();
        if (selectedWord.get_id() < 0){
            selectedWord = getNewAddedWord(word_type, currentInWord, edtOutWord.getText().toString(), 0, word_level);
        } else {
            updateRecord(selectedWord.get_id(), word_type, currentInWord, edtOutWord.getText().toString(), 0, word_level);
        }
        setAdapter();
        txtInfo.setText(getResources().getString(R.string.work_on_dict_search_inf));
        enableAllFields(false);
        btnSave.setEnabled(false);
        btnRemove.setEnabled(true);
        btnEdit.setEnabled(true);
    }
    
    public void updateRecord(int id, char word_type, String searchWord, String outWord, int state, int word_level){
        ContentValues values = new ContentValues();
        values.put(TrnrEntry.COLUMN_NAME_ARTIKEL, "" + word_type);
        values.put(TrnrEntry.COLUMN_NAME_IN_WORD, searchWord);
        values.put(TrnrEntry.COLUMN_NAME_OUT_WORD, "" + outWord);
        values.put(TrnrEntry.COLUMN_NAME_STATE, state);
        values.put(TrnrEntry.COLUMN_NAME_LEVEL, word_level);

        String selection = TrnrEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        db.update(TrnrEntry.TABLE_TDICT, values, selection, selectionArgs);
    }
    
    public void onRemoveClick(View v){
        if (selectedWord.get_id() < 0) return;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.alert_confirm_title));
        alertDialogBuilder
            .setMessage(getResources().getString(R.string.ma_remove_alert_message))
            .setCancelable(false)
            .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    mDbHelper.onRemoveRecord(db, selectedWord.get_id());
                    clearFields();
                    enableAllFields(false);
                    btnRemove.setEnabled(false);
                    btnSave.setEnabled(false);
                    btnEdit.setEnabled(false);
                    setAdapter();
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
    
    public void clearFields(){
        txtInfo.setText(getResources().getString(R.string.work_on_dict_search_inf));
        edtOutWord.setText("");
        edtSearchWord.setText("");
    }
    
    public void enableAllFields(boolean enabled){
        sp_wordType.setEnabled(enabled);
        sp_wordLevel.setEnabled(enabled);
        edtOutWord.setEnabled(enabled);
    }

    public void setAdapter() {
        String[] in_words = mDbHelper.getWordsMatchingQuery(db, "");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, in_words);
        edtSearchWord.setAdapter(adapter);
    }

    protected void onPause(){
        super.onPause();
        db.close();
    }
}
