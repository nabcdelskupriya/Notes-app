package com.example.mynotes;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class PdfActivity extends AppCompatActivity {

    Uri pdfUri;
    Button button;
    TextView pdfname;
    EditText note,title;
    ImageView iv_pdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);


        button = findViewById(R.id.btn_save_pdf);
        pdfname = findViewById(R.id.pdfname_tv);
        note = findViewById(R.id.pdf_note);
        title = findViewById(R.id.pdf_title);
        iv_pdf = findViewById(R.id.pdf_iv);



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        switch (requestCode){
//            case 1212:
//                if (resultCode == RESULT_OK){
//                    Uri uri = data.getData();
//                    String uriString = uri.toString();
//                    File myfile = new File(uriString);
//                    String path = myfile.getAbsolutePath();
//                    String displayname = null;
//
//
//                    if (uriString.startsWith("content://")){
//                        Cursor cursor = null;
//                        try {
//                            cursor = getApplication().getContentResolver().query(uri,null,null,null,null);
//                            displayname = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
//                            pdfname.setText(displayname);
//
//                        }catch (Exception e){
//
//                        }finally {
//                            cursor.close();
//                        }
//                        }else if (uriString.startsWith("file://")){
//                        displayname = myfile.getName();
//
//                    }
//                }
//
//                break;
//        }
        super.onActivityResult(requestCode, resultCode, data);

        try {

            if (requestCode == 1 || resultCode == RESULT_OK ||
                    data != null || data.getData() != null){
                pdfUri = data.getData();
                iv_pdf.setVisibility(View.VISIBLE);

                }
        }catch (Exception e){

            Toast.makeText(this, "Error"+e, Toast.LENGTH_SHORT).show();
        }

    }
}