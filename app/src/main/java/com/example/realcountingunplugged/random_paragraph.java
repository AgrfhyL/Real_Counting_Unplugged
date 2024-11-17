package com.example.realcountingunplugged;

import static com.itextpdf.svg.converter.SvgConverter.createPdf;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class random_paragraph extends AppCompatActivity {

    private TextView textOutput;
    private EditText editText;
    private Button button;
    private Button backButton2;
    private Button createPdfButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_random_paragraph);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textOutput = findViewById(R.id.textOutput);
        button = findViewById(R.id.button);
        editText = findViewById(R.id.editTextNumber);
        backButton2 = findViewById(R.id.backButton2);
        createPdfButton = findViewById(R.id.createRandPDF);

        backButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(random_paragraph.this, start_screen.class);
                startActivity(intent);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int temp = Integer.parseInt(editText.getText().toString());
                try {
                    textOutput.setText(sendRandParagraph(temp));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        createPdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("applicaiton/pdf");
                intent.putExtra(Intent.EXTRA_TITLE, "Random_Paragraph.pdf");

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
                        createPdf(getContentResolver().openOutputStream(uri));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

    public void createPdf(OutputStream os) throws IOException {
        String everything = textOutput.getText().toString();

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

    public String sendRandParagraph(int temp) throws IOException {
        RandomWords randomWords = new RandomWords(temp, this);
        return randomWords.outputParagraph();
    }
}