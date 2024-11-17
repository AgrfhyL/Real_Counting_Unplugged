package com.example.realcountingunplugged;

public class RandomWord {
    private int usages;
    private String word;

    public RandomWord(String word) {
        this.word = word;
        usages = 0;
    }

    public int findMaxUsages(int temp) {
        if (temp >= 96) {
            return 1;
        } else if (temp >= 91) {
            return 1;
        } else if (temp >= 71) {
            return 3;
        } else if (temp >= 51) {
            return 5;
        } else if (temp >= 41) {
            return 7;
        } else if (temp >= 21) {
            return 10;
        } else if (temp >= 11) {
            return 15;
        } else if (temp >= 6) {
            return 20;
        } else {
            return 30;
        }
    }

    public void incrementUsages() {
        usages++;
    }

    public int getUsages() {
        return usages;
    }

    public String getWord() {
        return word;
    }
}
