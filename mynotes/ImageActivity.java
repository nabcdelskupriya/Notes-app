package com.example.mynotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

public class ImageActivity extends AppCompatActivity {

    ImageButton ib_back,ib_options;
    TextView timetv;
    EditText titleEt,notesEt;
    NotesMember member;
    ImageView imageView;
    String time;
    String imageUri;
    Uri imageUrl,uri;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference imageref;
    Button button;
    ProgressBar progressBar;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference;
    UploadTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        progressBar = findViewById(R.id.pb_image);
        storageReference = storage.getReference("images");
        button = findViewById(R.id.btn_save_img);
        imageView = findViewById(R.id.iv_img);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String currentuid= user.getUid();
        imageref = database.getReference("Notes").child(currentuid);

        member = new NotesMember();
        ib_back = findViewById(R.id.ib_back_img);
        ib_options = findViewById(R.id.ib_menu_img);
        timetv = findViewById(R.id.timetv_img);
        titleEt = findViewById(R.id.et_title_img);
        notesEt = findViewById(R.id.et_note_img);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            imageUri = bundle.getString("u");

        }else {


        }

        uri = Uri.parse(imageUri);


            Picasso.get().load(imageUri).into(imageView);
            imageView.setVisibility(View.VISIBLE);


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
                Intent intent = new Intent(ImageActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    uploadImage();

            }
        });

    }


    private String getFileExt(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }


    private void uploadImage(){

        progressBar.setVisibility(View.VISIBLE);
        String note = notesEt.getText().toString();
        String title = titleEt.getText().toString();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = user.getUid();

        final StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + getFileExt(uri));

        uploadTask = reference.putFile(uri);


        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return reference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    imageUrl = task.getResult();

                    Calendar callfordate = Calendar.getInstance();
                    SimpleDateFormat currentdate = new
                            SimpleDateFormat("dd-MMMM-yyyy");
                    final String savedate = currentdate.format(callfordate.getTime());


                    Calendar callfortime = Calendar.getInstance();
                    SimpleDateFormat currenttime = new
                            SimpleDateFormat("HH:mm:ss:ms");
                    final String savetime = currenttime.format(callfortime.getTime());

                    String time = savedate + ":" + savetime;

                    member.setType("IMG");
                    member.setTitle(title);
                    member.setNote(note);
                    member.setUriimage(imageUrl.toString());
                    member.setTime(time);
                    member.setSearch(title.toLowerCase());
                    member.setDelete(System.currentTimeMillis());
                    String key = imageref.push().getKey();
                    imageref.child(key).setValue(member);

                    Toast.makeText(ImageActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            Intent intent = new Intent(ImageActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }, 1000);


                }
            }
        });
        
    }

}