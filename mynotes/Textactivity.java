package com.example.mynotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Textactivity extends AppCompatActivity {

    ImageButton ib_back,ib_options;
    TextView timetv;
    EditText titleEt,notesEt;
    NotesMember member;
    String time;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reference;
    Button button;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textactivity);

        progressBar = findViewById(R.id.pb_image);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentuid= user.getUid();
        button = findViewById(R.id.btn_save);
        reference = database.getReference("Notes").child(currentuid);
        member = new NotesMember();
        ib_back = findViewById(R.id.ib_back);
        ib_options = findViewById(R.id.ib_menu);
        timetv = findViewById(R.id.timetv);
        titleEt = findViewById(R.id.et_title);
        notesEt = findViewById(R.id.et_note);


        Calendar callfordate = Calendar.getInstance();
        SimpleDateFormat currentdate = new
                SimpleDateFormat("dd-MMMM-yyyy");
        final  String savedate = currentdate.format(callfordate.getTime());


        Calendar callfortime = Calendar.getInstance();
        SimpleDateFormat currenttime = new
                SimpleDateFormat("HH:mm:ss");
        final  String savetime = currenttime.format(callfortime.getTime());
         time = savedate+":"+savetime;

        timetv.setText(time);

        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Textactivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String note = notesEt.getText().toString().trim();
                String title = titleEt.getText().toString().trim();

                    member.setType("TXT");
                    member.setTitle(title);
                    member.setNote(note);
                    member.setTime(time);
                    member.setSearch(title.toLowerCase());
                    member.setDelete(System.currentTimeMillis());

                    String key = reference.push().getKey();
                    reference.child(key).setValue(member);

                    Toast.makeText(Textactivity.this, "Saved", Toast.LENGTH_SHORT).show();

                        }
                    });
                }
    }