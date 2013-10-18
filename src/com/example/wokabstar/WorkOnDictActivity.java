package com.example.wokabstar;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.example.wokabstar.TrnrDbHelper.TrnrEntry;

public class WorkOnDictActivity extends android.support.v7.app.ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_on_dict);
        // Show the Up button in the action bar.
        setupActionBar();
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
        
        doSmth();
    }

    public void doSmth(){
        TrnrDbHelper mDbHelper = new TrnrDbHelper(this);
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        //values.put(TrnrEntry._ID, 2);
        values.put(TrnrEntry.COLUMN_NAME_ARTIKEL, "m");
        values.put(TrnrEntry.COLUMN_NAME_IN_WORD, "m1");
        values.put(TrnrEntry.COLUMN_NAME_OUT_WORD, "m2");
        values.put(TrnrEntry.COLUMN_NAME_STATE, 0);
        values.put(TrnrEntry.COLUMN_NAME_LEVEL, 0);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(
                 TrnrEntry.TABLE_NAME,
                 null,
                 values);
        if (newRowId == -1){
            System.out.println("Error during inserting values: ...");
        }
        db.close();
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

}
