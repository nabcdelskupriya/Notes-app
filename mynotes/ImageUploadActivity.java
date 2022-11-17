package com.example.mynotes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.ArrayList;
import java.util.Calendar;

public class ImageUploadActivity extends AppCompatActivity {

    ArrayList<Uri> imagelist = new ArrayList<Uri>();
    private  int upload_count = 0;
    DatabaseReference roomlist;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    String currentuid,address,sendername,roomname;
    RoomNoteMember member;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference;
    Uri imageUrl,ImageUri;
    Button uploadbtn,choosebtn;
    TextView tvNumberof;
    ProgressBar pv;
    EditText editText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);

        storageReference = storage.getReference("files");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentuid = user.getUid();

        tvNumberof = findViewById(R.id.tv_img_up);
        uploadbtn = findViewById(R.id.btn_up_img);
        pv = findViewById(R.id.pb_up_img);
        editText = findViewById(R.id.et_image_name);
        choosebtn = findViewById(R.id.btn_choose_img);

        member = new RoomNoteMember();
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            roomname = extras.getString("rn");
            address = extras.getString("a");
            sendername = extras.getString("n");

        } else {
            Toast.makeText(this, "null value", Toast.LENGTH_SHORT).show();
        }
        roomlist = database.getReference().child("notelist").child(address);


        choosebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseimage();
            }
        });



        uploadbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pv.setVisibility(View.VISIBLE);
                for (upload_count = 0; upload_count <imagelist.size() ;upload_count++){

                    final StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + "jpg");

                    Uri selectedUri = imagelist.get(upload_count);

                    UploadTask  uploadTask = reference.putFile(selectedUri);

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
                                Calendar callfortime = Calendar.getInstance();
                                SimpleDateFormat currenttime = new
                                        SimpleDateFormat("HH:mm:ss a");
                                final String savetime = currenttime.format(callfortime.getTime());

                                member.setType("IMG");
                                member.setNote(editText.getText().toString().trim());
                                member.setUrl(imageUrl.toString());
                                member.setTime(savetime);
                                member.setSendername(sendername);
                                member.setSenderuid(currentuid);
                                member.setCode(5);
                                String key = roomlist.push().getKey();
                                roomlist.child(key).setValue(member);

                                Toast.makeText(ImageUploadActivity.this, "Sent", Toast.LENGTH_SHORT).show();
                                pv.setVisibility(View.GONE);
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        String token = "/topics/"+address;

                                        FcmNotificationsSender notificationsSender =
                                                new FcmNotificationsSender(token, "Notes app",  "[" +roomname+"] "+ sendername+ " Sent Image",
                                                        getApplicationContext(), ImageUploadActivity.this);

                                        notificationsSender.SendNotifications();

                                        Intent intent = new Intent(ImageUploadActivity.this,OpenRoon.class);
                                        startActivity(intent);
                                        finish();

                                    }
                                }, 1000);

                            }
                        }
                    });


                }



            }
        });
    }

    private void chooseimage() {

        Intent intentstory = new Intent(Intent.ACTION_GET_CONTENT);

        intentstory.setType("image/*");
        intentstory.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        startActivityForResult(intentstory, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == 1 || resultCode == RESULT_OK ||
                    data != null || data.getData() != null){

                int countclipdata = data.getClipData().getItemCount();

                int currentImageselect =0;

                while (currentImageselect < countclipdata){

                    ImageUri =data.getClipData().getItemAt(currentImageselect).getUri();
                    imagelist.add(ImageUri);
                    currentImageselect = currentImageselect +1;
                }
                tvNumberof.setVisibility(View.VISIBLE);
                tvNumberof.setText("you have selected" + imagelist.size()+ " images");

            }



        }catch (Exception e){

            Toast.makeText(this, "please select any file", Toast.LENGTH_SHORT).show();
        }

    }

}