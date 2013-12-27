package com.example.wokabstar;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.example.wokabstar.TrnrDbHelper.TrnrEntry;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class StarUtility {

    public static String getOrderedSymbols(String string){
        Set<Character> resultSet = new TreeSet<Character>();
        Set<Character> adtSet = new TreeSet<Character>();
        for (int i = 0; i < string.length(); i++) {
            Character c = Character.valueOf(string.charAt(i));
            if (c != ' '){
            if (resultSet.size() == 8) {
                if (!resultSet.contains(c)) 
                    adtSet.add(c);
                }
            else resultSet.add(c);
            }
        }
        String s = "";
        Iterator<Character> i = resultSet.iterator();
        while (i.hasNext()) s += i.next();
                            i = adtSet.iterator();
        while (i.hasNext()) s += i.next();
        return s;
    }
    
    public static Typeface setFonts(DictWord curWord, Typeface ttf_checked1, 
            Typeface ttf_checked2, Typeface ttf_base, Object args[]){
        for (int i = 0; i < args.length; i++){
            setObjTypeFont(args[i], ttf_checked1);
        }
        String symbols = getOrderedSymbols(curWord.getForeignWord().toUpperCase() 
                + curWord.getTranslation().toUpperCase());
        for (int i = 0; i < symbols.length(); i++){
            int chari = (int) (symbols.charAt(i));
            if(!isCoveredByFontSymbol(chari, "Chantelli_Antiqua") || 
                    !isCoveredByFontSymbol(chari, "alpha_echo" )){
                for (int j = 0; j < args.length; j++){
                    setObjTypeFont(args[j], ttf_base);
                }
                return ttf_base;
            }
        }
        return ttf_checked2;
    }
    
    public static void setFont(String symbols, Typeface ttf_checked, String ttf_nameChecked, Typeface ttf_base, Object args[]){
        for (int i = 0; i < args.length; i++){
            setObjTypeFont(args[i], ttf_checked);
        }
        for (int i = 0; i < symbols.length(); i++){
            if(!isCoveredByFontSymbol((int) (symbols.charAt(i)), ttf_nameChecked)){
                for (int j = 0; j < args.length; j++){
                    setObjTypeFont(args[j], ttf_base);
                }
                return;
            }
        }
    }
    
    private static void setObjTypeFont(Object comp, Typeface ttf){
        if (comp instanceof android.view.ViewGroup){
            for(int i = 0; i< ((android.view.ViewGroup) comp).getChildCount(); i++){
                setObjTypeFont(((android.view.ViewGroup) comp).getChildAt(i), ttf);
            }
        } else if (comp instanceof android.widget.TextView){
            ((android.widget.TextView) comp).setTypeface(ttf);
        }
    }
    
    private static boolean isCoveredByFontSymbol(int chari, String font_name){
        if (font_name.equals("Chantelli_Antiqua"))
        return (!Arrays.asList(new int[]{240,248}).contains(chari) &&
                        ((chari > 32 && chari < 127) ||
                        (chari > 191 && chari < 215) ||
                        (chari > 222 && chari < 253) ||
                        (chari > 216 && chari < 221) ||
                        Arrays.asList(new int[]{137,140,153,156,161,167,169,174,182}).contains(chari))
                    );
        if (font_name.equals("alpha_echo")) 
            return (!Arrays.asList(new int[]{37,35,43,197,198,208,215,216,221,222,229,230,240,247,248
            }).contains(chari) &&
            ((chari > 32 && chari < 127) ||
                    (chari > 32 && chari < 60) ||
                    (chari > 64 && chari < 91) ||
                    (chari > 95 && chari < 123) ||
                    (chari > 190 && chari < 253) ||
                    Arrays.asList(new int[]{63,96,128,161}).contains(chari))
      );
        return false;
    }

    public static void showInfo(Context context, String info){
        CharSequence text = info;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
    
    public static TreeMap<String, DictWord> getWordsToRepeat(SharedPreferences prefs, 
            TrnrDbHelper mDbHelper, Context context, String msg) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        
        String levelComparison = (prefs.getBoolean("strikt_level", false)) ? "=" : "<=";
        String word_level = "" + prefs.getInt("word_level", 0);
        
        //save and check last id of repeated word and get exactly N words from that point
        int lastId = prefs.getInt("last_id", 0);
        boolean mode_learn = prefs.getBoolean("mode_learn", true);
        String comp_operator = (mode_learn) ? "<" : "=";
        String id_condition = (!mode_learn) ? (" and " + TrnrEntry._ID + " >= " + lastId) : "";
        
        int word_number = prefs.getInt("word_number", 0);
        word_number = (word_number > 1) ? word_number * 10 : ((word_number == 0) ? 10 : 15);
        
        String sql = "SELECT * FROM " + TrnrEntry.TABLE_TDICT + 
                " WHERE " + TrnrEntry.COLUMN_NAME_STATE + " " + comp_operator +" 4 and "+ TrnrEntry.COLUMN_NAME_LEVEL + 
                " " + levelComparison + word_level +
                id_condition + " order by " + TrnrEntry.COLUMN_NAME_STATE + " desc LIMIT " + word_number;

        TreeMap<String, DictWord> tm = new TreeMap<String, DictWord>();
        tm = getFilledTreeMap(tm, db, sql);
        
        if (!mode_learn && tm.size() < word_number && lastId != 0){
            int req_col = word_number - tm.size();
            String a_sql = "SELECT * FROM " + TrnrEntry.TABLE_TDICT + 
                    " WHERE " + TrnrEntry._ID + " >= 0 and " + TrnrEntry.COLUMN_NAME_STATE + " = 4 and " + TrnrEntry.COLUMN_NAME_LEVEL +
                    " " + levelComparison + word_level +
                    " order by " + TrnrEntry._ID + " desc LIMIT " + req_col;
            tm = getFilledTreeMap(tm, db, a_sql);
        }
        
        if(tm.isEmpty()) showInfo(context, msg);
        db.close();
        return tm;
    }
    public static TreeMap<String, DictWord> getFilledTreeMap(TreeMap<String, DictWord> tm, SQLiteDatabase db, String sql){
        Cursor c = db.rawQuery(sql, null);
        if(c.moveToFirst()){
            do{
                DictWord dw = new DictWord(c.getInt(c.getColumnIndex(TrnrEntry._ID)));
                dw.setForeignWord((c.getString(c.getColumnIndex(TrnrEntry.COLUMN_NAME_IN_WORD))).toUpperCase());
                dw.setArt(c.getString(c.getColumnIndex(TrnrEntry.COLUMN_NAME_ARTIKEL)).charAt(0));
                dw.setLevel(c.getInt(c.getColumnIndex(TrnrEntry.COLUMN_NAME_LEVEL)));
                dw.setTranslation((c.getString(c.getColumnIndex(TrnrEntry.COLUMN_NAME_OUT_WORD))).toUpperCase());
                dw.setState(c.getInt(c.getColumnIndex(TrnrEntry.COLUMN_NAME_STATE)));
                String id = c.getString(c.getColumnIndex(TrnrEntry._ID));
                if (!tm.containsKey(id)) tm.put(id, dw);
            }while(c.moveToNext());
        }
        c.close();
        return tm;
    }
    
    public static TreeMap<String, DictWord> getOptionsForDictWord(DictWord curWord, TrnrDbHelper mDbHelper, 
            Context context, String msg) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String sql = "SELECT * FROM " + TrnrEntry.TABLE_TDICT + " WHERE " + 
                        TrnrEntry.COLUMN_NAME_OUT_WORD + " <> '" + curWord.getTranslation() + "' and "+
                        TrnrEntry._ID + " > " + curWord.get_id() +" LIMIT 3";
        TreeMap<String, DictWord> tm = new TreeMap<String, DictWord>();
        tm = getFilledTreeMap(tm, db, sql);
        if(tm.size() < 3) showInfo(context, msg);
        db.close();
        return tm;
    }
    
    
    public static DictWord getNextDictWord(Iterator<DictWord> itr){
        DictWord tword = new DictWord();
        if(itr.hasNext()){
            tword = itr.next(); 
            }
        return tword;
    }
    
    public static void updateWordState(TrnrDbHelper mDbHelper, TreeMap<String, DictWord> tm){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Collection<DictWord> colection = tm.values();
        Iterator <DictWord> itr = colection.iterator(); 
        while(itr.hasNext()){
            DictWord tword = itr.next();
            if (!(tword.getState() == 0)) continue;

            ContentValues values = new ContentValues();
            values.put(TrnrEntry.COLUMN_NAME_STATE, tword.getState());
            String selection = TrnrEntry._ID + " = ?";
            String[] selectionArgs = { String.valueOf(tword.get_id()) };
            db.update(TrnrEntry.TABLE_TDICT, values, selection, selectionArgs);
        }
        db.close();
    }
}
