package com.example.realcountingunplugged;

public class Word {
    private String word;
    private int count;

    public Word(String word){
        this.word = word;
        count = 1;
    }

    public void incrementCount() {
        count++;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getCount() {
        return count;
    }

    public String getWord() {
        return word;
    }
}
