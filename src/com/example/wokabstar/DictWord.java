package com.example.wokabstar;

public class DictWord {

    private char art;
    private String in_word;
    private String out_word;
    private int state;
    private int level;
    private int _id;
    
    public int get_id() {
        return _id;
    }

    public DictWord(){
        _id = -1;
        in_word  = "";
    }

    public DictWord(int id) {
        _id = id;
    }

    public char getArt() {
        return art;
    }

    public void setArt(char art) {
        this.art = art;
    }

    public String getForeignWord() {
        return in_word;
    }

    public void setForeignWord(String in_word) {
        this.in_word = in_word;
    }

    public String getTranslation() {
        return out_word;
    }

    public void setTranslation(String out_word) {
        this.out_word = out_word;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}