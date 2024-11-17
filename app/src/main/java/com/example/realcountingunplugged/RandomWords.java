package com.example.realcountingunplugged;

import android.content.Context;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class RandomWords {
    private ArrayList<RandomWord> pool = new ArrayList<>();
    private int temperature;
    private Context context;

    public RandomWords (int temperature, Context context) throws IOException {
        this.context = context;
        this.temperature = temperature;
        int counter = 0;
        ArrayList<String> allWord = readWords();
        while (counter <= findWordPoolCount(temperature)) {
            int randomIndex = (int) (Math.random() * fileLength("allWords.txt"));
            RandomWord randWord = new RandomWord(allWord.get(randomIndex));
            pool.add(randWord);
            counter++;
        }
    }

    public int findWordPoolCount(int temp) {
        if (temp >= 91) {
            return 100;
        } else if (temp >= 81) {
            return 90;
        } else if (temp >= 71) {
            return 80;
        } else if (temp >= 61) {
            return 70;
        } else if (temp >= 51) {
            return 60;
        } else if (temp >= 41) {
            return 50;
        } else if (temp >= 31) {
            return 40;
        } else if (temp >= 21) {
            return 30;
        } else if (temp >= 11) {
            return 20;
        } else {
            return 10;
        }
    }

    public int fileLength(String fileAddress) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(context.getAssets().open(String.format(fileAddress)));
        Scanner scanner = new Scanner(dataInputStream);
        int len = 0;
        while (scanner.hasNext()) {
            scanner.next();
            len++;
        }
        return len;
    }

    public ArrayList<String> readWords() throws IOException {
        ArrayList<String> out = new ArrayList<>();
        DataInputStream dataInputStream = new DataInputStream(context.getAssets().open(String.format("allWords.txt")));
        Scanner scanner = new Scanner(dataInputStream);
        while (scanner.hasNext()) {
            out.add(scanner.next());
        }
        return out;
    }

    public String outputParagraph() {
        String out = "";
        for (int i = 0; i < 100; i++) {
            int randomIndex = (int) (Math.random() * pool.size());
            RandomWord randWord = pool.get(randomIndex);
            if (randWord.getUsages() <= randWord.findMaxUsages(temperature)) {
                out += randWord.getWord().toLowerCase() + " ";
                randWord.incrementUsages();
            }
        }
        return out;
    }
}
