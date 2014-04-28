package com.example.wokabstar;

import java.util.HashSet;
import java.util.LinkedList;

import com.example.wokabstar.TrnrDbHelper.DbEn;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

    public class DictListActivity  extends ListActivity {
      private TrnrDbHelper mDbHelper;
      private SQLiteDatabase db;
      private Cursor c;
      private HashSet<String> selectedIds;
      private SparseIntArray preSelectedPositions;
      private int itemPos;
      private DictWord dw;
      private ColoredArrayAdapter adapter;
      private String selected_lng;
      
      private static class ColoredArrayAdapter extends ArrayAdapter<String> {
          private final Context context;
          private final LinkedList<String> values;
          private SparseIntArray colored_positions;
          private long[] checkedIds;

          public ColoredArrayAdapter(Context context, LinkedList<String> values, SparseIntArray positions) {
            super(context, android.R.layout.simple_list_item_multiple_choice, values);
            this.context = context;
            this.values = values;
            this.colored_positions = positions;
          }

          public void setCheckedIds(long[] ids) {
              this.checkedIds = ids;
              notifyDataSetChanged();
            }
          
          @Override
          public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(android.R.layout.simple_list_item_multiple_choice, parent, false);
            CheckedTextView textView = (CheckedTextView) rowView.findViewById(android.R.id.text1);
            textView.setText(values.get(position));
            if (colored_positions.indexOfKey(position) >= 0) {
                textView.setBackgroundColor(colored_positions.get(position));
            }
            if (checkedIds != null) for(long i:checkedIds){
                if(position == (int) i) textView.setChecked(true);
            }
            return rowView;
          }
        } 
      
      
      public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getListView().setCacheColorHint(0);
        getListView().setItemsCanFocus(false);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
      }
      
      public LinkedList<String> getAdapterValues(){
          String sql = "Select * from " + DbEn.TABLE_TDICT +
                  " where " + DbEn.CN_LNG + "=" + selected_lng 
                  + " order by " + DbEn.CN_IN_WORD;
          c = db.rawQuery(sql, null);
          LinkedList<String> list = new LinkedList<String>();
          int i = 0;
          if(c.moveToFirst()){
              do{
                  String art = StarUtility.getUIArt(c.getString(c.getColumnIndex(
                          DbEn.CN_ARTIKEL)).charAt(0),this); 
                  if(art.length() > 0) art+=" ";
                  list.add(art + c.getString(c.getColumnIndex(DbEn.CN_IN_WORD)));
                  //if this word already in the long memory
                  if(c.getInt(c.getColumnIndex(DbEn.CN_STATE)) > 4) {
                      preSelectedPositions.put(i, getResources().getColor(R.color.green_color));
                  }
                  i++;
              }while(c.moveToNext());
          } else StarUtility.showInfo(getApplicationContext(), 
                  getResources().getString(R.string.ta_nowords_alert_message));
          return list;
      }
      protected void onStart(){
          super.onStart();
          SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this); 
          selected_lng = "" + prefs.getInt("trnr_language", 13);
         
          selectedIds = new HashSet<String>();
          preSelectedPositions = new SparseIntArray();
          mDbHelper = new TrnrDbHelper(this);
          db = mDbHelper.getWritableDatabase();
          LinkedList<String> values = getAdapterValues();
          
          adapter = new ColoredArrayAdapter(this, values, preSelectedPositions);
          setListAdapter(adapter);
      }
      
      public void setItemsColor(int color){
          if (itemPos >= 0) {
             SparseBooleanArray sa = getListView().getCheckedItemPositions();
              for(int i = 0; i < sa.size(); i++){
                  int n = sa.keyAt(i);
                  if(sa.get(n)) preSelectedPositions.put(n, color);
              }
              adapter.setCheckedIds(getListView().getCheckedItemIds());
          }
      }
      
      protected void onPause(){
          super.onPause();
          c.close();
          db.close();
      }
      @Override
      protected void onListItemClick(ListView l, View v, int position, long id) {
        c.moveToPosition(position);
        String item = c.getString(c.getColumnIndex(DbEn._ID));
        if(getListView().isItemChecked(position)){
            selectedIds.add(item);
            itemPos = position;
        } else {
            selectedIds.remove(item);
        }
      }

      @Override
      public boolean onCreateOptionsMenu(Menu menu) {
          getMenuInflater().inflate(R.menu.edit_list, menu);
          return true;
      }

      @Override
      public boolean onOptionsItemSelected(MenuItem item) {
          if(selectedIds.isEmpty()){
              StarUtility.showInfo(this, getResources().getString(R.string.work_on_dict_noitems));
              return true;
          }
          String ids = selectedIds.toString().substring(1, 
                  selectedIds.toString().length() - 1);
          switch (item.getItemId()) {
          case R.id.action_edit:
              Intent intent = new Intent(this, WorkOnDictActivity.class);
              if (selectedIds.size() == 1){
                  c.moveToPosition(itemPos);
                  dw = new DictWord(c.getInt(c.getColumnIndex(DbEn._ID)));
                  dw.setForeignWord((c.getString(c.getColumnIndex(DbEn.CN_IN_WORD))));
                  dw.setArt(c.getString(c.getColumnIndex(DbEn.CN_ARTIKEL)).charAt(0));
                  dw.setLevel(c.getInt(c.getColumnIndex(DbEn.CN_LEVEL)));
                  dw.setTranslation((c.getString(c.getColumnIndex(DbEn.CN_OUT_WORD))));
                  dw.setState(c.getInt(c.getColumnIndex(DbEn.CN_STATE)));
                  intent.putExtra("oneWord", dw);
                  startActivity(intent);
              } else {
                  StarUtility.showInfo(this, getResources().getString(R.string.work_on_dict_oneword_only));
                  //intent.putExtra("manyWords", ids);
              }
              return true;
          case R.id.action_exclude:
              db.execSQL("UPDATE " + DbEn.TABLE_TDICT + 
                      " SET " + DbEn.CN_STATE + " = " + 
                      DbEn.CN_STATE + " + 10 WHERE " + 
                      DbEn._ID + " IN (" + ids + ") AND " +
                      DbEn.CN_STATE + " < 5");
              setItemsColor(getResources().getColor(R.color.green_color));
              StarUtility.showInfo(this, getResources().getString(R.string.work_on_dict_done));
              return true;
          case R.id.action_delete:
              db.execSQL("DELETE FROM " + DbEn.TABLE_TDICT + 
                      " WHERE " + DbEn._ID + " IN (" + ids + ")");
              SparseBooleanArray sa = getListView().getCheckedItemPositions();
              LinkedList<String> items = new LinkedList<String>();
              for(int i = 0; i < sa.size(); i++){
                  int n = sa.keyAt(i);
                  if(sa.get(n)) { 
                      items.add(adapter.getItem(n));
                      getListView().setItemChecked(n, false);
                      preSelectedPositions.delete(n);
                  }
              }
              for(String string:items) adapter.remove(string);
              adapter.setCheckedIds(null);
              selectedIds.clear(); 
              itemPos = -1;
              StarUtility.showInfo(this, getResources().getString(R.string.work_on_dict_deleted));
              return true;
          case R.id.action_move_training:
              db.execSQL("UPDATE " + DbEn.TABLE_TDICT + 
                      " SET " + DbEn.CN_STATE + " = " + 
                      DbEn.CN_STATE + " - 10 WHERE " + 
                      DbEn._ID + " IN (" + ids + ") AND " +
                      DbEn.CN_STATE + " > 4 ");
              setItemsColor(getResources().getColor(R.color.base_color));
              StarUtility.showInfo(this, getResources().getString(R.string.work_on_dict_done));
              return true;
          case R.id.action_uncheck_all:
              for(int i = 0; i < getListView().getCount(); i++) {
                  getListView().setItemChecked(i, false);
              }
              selectedIds.clear();
              itemPos = -1;
              return true;
          default:
              return super.onOptionsItemSelected(item);
          }
      }
    } 