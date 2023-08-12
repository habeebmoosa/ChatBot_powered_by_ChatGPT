package com.generativeaichatbot.gaichatbot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExportsMessage extends AppCompatActivity {
    private static final int CREATE_PDF_REQUEST_CODE = 123;
    private static final int REQUEST_CODE_SAVE_DOC = 121;
    private static PdfDocument pdfDocument;
    private static XWPFDocument document;

    public static void createPdf(Context context, String pdfText, String name) {
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(1654, 2362, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);

        float baselineFontSize = 10;

        float scaleFactor = context.getResources().getDisplayMetrics().scaledDensity;

        float textSizeSP = baselineFontSize * scaleFactor;
        textPaint.setTextSize(textSizeSP);

        int margin = 25;
        int width = pageInfo.getPageWidth() - 2 * margin;

        StaticLayout staticLayout = new StaticLayout(pdfText, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

        canvas.save();
        canvas.translate(margin, margin);
        staticLayout.draw(canvas);
        canvas.restore();

        pdfDocument.finishPage(page);

        savePdf(context, pdfDocument, name);

        pdfDocument.close();
    }


    private static void savePdf(Context context, PdfDocument pdfDocument, String name) {

        File pdfDir = new File(context.getExternalFilesDir(null), "PDFs");
        if (!pdfDir.exists()) {
            pdfDir.mkdir();
        }

        File pdfFile = new File(pdfDir, name+".pdf");

        try {
            FileOutputStream outputStream = new FileOutputStream(pdfFile);
            pdfDocument.writeTo(outputStream);
            Toast.makeText(context, "PDF created successfully", Toast.LENGTH_SHORT).show();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void createDoc(Context context, String docText, String name) {
        XWPFDocument document = new XWPFDocument();
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();


        String[] lines = docText.split("\n");
        for (String line : lines) {
            run.setText(line);
            run.addBreak();
        }

        saveDoc(context, document, name);
    }

    private static void saveDoc(Context context, XWPFDocument document, String name) {
        File docDir = new File(context.getExternalFilesDir(null), "Documents");
        if (!docDir.exists()) {
            docDir.mkdir();
        }

        File docFile = new File(docDir, name+".docx");

        try {
            FileOutputStream outputStream = new FileOutputStream(docFile);
            document.write(outputStream);
            Toast.makeText(context, "DOC created successfully", Toast.LENGTH_SHORT).show();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



//    public static String xh1g;
//
//    public static void createDoc(Activity activity, String docText, String name) {
//        xh1g = docText;
//        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
//        intent.putExtra(Intent.EXTRA_TITLE, name + ".docx");
//
//        activity.startActivityForResult(intent, REQUEST_CODE_SAVE_DOC);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == REQUEST_CODE_SAVE_DOC && resultCode == Activity.RESULT_OK && data != null) {
//            Log.d("doc_created_debug","docx8");
//            if (xh1g != null) {
//                Uri uri = data.getData();
//                if (uri != null) {
//                    try {
//                        Log.d("doc_created_debug","docx9");
//                        OutputStream outputStream = getContentResolver().openOutputStream(uri);
//                        if (outputStream != null) {
//                            XWPFDocument document = new XWPFDocument();
//                            XWPFParagraph paragraph = document.createParagraph();
//                            XWPFRun run = paragraph.createRun();
//                            Log.d("doc_created_debug","docx10");
//                            String[] lines = xh1g.split("\n");
//                            for (String line : lines) {
//                                run.setText(line);
//                                run.addBreak();
//                            }
//
//                            Log.d("doc_created_debug","docx11");
//                            document.write(outputStream);
//                            outputStream.close();
//                            Toast.makeText(this, "DOC created successfully", Toast.LENGTH_SHORT).show();
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        Log.d("doc_created_debug","docx12 "+e);
//                    }
//                }
//            }
//        }
//    }







//    public static void createPdf(Context context, String pdfText) {
//        pdfDocument = new PdfDocument();
//        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(1654, 2362, 1).create();
//        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
//        Canvas canvas = page.getCanvas();
//
//        TextPaint textPaint = new TextPaint();
//        textPaint.setColor(Color.BLACK);
//
//        float baselineFontSize = 10;
//
//        float scaleFactor = context.getResources().getDisplayMetrics().scaledDensity;
//
//        float textSizeSP = baselineFontSize * scaleFactor;
//        textPaint.setTextSize(textSizeSP);
//
//        int margin = 25;
//        int width = pageInfo.getPageWidth() - 2 * margin;
//
//        StaticLayout staticLayout = new StaticLayout(pdfText, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
//
//        canvas.save();
//        canvas.translate(margin, margin);
//        staticLayout.draw(canvas);
//        canvas.restore();
//
//        pdfDocument.finishPage(page);
//
//        // Create the PDF file using an Intent
//        savePdfWithIntent(context, pdfDocument);
////        savePdf(context, pdfDocument);
//
//        pdfDocument.close();
//    }
//
//    private static void savePdfWithIntent(Context context, PdfDocument pdfDocument) {
//        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setType("application/pdf");
//        intent.putExtra(Intent.EXTRA_TITLE, "example.pdf");
//
//        if (intent.resolveActivity(context.getPackageManager()) != null) {
//            ((Activity) context).startActivityForResult(intent, CREATE_PDF_REQUEST_CODE);
//        } else {
//            // Handle the case where no app supports the ACTION_CREATE_DOCUMENT intent
//            Toast.makeText(context, "No app available to handle PDF creation.", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == CREATE_PDF_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            Uri uri = data.getData();
//            try {
//                OutputStream outputStream = getContentResolver().openOutputStream(uri);
//                pdfDocument.writeTo(outputStream);
//                outputStream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }




    //    public static void createDoc(Context context, String docText) {
//        document = new XWPFDocument();
//        XWPFParagraph paragraph = document.createParagraph();
//        XWPFRun run = paragraph.createRun();
//
//        String[] lines = docText.split("\n");
//        for (String line : lines) {
//            run.setText(line);
//            run.addBreak();
//        }
//
//        saveDocWithIntent(context, document);
//    }
//
//    private static void saveDocWithIntent(Context context, XWPFDocument document) {
//        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
//        intent.putExtra(Intent.EXTRA_TITLE, "example.docx");
//
//        if (intent.resolveActivity(context.getPackageManager()) != null) {
//            ((AppCompatActivity) context).startActivityForResult(intent, CREATE_DOCX_REQUEST_CODE);
//        } else {
//            // Handle the case where no app supports the ACTION_CREATE_DOCUMENT intent
//            Toast.makeText(context, "No app available to handle DOCX creation.", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == CREATE_DOCX_REQUEST_CODE && resultCode == RESULT_OK) {
//            if (data != null) {
//                Uri uri = data.getData();
//                if (uri != null) {
//                    try {
//                        document.write(getContentResolver().openOutputStream(uri));
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//    }

}