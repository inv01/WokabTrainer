package com.example.wokabstar;

import java.util.*;
import com.example.wokabstar.TrnrDbHelper.TrnrEntry;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup;

public class StarUtility {

    public static String getOrderedSymbols(String string){
        TreeSet<Character> resultSet = new TreeSet<Character>();
        LinkedHashSet<Character> adtSet = new LinkedHashSet<Character>();
        for (char c:string.toCharArray()) {
            if (c != ' '){
            if (resultSet.size() == 8 
                    && !resultSet.contains(c)) {adtSet.add(c);}
              else resultSet.add(c);}
        }
        
        String s = "";
        Iterator<Character> i = resultSet.iterator();
        while (i.hasNext()) s += i.next();
                            i = adtSet.iterator();
        while (i.hasNext()) s += i.next();
        return s;
    }
    
    public static Typeface setFonts(DictWord curWord, Typeface niceFontForStaticEl, 
            Typeface niceFontForDynamEl, Typeface defaultFont, Object components[]){
        for (Object a:components) setComponentFont(a, niceFontForStaticEl);
        
        String symbols = getOrderedSymbols(curWord.getForeignWord().toUpperCase() 
                + curWord.getTranslation().toUpperCase());
        for (int chari:symbols.toCharArray()){
            if(!isSymbolCoveredByFont(chari, "Chantelli_Antiqua") || 
                    !isSymbolCoveredByFont(chari, "alpha_echo" )){
                for (Object a:components) setComponentFont(a, defaultFont);
                return defaultFont;
            }
        }
        return niceFontForDynamEl;
    }
    
    public static void setFont(String str, Typeface font_new, String font_new_name, 
            Typeface font_default, Object components[]){
        for (Object a:components) setComponentFont(a, font_new);
        
        for (int chari:str.toCharArray()){
            if(!isSymbolCoveredByFont(chari, font_new_name)){
                for (Object a:components) setComponentFont(a, font_default);
                return;
            }
        }
    }
    
    private static void setComponentFont(Object comp, Typeface font){
        if (comp instanceof ViewGroup){
            for(int i = 0; i< ((ViewGroup) comp).getChildCount(); i++){
                setComponentFont(((ViewGroup) comp).getChildAt(i), font);
            }
        } else if (comp instanceof TextView){
            ((TextView) comp).setTypeface(font);
        }
    }
    
    private static boolean isSymbolCoveredByFont(int chari, String font_name){
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
        Toast toast = Toast.makeText(context, info, Toast.LENGTH_SHORT);
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
        
        String ordby = (mode_learn) ? TrnrEntry.COLUMN_NAME_STATE + " desc " : TrnrEntry._ID;
        String sql = "SELECT * FROM " + TrnrEntry.TABLE_TDICT + 
                " WHERE " + TrnrEntry.COLUMN_NAME_STATE + " " + comp_operator +" 4 and "+ TrnrEntry.COLUMN_NAME_LEVEL + 
                " " + levelComparison + word_level +
                id_condition + " order by " + ordby + " LIMIT " + word_number;

        TreeMap<String, DictWord> tm = new TreeMap<String, DictWord>();
        tm = getFilledTreeMap(tm, db, sql);
        
        SharedPreferences.Editor editor = prefs.edit();
        if (!mode_learn && tm.size() < word_number && lastId != 0){
            int req_col = word_number - tm.size();
            String a_sql = "SELECT * FROM " + TrnrEntry.TABLE_TDICT + 
                    " WHERE " + TrnrEntry._ID + " >= 0 and " + TrnrEntry.COLUMN_NAME_STATE + " = 4 and " + TrnrEntry.COLUMN_NAME_LEVEL +
                    " " + levelComparison + word_level +
                    " order by " + TrnrEntry._ID + " LIMIT " + req_col;
            tm = getFilledTreeMap(tm, db, a_sql);
            
            editor.putBoolean("strat_again", true);
        } else editor.putBoolean("strat_again", false);
        editor.commit();
        
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
                        TrnrEntry._ID + " <> " + curWord.get_id() +" order by RANDOM() LIMIT 3";
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
    
    public static boolean isNotNoun(char art){
        return (art == TrnrEntry.TYPE_ADJECTIVE) 
        || (art == TrnrEntry.TYPE_OTHER) 
        || (art == TrnrEntry.TYPE_VERB);
    }
    
    public static String getUIArt(char art){
        List<String> uiArts = Arrays.asList(new String[]{"der", "die", "das"});
        List<Character> dbArts = Arrays.asList(new Character[]{TrnrEntry.TYPE_MASCULINE, TrnrEntry.TYPE_FEMININE, TrnrEntry.TYPE_NEUTRAL});
        if (dbArts.contains(art)){
            return uiArts.get(dbArts.indexOf(art));
        }
        return "";
    }
}
