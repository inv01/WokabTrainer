package com.example.wokabstar;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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
    private int selectedID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_on_dict);
        // Show the Up button in the action bar.
        setupActionBar();
        init();
        /*
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        // Create the text view
        TextView textView = new TextView(this);
        textView.setTextSize(24);
        textView.setText(message);
        // Set the text view as the activity layout
        setContentView(textView);*/
        
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
        /*
        edtSearchWord.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                    onClickSearch(v);
            }
        });
        edtSearchWord.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                onKeySearch();
                return true;
            }
        });*/
        enableAllFields(false);
        setEditEnabled(false);
        setSaveEnabled(false);
        setRecycleEnabled(false);
    }
    
    public void addNewWordToDict(char art, String new_word,
                                 String new_translation, int state, int level){
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(TrnrEntry.COLUMN_NAME_ARTIKEL, ""+art);
        values.put(TrnrEntry.COLUMN_NAME_IN_WORD, new_word);
        values.put(TrnrEntry.COLUMN_NAME_OUT_WORD, new_translation);
        values.put(TrnrEntry.COLUMN_NAME_STATE, state);
        values.put(TrnrEntry.COLUMN_NAME_LEVEL, level);
        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(
                 TrnrEntry.TABLE_TDICT,
                 null,
                 values);
        if (newRowId == -1){
            System.out.println(getResources().getString(R.string.error_inserting) + art + ", " + new_word 
                    + ", " + new_translation + ", " + state + ", " + level);
        }
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
       db.execSQL("DELETE FROM " + TrnrEntry.TABLE_TDICT);
       
       addNewWordToDict(TrnrEntry.TYPE_ADJECTIVE, "schon","beautiful", 0, 0);
       addNewWordToDict(TrnrEntry.TYPE_FEMININE, "schon123","beautiful123", 1, 1);
       addNewWordToDict(TrnrEntry.TYPE_MASCULINE, "schonMann","beautifulMann", 2, 2);
       addNewWordToDict(TrnrEntry.TYPE_FEMININE, "Frau","woman", 0, 0);
       addNewWordToDict(TrnrEntry.TYPE_MASCULINE, "Mann","man", 0, 0);
       addNewWordToDict(TrnrEntry.TYPE_NEUTRAL, "Madchen","girl", 0, 0);
       addNewWordToDict(TrnrEntry.TYPE_VERB, "gehen","to go", 0, 0);
       addNewWordToDict(TrnrEntry.TYPE_OTHER, "gehen wir","we go", 0, 0);
    }
    
    protected void onPause(){
        super.onPause();
        db.close();
    }

    public void onClickSearch(View v) {
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
            selectedID =  c.getInt(c.getColumnIndex(TrnrEntry._ID));
            String word_type = c.getString(c.getColumnIndex(TrnrEntry.COLUMN_NAME_ARTIKEL));
            int word_level = c.getInt(c.getColumnIndex(TrnrEntry.COLUMN_NAME_LEVEL));
            edtOutWord.setText(c.getString(c.getColumnIndex(TrnrEntry.COLUMN_NAME_OUT_WORD)));

            switch(word_type.charAt(0)){
                case TrnrEntry.TYPE_MASCULINE: sp_wordType.setSelection(0); break;
                case TrnrEntry.TYPE_FEMININE: sp_wordType.setSelection(1); break;
                case TrnrEntry.TYPE_NEUTRAL: sp_wordType.setSelection(2); break;
                case TrnrEntry.TYPE_VERB: sp_wordType.setSelection(3); break;
                case TrnrEntry.TYPE_ADJECTIVE: sp_wordType.setSelection(4); break;
                case TrnrEntry.TYPE_OTHER: sp_wordType.setSelection(5); break;
                default: assert false : word_type;
            }

            switch(word_level){
                case TrnrEntry.LEVEL_A1: sp_wordLevel.setSelection(0); break;
                case TrnrEntry.LEVEL_A2: sp_wordLevel.setSelection(1); break;
                case TrnrEntry.LEVEL_B1: sp_wordLevel.setSelection(2); break;
                case TrnrEntry.LEVEL_B2: sp_wordLevel.setSelection(3); break;
                case TrnrEntry.LEVEL_C1: sp_wordLevel.setSelection(4); break;
                case TrnrEntry.LEVEL_C2: sp_wordLevel.setSelection(5); break;
                default: assert false : word_level;
            }
            txtInfo.setText(getResources().getString(R.string.work_on_dict_search_inf));
            setEditEnabled(true);
            setRecycleEnabled(true);
            setSaveEnabled(false);
        } else {
          selectedID = -1;
          txtInfo.setText(getResources().getString(R.string.work_on_dict_not_found));
          enableAllFields(true);
          setSaveEnabled(true);
          setEditEnabled(false);
          setRecycleEnabled(false);
        }
        c.close();
    }
    public void onEditClick(View v){
        enableAllFields(true);
        setSaveEnabled(true);
        setRecycleEnabled(true);
    }
    
    public void onSaveClick(View v){
        //check data
        if (edtSearchWord.getText().toString().equals("") || edtOutWord.getText().toString().equals("")){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(getResources().getString(R.string.alert_info_title));
            alertDialogBuilder
                .setMessage(getResources().getString(R.string.alert_empty_info))
                .setCancelable(true)
                .setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                  });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
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
        
        addNewWordToDict(word_type, edtSearchWord.getText().toString(), edtOutWord.getText().toString(), 0, word_level);
        enableAllFields(false);
        setSaveEnabled(false);
        setRecycleEnabled(true);
        setEditEnabled(true);
    }
    
    public void onRemoveClick(View v){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.alert_confirm_title));
        alertDialogBuilder
            .setMessage(getResources().getString(R.string.ma_remove_alert_message))
            .setCancelable(false)
            .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    mDbHelper.onRemoveRecord(db, selectedID);
                    clearFields();
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
        sp_wordType.setSelection(0);
        sp_wordLevel.setSelection(0);
        edtOutWord.setText("");
        edtSearchWord.setText("");
        enableAllFields(false);
        setRecycleEnabled(false);
        setSaveEnabled(false);
        setEditEnabled(false);
    }
    
    public void enableAllFields(boolean enabled){
        sp_wordType.setEnabled(enabled);
        sp_wordLevel.setEnabled(enabled);
        edtOutWord.setEnabled(enabled);
    }
    
    public void setRecycleEnabled(boolean enabled){
        btnRemove.setEnabled(enabled);
        if (enabled){
            btnRemove.setImageDrawable(getResources().getDrawable(R.drawable.ic_recycle));
        } else {
              btnRemove.setImageDrawable(getResources().getDrawable(R.drawable.ic_recycle_disable));
        }
    }

    public void setSaveEnabled(boolean enabled){
        btnSave.setEnabled(enabled);
        if (enabled){
            btnSave.setImageDrawable(getResources().getDrawable(R.drawable.ic_floppy));
        } else {
              btnSave.setImageDrawable(getResources().getDrawable(R.drawable.ic_floppy_disable));
        }
    }

    public void setEditEnabled(boolean enabled){
        btnEdit.setEnabled(enabled);
        if (enabled){
            btnEdit.setImageDrawable(getResources().getDrawable(R.drawable.ic_pencil));
        } else {
            btnEdit.setImageDrawable(getResources().getDrawable(R.drawable.ic_pencil_disable));
        }
    }

    public void onKeySearch() {
        String[] in_words = mDbHelper.getWordsMatchingQuery(db, edtSearchWord.getText().toString());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, in_words);
        edtSearchWord.setAdapter(adapter);
    }
}
