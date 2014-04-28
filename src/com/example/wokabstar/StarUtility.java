package com.example.wokabstar;

import java.util.*;
import com.example.wokabstar.TrnrDbHelper.DbEn;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.widget.TextView;
import android.widget.Toast;
import android.preference.PreferenceManager;
import android.view.ViewGroup;

public class StarUtility {

    public static String getOrdSymbols(String string){
        string = string.toUpperCase(Locale.getDefault());
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
        
        String symbols = getOrdSymbols(curWord.getForeignWord() 
                + curWord.getTranslation());
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
        String selected_lng = " and " + DbEn.CN_LNG + "=" + prefs.getInt("trnr_language", 13) + " ";
        
        //save and check last id of repeated word and get exactly N words from that point
        int lastId = prefs.getInt("last_id", 0);
        boolean mode_learn = prefs.getBoolean("mode_learn", true);
        String comp_operator = (mode_learn) ? "<" : "=";
        String id_condition = (!mode_learn) ? (" and " + DbEn._ID + " >= " + lastId) : "";
        
        int word_number = prefs.getInt("word_number", 0);
        word_number = (word_number > 1) ? word_number * 10 : ((word_number == 0) ? 10 : 15);
        
        String ordby = (mode_learn) ? DbEn.CN_STATE + " desc " : DbEn._ID;
        String sql = "SELECT * FROM " + DbEn.TABLE_TDICT + 
                " WHERE " + DbEn.CN_STATE + " " + comp_operator +" 4 and "+ DbEn.CN_LEVEL + 
                " " + levelComparison + word_level + selected_lng +
                id_condition + " order by " + ordby + " LIMIT " + word_number;

        TreeMap<String, DictWord> tm = new TreeMap<String, DictWord>();
        tm = getFilledTreeMap(tm, db, sql);
        
        SharedPreferences.Editor editor = prefs.edit();
        if (!mode_learn && tm.size() < word_number && lastId != 0){
            int req_col = word_number - tm.size();
            String a_sql = "SELECT * FROM " + DbEn.TABLE_TDICT + 
                    " WHERE " + DbEn._ID + " >= 0 and " + DbEn.CN_STATE + " = 4 and " + DbEn.CN_LEVEL +
                    " " + levelComparison + word_level + selected_lng +
                    " order by " + DbEn._ID + " LIMIT " + req_col;
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
                DictWord dw = new DictWord(c.getInt(c.getColumnIndex(DbEn._ID)));
                dw.setForeignWord((c.getString(c.getColumnIndex(DbEn.CN_IN_WORD))).toUpperCase(Locale.getDefault()));
                dw.setArt(c.getString(c.getColumnIndex(DbEn.CN_ARTIKEL)).charAt(0));
                dw.setLevel(c.getInt(c.getColumnIndex(DbEn.CN_LEVEL)));
                dw.setTranslation((c.getString(c.getColumnIndex(DbEn.CN_OUT_WORD))).toUpperCase(Locale.getDefault()));
                dw.setState(c.getInt(c.getColumnIndex(DbEn.CN_STATE)));
                dw.setLng(c.getInt(c.getColumnIndex(DbEn.CN_LNG)));
                String id = c.getString(c.getColumnIndex(DbEn._ID));
                if (!tm.containsKey(id)) tm.put(id, dw);
            }while(c.moveToNext());
        }
        c.close();
        return tm;
    }
    
    public static TreeMap<String, DictWord> getOptionsForDictWord(DictWord curWord, TrnrDbHelper mDbHelper, 
            Context context, String msg) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context); 
        String selected_lng = DbEn.CN_LNG + "=" + prefs.getInt("trnr_language", 13) + " and ";
        
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String sql = "SELECT * FROM " + DbEn.TABLE_TDICT + " WHERE " + 
                        selected_lng +
                        DbEn.CN_OUT_WORD + " <> '" + curWord.getTranslation() + "' and "+
                        DbEn._ID + " <> " + curWord.get_id() +" order by RANDOM() LIMIT 3";
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
            if ((tword.getState() > 0) && (tword.getState() < 10) ) continue;

            ContentValues values = new ContentValues();
            values.put(DbEn.CN_STATE, tword.getState());
            String selection = DbEn._ID + " = ?";
            String[] selectionArgs = { String.valueOf(tword.get_id()) };
            db.update(DbEn.TABLE_TDICT, values, selection, selectionArgs);
        }
        db.close();
    }
    
    public static boolean isNotNoun(char art){
        return (art == DbEn.TYPE_ADJECTIVE) 
        || (art == DbEn.TYPE_OTHER) 
        || (art == DbEn.TYPE_VERB);
    }
    
    public static String getUIArt(char art, Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context); 
        String lng_arts = prefs.getString("lng_arts", "der,die,das,nom");
        return getCurUIArt(art, lng_arts);
    }
    
    public static String getCurUIArt(char art, String lng_arts){
        if(lng_arts.length() == 0) return "";
        StringTokenizer st = new StringTokenizer(lng_arts,",",false);
        if(st.countTokens() == 4){
            String[] arts = new String[3];
            for(int i = 0; i < 3; i++){
                arts[i] = st.nextToken();
            }
        //String[] arts = new String[]{"der", "die", "das"};
        List<String> uiArts = Arrays.asList(arts);
        List<Character> dbArts = Arrays.asList(new Character[]{DbEn.TYPE_MASCULINE, DbEn.TYPE_FEMININE, DbEn.TYPE_NEUTRAL});
        if (dbArts.contains(art)){
            String ui_article = uiArts.get(dbArts.indexOf(art));
            if (ui_article.equals(" ") && dbArts.indexOf(art) == 1) return uiArts.get(0);
            else if (ui_article.equals(" ")) return "";
                    else return ui_article;
        }}
        return "";
    }
}
