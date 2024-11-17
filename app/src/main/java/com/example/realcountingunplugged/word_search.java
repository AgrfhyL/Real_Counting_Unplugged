package com.example.realcountingunplugged;

import static com.example.realcountingunplugged.MainActivity.checkString;
import static com.example.realcountingunplugged.MainActivity.getSortedWords;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.ArrayList;

public class word_search extends AppCompatActivity {

    private Button searchButton;
    private TextView textView;
    private EditText fileNameInput;
    private EditText wordInput;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_word_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        searchButton = findViewById(R.id.searchButton);
        textView = findViewById(R.id.textView2);
        fileNameInput = findViewById(R.id.enterFile2);
        wordInput = findViewById(R.id.enterWord);
        backButton = findViewById(R.id.backButton3);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(word_search.this, start_screen.class);
                startActivity(intent);
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileAtt[] = checkString((fileNameInput.getText().toString()));
                String word = wordInput.getText().toString();
                try {
                    Word searchedWord = searchWord(word, getSortedWords(fileAtt[0], word_search.this)); //catch error from this line
                    if (searchedWord.getWord().equals("The word you searched for is not in the text.")) {
                        textView.setText("The word you searched for is not in the text.");
                    } else {
                        String output = "The word you searched for: \"" + searchedWord.getWord() + "\" has " + searchedWord.getCount() + " usages in the text";
                        textView.setText(output);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public Word searchWord(String word, ArrayList<Word> words) {
        word = word.toLowerCase();
        Word notFound = new Word("The word you searched for is not in the text.");
        for (int i = 0; i < words.size(); i++) {
            if (words.get(i).getWord().equals(word)) {
                return words.get(i);
            }
        }
        return notFound;
    }
}