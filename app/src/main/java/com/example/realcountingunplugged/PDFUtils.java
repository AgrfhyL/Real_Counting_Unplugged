package com.example.realcountingunplugged;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;

public class PDFUtils {
    private Context context;

    public PDFUtils(Context context) {
        this.context = context;
    }

    // Method to read PDF from URI
    public String readPDFFromUri(Uri uri) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

            // Create PDF reader and document
            PdfReader pdfReader = new PdfReader(new FileInputStream(fileDescriptor));
            PdfDocument pdfDocument = new PdfDocument(pdfReader);

            // Get number of pages
            int numberOfPages = pdfDocument.getNumberOfPages();

            // Extract text from each page
            for (int i = 1; i <= numberOfPages; i++) {
                LocationTextExtractionStrategy strategy = new LocationTextExtractionStrategy();
                String pageText = PdfTextExtractor.getTextFromPage(pdfDocument.getPage(i), strategy);
                stringBuilder.append(pageText);
                stringBuilder.append("\n"); // Add line break between pages
            }

            pdfDocument.close();
            pdfReader.close();
            parcelFileDescriptor.close();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error reading PDF: " + e.getMessage();
        }

        return stringBuilder.toString();
    }

    // Method to read PDF from file path
    public String readPDFFromPath(String filePath) {
        return readPDFFromUri(Uri.fromFile(new File(filePath)));
    }
}