package com.example.wokabstar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.util.SparseArray;
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

import com.example.wokabstar.TrnrDbHelper.DbEn;

public class WorkOnDictActivity extends android.support.v7.app.ActionBarActivity {
    
    private ImageButton btnEdit, btnRemove, btnSave;
    private TextView txtInfo;
    private Spinner sp_wordType, sp_wordLevel;
    private AutoCompleteTextView edtSearchWord, edtOutWord;
    private TrnrDbHelper mDbHelper;
    private SQLiteDatabase db;
    private DictWord selectedWord;
    private int selected_lng;
    private SparseArray<String> hm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_on_dict);
        setupActionBar();
        init();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void init(){
        btnEdit = (ImageButton)findViewById(R.id.btnEdit);
        btnSave = (ImageButton)findViewById(R.id.btnSave);
        txtInfo = (TextView)findViewById(R.id.txtInfo);
        sp_wordType = (Spinner)findViewById(R.id.spinner1);
        sp_wordLevel = (Spinner)findViewById(R.id.spinner2);
        edtSearchWord = (AutoCompleteTextView)findViewById(R.id.edtSearchWord);
        edtOutWord = (AutoCompleteTextView)findViewById(R.id.edtOutWord);
        btnRemove = (ImageButton)findViewById(R.id.btnRemove);
        
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this); 
        selected_lng = prefs.getInt("trnr_language", 13);
        adjustArts(prefs);
        if(getIntent().getSerializableExtra("oneWord") != null){
            selectedWord = (DictWord) getIntent().getSerializableExtra("oneWord");
            edtSearchWord.setText(selectedWord.getForeignWord());
            initFieldsWithWord(selectedWord);
        } else {
            selectedWord = new DictWord();
            btnEdit.setEnabled(false);
            btnSave.setEnabled(false);
            btnRemove.setEnabled(false);
        }
    }
    
    public DictWord getNewAddedWord(char art, String in_word,
                                 String out_word, int state, int level, int selected_lng){
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DbEn.CN_ARTIKEL, ""+art);
        values.put(DbEn.CN_IN_WORD, in_word);
        values.put(DbEn.CN_OUT_WORD, out_word);
        values.put(DbEn.CN_STATE, state);
        values.put(DbEn.CN_LEVEL, level);
        values.put(DbEn.CN_LNG, selected_lng);
        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(
                 DbEn.TABLE_TDICT,
                 null,
                 values);
        if (newRowId == -1){
            Log.e("Row not inserted", getResources().getString(R.string.error_inserting) + art + ", " + in_word 
                    + ", " + out_word + ", " + state + ", " + level + ", " + selected_lng);
        }
        DictWord newWord = new DictWord((int) newRowId);
        newWord.setArt(art);
        newWord.setForeignWord(in_word);
        newWord.setTranslation(out_word);
        newWord.setLevel(level);
        newWord.setState(state);
        newWord.setLng(selected_lng);
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
        getMenuInflater().inflate(R.menu.work_on_dict, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        case R.id.action_showlist:
            onShowListClick();
            return true;
        case R.id.action_exportdict:
            String e_state = Environment.getExternalStorageState();
            if (!Environment.MEDIA_MOUNTED.equals(e_state)) {
                StarUtility.showInfo(this, "SD card not available.");
                return true;
            }
            new DictionaryExport(this).execute();
            return true;
        case R.id.action_importdict:
            String i_state = Environment.getExternalStorageState();
            if (!Environment.MEDIA_MOUNTED.equals(i_state)) {
                StarUtility.showInfo(this, "SD card not available.");
                return true;
            }
            new DictionaryImport(this).execute();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    protected void onStart(){
       super.onStart();
       mDbHelper = new TrnrDbHelper(this);
       db = mDbHelper.getWritableDatabase();
       setAdapter();
    }

    public void adjustArts(SharedPreferences prefs){
        hm = new SparseArray<String>(6);
        String lng_arts = prefs.getString("lng_arts", "der,die,das,noun");
        String[] def_types = getResources().getStringArray(R.array.word_type_arrays);
        char[] def_db_types = new char[]{
                DbEn.TYPE_MASCULINE,DbEn.TYPE_FEMININE,
                DbEn.TYPE_NEUTRAL,DbEn.TYPE_VERB,
                DbEn.TYPE_ADJECTIVE,DbEn.TYPE_OTHER
        };
        for(int i = 0; i < def_db_types.length; i++){ 
            hm.put(i, "" + def_db_types[i]);}
        ArrayAdapter<String> adapter;
        if(lng_arts.length() == 0) {
            adapter = new ArrayAdapter<String>(this, 
                    android.R.layout.simple_spinner_item,
                    new String[]{def_types[3],def_types[4],def_types[5]});
            sp_wordType.setAdapter(adapter);
            for(int i = 0; i < 3; i++){hm.put(i, "" + def_db_types[i+3]);}
            return;
        }
        StringTokenizer st = new StringTokenizer(lng_arts,",",false);
        if(st.countTokens() == 4){
            List<String> list = new LinkedList<String>();
            for(String s : def_types) list.add(s);
            for(int i = 0; i < 3; i++){
                String art = st.nextToken();
                if(art.equals(" ")){
                    list.remove(i);
                    for(int j = i; j < def_db_types.length - 1; j++) hm.put(j, "" + def_db_types[j+1]);
                } else {
                    list.set(i, art);
                }
            }
            adapter = new ArrayAdapter<String>(this, 
                    android.R.layout.simple_spinner_item, list);
            sp_wordType.setAdapter(adapter);
        }
    }
    
    public void onSearchClick(View v) {
        enableAllFields(false);
        //check data
        String in_word = this.edtSearchWord.getText().toString().trim();
        if (in_word.equals("")){
            clearFields();
            return;
        }
        //search perform
        String select = "SELECT "+ DbEn._ID + ", "
                + DbEn.CN_ARTIKEL + ", " + DbEn.CN_IN_WORD + ", "
                + DbEn.CN_OUT_WORD + ", " + DbEn.CN_LEVEL 
                + " FROM " + DbEn.TABLE_TDICT
                + " WHERE " + DbEn.CN_IN_WORD + " = '" + in_word + "'" 
                + " AND " + DbEn.CN_LNG + "=" + selected_lng;
        Cursor c = db.rawQuery(select, null);
        if (c.moveToFirst()){
            selectedWord = new DictWord(c.getInt(c.getColumnIndex(DbEn._ID)));
            selectedWord.setForeignWord(c.getString(c.getColumnIndex(DbEn.CN_IN_WORD)));
            selectedWord.setArt(c.getString(c.getColumnIndex(DbEn.CN_ARTIKEL)).charAt(0));
            selectedWord.setLevel(c.getInt(c.getColumnIndex(DbEn.CN_LEVEL)));
            selectedWord.setTranslation(c.getString(c.getColumnIndex(DbEn.CN_OUT_WORD)));
            selectedWord.setLng(this.selected_lng);
        } else {
            selectedWord = new DictWord();
        }
        c.close();
        
        initFieldsWithWord(selectedWord);
    }
    
    public void initFieldsWithWord(DictWord word){
        if (word.getForeignWord().equals("")){
            enableAllFields(true);
            String s = edtSearchWord.getText().toString().trim();
            clearFields();
            edtSearchWord.setText(s);
            btnSave.setEnabled(true);
            btnEdit.setEnabled(false);
            btnRemove.setEnabled(false);
            return;
        }
        edtOutWord.setText(word.getTranslation());
        for(int key = 0; key < hm.size(); key++){
            if(hm.get(key).equals("" + word.getArt())){
                sp_wordType.setSelection(key);
                break;
            }
        }
        sp_wordLevel.setSelection(word.getLevel());
        
        btnEdit.setEnabled(true);
        btnRemove.setEnabled(true);
        btnSave.setEnabled(false);
    }
    
    public void onEditClick(View v){
        if (selectedWord.get_id() < 0) {clearFields();}
        
        String typedWord = edtSearchWord.getText().toString().trim();
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
        String in_word = edtSearchWord.getText().toString().trim();
        String out_word = edtOutWord.getText().toString().trim();
        if (in_word.equals("") || out_word.equals("")){
            StarUtility.showInfo(getApplicationContext(), getResources().getString(R.string.alert_empty_info));
            return;
        }
        char word_type = getdbArt();
        int word_level = (int) sp_wordLevel.getSelectedItemId();
        if (selectedWord.get_id() < 0){
            selectedWord = getNewAddedWord(word_type, in_word, out_word, 0, word_level, selected_lng);
        } else {
            updateRecord(selectedWord.get_id(), word_type, in_word, out_word, 0, word_level);
        }
        setAdapter();
        enableAllFields(false);
        btnSave.setEnabled(false);
        btnRemove.setEnabled(true);
        btnEdit.setEnabled(true);
    }
    
    public char getdbArt(){
        String s = hm.get((int) sp_wordType.getSelectedItemId());
        return s.charAt(0);
    }
    
    
    public void updateRecord(int id, char word_type, String searchWord, String outWord, int state, int word_level){
        ContentValues values = new ContentValues();
        values.put(DbEn.CN_ARTIKEL, "" + word_type);
        values.put(DbEn.CN_IN_WORD, searchWord);
        values.put(DbEn.CN_OUT_WORD, "" + outWord);
        values.put(DbEn.CN_STATE, state);
        values.put(DbEn.CN_LEVEL, word_level);

        String selection = DbEn._ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        db.update(DbEn.TABLE_TDICT, values, selection, selectionArgs);
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
        edtOutWord.setText("");
        edtSearchWord.setText("");
    }
    
    public void enableAllFields(boolean enabled){
        sp_wordType.setEnabled(enabled);
        sp_wordLevel.setEnabled(enabled);
        edtOutWord.setEnabled(enabled);
    }

    public void setAdapter() {
        String[] in_words = mDbHelper.getWordsMatchingQuery(db, "", selected_lng);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, in_words);
        edtSearchWord.setAdapter(adapter);
    }

    protected void onPause(){
        super.onPause();
        db.close();
    }
    
    public void copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }
    
    public void onShowListClick(){
        Intent intent = new Intent(this, DictListActivity.class);
        startActivity(intent);
    }

    private class DictionaryExport extends AsyncTask<Void, Integer, String> {
        @Override
        protected String doInBackground(Void... params) {
            String result = "none";
            String destPath = getFilesDir().getPath();
            destPath = destPath.substring(0, destPath.lastIndexOf("/"))
                    + "/databases";
            File fileInDb = new File(destPath + "/"
                    + TrnrDbHelper.DATABASE_NAME);
            File download = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File fileExtDb = new File(download + "/WokabStar.db");
            try {
                download.mkdirs();
                copy(fileInDb, fileExtDb);
                // new SingleMediaScaner(this, fileExtDb);
                Intent intent = new Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(fileExtDb));
                sendBroadcast(intent);
                result = "db";
            } catch (IOException ex) {
                Log.e(ex.getLocalizedMessage(), getResources().getString(R.string.dict_not_exp));
            }
            Cursor c = db.rawQuery("Select * FROM " + DbEn.TABLE_TDICT, null);
            try {
                File file_printable = new File(
                        Environment
                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                + "/WokabStar.txt");
                FileOutputStream outputStream = new FileOutputStream(
                        file_printable);
                OutputStreamWriter out = new OutputStreamWriter(outputStream);
                BufferedWriter bwriter = new BufferedWriter(out);
                String[] levels = getResources().getStringArray(R.array.word_level_arrays);
                String[] languages = getResources().getStringArray(R.array.languages_arrays);
                String[] lang_arts = getResources().getStringArray(R.array.lang_articles_arrays);
                if (c.moveToFirst()) {
                    do {
                        int i = c.getInt(c.getColumnIndex(DbEn.CN_LNG));
                        String art = StarUtility.getCurUIArt(c.getString(
                                c.getColumnIndex(DbEn.CN_ARTIKEL)).charAt(0), lang_arts[i]);
                        if(art.length()>0)art+=" ";
                        String string = art
                                + c.getString(c.getColumnIndex(DbEn.CN_IN_WORD))
                                + ";"
                                + c.getString(c
                                        .getColumnIndex(DbEn.CN_OUT_WORD))
                                + ";"
                                + levels[c.getInt(c.getColumnIndex(DbEn.CN_LEVEL))]
                                + ";"
                                + c.getInt(c.getColumnIndex(DbEn.CN_STATE))
                                + ";"
                                + languages[i]
                                + "\r\n";
                        bwriter.write(string);
                    } while (c.moveToNext());
                }
                bwriter.close();
                c.close();
                // new SingleMediaScaner(this, file_printable);
                File f = new File(
                        Environment
                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                + "/WokabStar.txt");
                Uri contentUri = Uri.fromFile(f);
                Intent mediaScanIntent = new Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
                sendBroadcast(mediaScanIntent);
                if (result.equals("db")) {
                    result = "db_txt";
                } else {
                    result = "txt";
                }
            } catch (Exception e) {
                Log.e(e.getLocalizedMessage(),
                        getResources().getString(R.string.print_dict_notexp));
            }

            return result;
            // sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
            // Uri.parse("file://"+
            // Environment.getExternalStorageDirectory())));
        }

        private Context mContext;
        private ProgressDialog p;
        
        public DictionaryExport(Context context) {
            mContext = context;
            this.p=new ProgressDialog(context);
        }

        @Override
        protected void onPreExecute() {
            Log.i("DictionaryExport", "onPreExecute()");
            super.onPreExecute();
            p.setMessage(getResources().getString(R.string.work_on_dict_exporting));
            p.setIndeterminate(false);
            p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            p.setCancelable(false);
            p.show();
        }

        @Override
        protected void onPostExecute(String result) {
            p.dismiss();
            if (result.equals("db") || result.equals("db_txt"))
                StarUtility.showInfo(mContext,
                        "WokabStar.db " + getResources().getString(R.string.saved_in_downloads));
            if (result.equals("db_txt") || result.equals("txt"))
                StarUtility.showInfo(mContext,
                        "WokabStar.txt " + getResources().getString(R.string.saved_in_downloads));
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.i("DictionaryExport",
                    "onProgressUpdate(): " + String.valueOf(values[0]));
        }
    }
    
    private class DictionaryImport extends AsyncTask<Void, Integer, String> {
        private ProgressDialog p;
        @Override
        protected String doInBackground(Void... params) {
            String destPath = getFilesDir().getPath();
            destPath = destPath.substring(0, destPath.lastIndexOf("/")) + "/databases";
            File fileTo = new File(destPath + "/WokabStar.db");
            File fileFrom = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS) + "/WokabStar.db");
            if(!fileFrom.isFile()) {return "";}
            try{
                copy(fileFrom, fileTo);
            } catch(IOException ex){
                Log.e(ex.getLocalizedMessage(), getResources().getString(R.string.dict_not_imported));
            }
            db.execSQL("ATTACH '" + destPath + "/WokabStar.db' AS other");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext); 
            boolean isForLearning = prefs.getBoolean("mode_import", false);
            String sql = "insert into " + DbEn.TABLE_TDICT + "("+
                    DbEn.CN_IN_WORD + "," + DbEn.CN_ARTIKEL + ", " + DbEn.CN_LEVEL + ", " + DbEn.CN_OUT_WORD 
                    + "," + DbEn.CN_STATE + ", " + DbEn.CN_LNG
                    +") select " + 
                    DbEn.CN_IN_WORD + "," + DbEn.CN_ARTIKEL + ", " + DbEn.CN_LEVEL + ", " + DbEn.CN_OUT_WORD
                     +", "+ ((isForLearning) ? "0" : DbEn.CN_STATE) + "," + DbEn.CN_LNG + " from other." + DbEn.TABLE_TDICT + " p where NOT EXISTS(SELECT " +
                     DbEn.CN_IN_WORD + "," + DbEn.CN_ARTIKEL + ", " + DbEn.CN_LEVEL + ", " + DbEn.CN_OUT_WORD  + ", " + DbEn.CN_STATE
                    + " FROM " + DbEn.TABLE_TDICT + " WHERE " + DbEn.CN_IN_WORD + "=p." + DbEn.CN_IN_WORD + ")";
            db.execSQL(sql);
            db.execSQL("DETACH DATABASE 'other'");
            fileTo.delete();
            return "done";
        }
        private Context mContext;
        public DictionaryImport (Context context){
             mContext = context;
             this.p=new ProgressDialog(context);
        }
        @Override
        protected void onPreExecute() {
            Log.i("DictionaryImport", "onPreExecute()");
            super.onPreExecute();
            p.setMessage(getResources().getString(R.string.work_on_dict_importing));
            p.setIndeterminate(false);
            p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            p.setCancelable(false);
            p.show();
        }
        @Override
        protected void onPostExecute(String result) {
            p.dismiss();
            if (result.equals("done")){
            StarUtility.showInfo(mContext, getResources().getString(R.string.work_on_dict_done));
            } else {
                StarUtility.showInfo(mContext, getResources().getString(R.string.not_found_file));
                }
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.i("DictionaryImport", "onProgressUpdate(): " + String.valueOf(values[0]));
        }
    }
}
