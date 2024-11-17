package com.example.realcountingunplugged;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    private String[] fileAtt;
    private static final int STORAGE_PERMISSION_CODE = 100;
    private TextView outputText;
    private Button mostUsedButton;
    private Button top5Button;
    private EditText textInput;
    private Button wordSentButton;
    private Button uniqueWords;
    private Button backButton;
    private Button createPdfButton;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        outputText = (TextView) findViewById(R.id.textView);
        mostUsedButton = (Button) findViewById(R.id.mostButton);
        top5Button = (Button) findViewById(R.id.topButton);
        textInput = (EditText) findViewById(R.id.enterFile);
        wordSentButton = (Button) findViewById(R.id.countButton);
        uniqueWords = findViewById(R.id.uniqueWords);
        backButton = findViewById(R.id.backButton);
        createPdfButton = findViewById(R.id.createPdfButton);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, start_screen.class);
                startActivity(intent);
            }
        });

        mostUsedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] fileAtt = checkString(textInput.getText().toString());
                outputText.setTextSize(20);
                try {
                    outputText.setText(outputResult(getSortedWords(fileAtt[0], MainActivity.this),fileAtt[1], 0));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        top5Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] fileAtt = checkString(textInput.getText().toString());
                outputText.setTextSize(20);
                try {
                    outputText.setText(outputResult(getSortedWords(fileAtt[0], MainActivity.this),fileAtt[1], 1));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        wordSentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] fileAtt = checkString(textInput.getText().toString());
                try {
                    outputText.setText(wordSentCount(getSortedWords(fileAtt[0], MainActivity.this), fileAtt[0], fileAtt[1]));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        uniqueWords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] fileAtt = checkString(textInput.getText().toString());
                outputText.setTextSize(11);
                try {
                    outputText.setText(uniqueWords(getSortedWords(fileAtt[0], MainActivity.this)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        createPdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileAtt = checkString(textInput.getText().toString());
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("applicaiton/pdf");
                intent.putExtra(Intent.EXTRA_TITLE, "Text_Data.pdf");

                saveData.launch(intent);
            }
        });
    }

    ActivityResultLauncher<Intent> saveData = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
               if (result.getResultCode() == MainActivity.RESULT_OK) {
                   Uri uri = result.getData().getData();
                   try {
                       createPdf(getContentResolver().openOutputStream(uri), fileAtt);
                   } catch (IOException e) {
                       throw new RuntimeException(e);
                   }
               }
            });


    //method for save all to pdf
    public void createPdf(OutputStream os, String[] fileAtt) throws IOException {
        String everything = outputResult(getSortedWords(fileAtt[0], MainActivity.this),fileAtt[1], 1) + "\n" + wordSentCount(getSortedWords(fileAtt[0], MainActivity.this), fileAtt[0], fileAtt[1]);
        everything += "\n" + uniqueWords(getSortedWords(fileAtt[0], MainActivity.this));

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 1500, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(12);

        int yPosition = 50;
        int pageWidth = pageInfo.getPageWidth();
        int padding = 50;
        int maxWidth = pageWidth - 2 * padding;
        draw(canvas, paint, everything, padding, yPosition, maxWidth);
        document.finishPage(page);

        try {
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File output = new File(directory, "blank.pdf");
            document.writeTo(os);

            Toast.makeText(this, "PDF saved to: " + output.getAbsolutePath(), Toast.LENGTH_LONG).show();
            Log.d("PDFCreation", "PDF saved to: " + output.getAbsolutePath());
            document.close();
        } catch (IOException e) {
            Log.e("PDFCreation", "Error: " + e.getMessage(), e);
        } finally {
            document.close();
        }
    }

    private void draw(Canvas canvas, Paint paint, String text, int x, int y, int maxWidth) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            if (paint.measureText(line + word) <= maxWidth) {
                line.append(word).append(" ");
            } else {
                canvas.drawText(line.toString(), x, y, paint);
                y += paint.descent() - paint.ascent();
                line = new StringBuilder(word + " ");
            }
        }
        if (!line.toString().isEmpty()) {
            canvas.drawText(line.toString(), x, y, paint);
        }
    }

    //method for all, determine which text is chosen
    public static String[] checkString(String textName) {
        String[] out = new String[2];
        if (textName.toLowerCase().contains("farm")) {
            out[0] = "Animal+Farm.txt";
            out[1] = "Animal Farm";
        } else if (textName.toLowerCase().contains("nine")) {
            out[0] = "Nineteen+eighty-four.txt";
            out[1] = "Nineteen Eighty-four";
        } else if (textName.toLowerCase().contains("two")) {
            out[0] = "textTwo.txt";
            out[1] = "Text Two";
        } else {
            out[0] = "textOne.txt"; //default
            out[1] = "Text One";
        }
        return out;
    }
    //method for
    public static ArrayList<Word> getSortedWords(String fileAddress, Context context) throws IOException {
        AssetManager assetManager = context.getAssets();
        DataInputStream dataInputStream = new DataInputStream(assetManager.open(String.format(fileAddress)));
        Scanner scanner = new Scanner(dataInputStream);
        ArrayList<String> commons = readFile("commonWords.txt", context);
        ArrayList<Word> wordList = new ArrayList<>();
        while (scanner.hasNext()) { // scanning token by token in the selected texted
            String word = removePunct(scanner.next().toLowerCase()); // set the current word;
            boolean inWordList = false; //wordList is the list of words already found
            int index = 0;
            if (wordList.isEmpty()) { //if wordList is empty, this must be the first word, then we always add it.
                Word a = new Word(word);
                wordList.add(a);
            } else { //if not the first word of the text
                for (int i = 0; i < wordList.size(); i++) { //determine if this word has been found before
                    if (word.equals(wordList.get(i).getWord())) {
                        inWordList = true; //update condition, determining whether word found before
                        index = i; // saves index of the found word in the array, can be used outside of this loop
                    }
                }
                if (inWordList) {
                    wordList.get(index).incrementCount(); //found word, so increment count
                } else {
                    // if word is new, check if it is part of the common word list
                    boolean inCommonList = false;
                    for (int i = 0; i < commons.size(); i++) {
                        if (word.equals(commons.get(i))) {
                            inCommonList = true;
                        }
                    }
                    if (!inCommonList) { // if not in common, we can add the word to the wordList
                        Word aWord = new Word(word);
                        wordList.add(aWord);
                    }
                }
            }
        }
        return sortWords(removeEmpty(wordList));
    }
    public String outputResult(ArrayList<Word> wordList,String fileName, int mode) throws IOException {
        if (mode == 0) {
            return printTopOne(wordList, fileName);
        } else {
            return printTopFive(wordList, fileName);
        }
    }

    // reads the commonWords txt file and stores in an arrayList
    public static ArrayList<String> readFile(String fileName, Context context) throws IOException {
        AssetManager assetManager = context.getAssets();
        DataInputStream commonWordStream = new DataInputStream(assetManager.open(String.format(fileName)));
        ArrayList<String> out = new ArrayList<>();
        Scanner scan = new Scanner(commonWordStream);
        while (scan.hasNextLine()) { // Since all words are on their own line, hasNextLine() will work here
            out.add(scan.nextLine().toLowerCase());
        }
        return out;
    }

    /* I classified a word as: no punctuation in front and back of the word.
    This method tries to remove (peel) the punctuation from the front and back layer by layer until both ends are "clear"
 */
    public static String removePunct(String word) {
        // use ascii values of chars to determine whether the front and back of the token is a letter
        int asciiFirst = word.charAt(0);
        int asciiLast = word.charAt(word.length() - 1);
        while (!(asciiFirst >= 97 && asciiFirst <= 122) || !(asciiLast >= 97 && asciiLast <= 122)) {
            boolean front = !(asciiFirst >= 97 && asciiFirst <= 122); //check which ones, front/back, needs a change
            boolean back = !(asciiLast >= 97 && asciiLast <= 122);
            if (word.length() > 1) {
                if (front) {
                    word = word.substring(1);
                }
                if (back) {
                    word = word.substring(0, word.length() - 1);
                }
            } else if (word.length() == 1) { // this word can be "a" or "I", so I see if front is a letter. if not, the word is not a word
                if (front) {
                    return "thisisnotaword";
                }
            } else { //if string is empty, it must have been an all symbol token (symbols have all been removed)
                return "thisisnotaword";
            }
            /*
            sometimes after passing through the previous condition statement, the "word" will go from for example "00" to ""
            Then a call to charAt() will give an index out of bounds exception.
            Therefore, I check again to see if the string is empty, if so, it is not a word.
            If not, I update the ascii of the first and last characters.
             */
            if (word.length() > 0) {
                asciiFirst = word.charAt(0);
                asciiLast = word.charAt(word.length() - 1);
            } else {
                return "thisisnotaword";
            }
        }
        return word;
    }

    public static ArrayList<Word> removeEmpty(ArrayList<Word> arr) {
        for (int i = 0; i < arr.size(); i++) {
            if (arr.get(i).getWord().equals("thisisnotaword")) {
                arr.remove(i);
                i--;
            }
        }
        return arr;
    }

    public static ArrayList<Word> sortWords(ArrayList<Word> arr) {
        for (int i = 0; i < arr.size(); i++) { //bubble sort algorithm in descending order
            for (int j = 0; j < arr.size() - 1 - i; j++) {
                int count1 = arr.get(j).getCount();
                int count2 = arr.get(j + 1).getCount();
                if (count1 < count2) {
                    Word tempWord = new Word(arr.get(j).getWord());
                    int tempCount = arr.get(j).getCount();
                    tempWord.setCount(tempCount);
                    arr.set(j, arr.get(j + 1));
                    arr.set(j + 1, tempWord);
                }
            }
        }
        return arr;
    }

    // given a sorted arraylist of words (descending count), top five is just first 5 elments
    public static String printTopFive(ArrayList<Word> arr, String textName) {
        String out = "The top 5 most common words in the text \"" + textName + "\" are: " + "\n";
        for (int i = 0; i < 5; i++) {
            out += i + 1 + ". \"" + arr.get(i).getWord() + "\" with " + arr.get(i).getCount() + " occurrences. \n";
        }
        return out;
    }

    public static String printTopOne(ArrayList<Word> arr, String textName) {
        String out = "The most common word in the text \"" + textName + "\" is: " + "\n";
        out += "\"" + arr.get(0).getWord() + "\" with " + arr.get(0).getCount() + " occurrences.";
        return out;
    }

    public String wordSentCount(ArrayList<Word> words, String fileAddress, String textName) throws IOException {
        String out = "";
        int countWord = 0;
        DataInputStream wordStream2 = new DataInputStream(getAssets().open(String.format(fileAddress)));
        Scanner scannerWord = new Scanner(wordStream2);
        while (scannerWord.hasNext()) {
            scannerWord.next();
            countWord++;
        }
        out += "Word count: " + countWord + "\n";
        int countSent = 0;
        DataInputStream wordStream = new DataInputStream(getAssets().open(String.format(fileAddress)));
        Scanner scannerSent = new Scanner(wordStream);
        while (scannerSent.hasNext()) {
            String token = scannerSent.next();
            if (token.contains(".") || token.contains("?") || token.contains("!"))
                countSent++;
        }
        out += "Sentence Count: " + countSent;
        return out;
    }

    public String uniqueWords(ArrayList<Word> words) {
        String out = "";
        int count2 = 0;
        out += ("Unique Words:");
        for (int i = words.size()-1; i >= 0; i--) {
            Word current = words.get(i);
            if (current.getCount() == 1) {
                out += ("\"" + current.getWord() + "\", ");
                count2++;
            } else {
                break;
            }
        }
        out += "\n" + "There are a total of " + count2 + " unique words in this text.";
        return out;
    }
}