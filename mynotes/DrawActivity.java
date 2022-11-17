package com.example.mynotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.DrawableWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.example.mynotes.Display.colorlist;
import static com.example.mynotes.Display.current_brush;
import static com.example.mynotes.Display.pathlist;

public class DrawActivity extends AppCompatActivity {


    public static android.graphics.Path path = new Path();
    public static Paint paint_brush = new Paint();


    ImageButton btn_pencil,btn_eraser;
    Button savebtn;
    Display display;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference imageref;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        final RelativeLayout relativeLayout = findViewById(R.id.rl_paint);

        btn_eraser = findViewById(R.id.eraser);
        display = findViewById(R.id.draw);
        savebtn = findViewById(R.id.save_draw);
        btn_pencil = findViewById(R.id.pencil_draw);

        btn_pencil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                paint_brush.setColor(Color.BLACK);
                currentColor(paint_brush.getColor());

            }
        });

        btn_eraser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pathlist.clear();
                colorlist.clear();
                path.reset();
            }
        });

        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Bitmap bitmap = Bitmap.createBitmap(relativeLayout.getWidth(), relativeLayout.getHeight(),
                        Bitmap.Config.ARGB_8888);

                Canvas canvas = new Canvas(bitmap);
                relativeLayout.draw(canvas);



                LayoutInflater inflater  = LayoutInflater.from(DrawActivity.this);
                 view = inflater.inflate(R.layout.draw_preview,null);

                ImageView imageView = view.findViewById(R.id.iv_draw);
                Button remove = view.findViewById(R.id.del_draw);
                Button upload = view.findViewById(R.id.up_draw);
                ProgressBar pb = view.findViewById(R.id.pb_draw);
                EditText editText = view.findViewById(R.id.draw_title);

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageReference;
                storageReference = storage.getReference("images");

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                String currentuid= user.getUid();
                imageref = database.getReference("Notes").child(currentuid);

                AlertDialog alertDialog = new AlertDialog.Builder(DrawActivity.this)
                        .setView(view)
                        .create();

                alertDialog.show();

                imageView.setImageBitmap(bitmap);

                remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        alertDialog.dismiss();
                    }
                });


                upload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        pb.setVisibility(View.VISIBLE);
                        String title = editText.getText().toString().trim();
                        imageView.setDrawingCacheEnabled(true);
                        imageView.buildDrawingCache();
                        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] data = baos.toByteArray();

                        NotesMember member;
                        member = new NotesMember();


                        final StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + "jpg");
                        UploadTask uploadTask = reference.putBytes(data);


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
                                 Uri imageUrl = task.getResult();

                                 pb.setVisibility(View.GONE);
                                    Calendar callfordate = Calendar.getInstance();
                                    SimpleDateFormat currentdate = new
                                            SimpleDateFormat("dd-MMMM-yyyy");
                                    final String savedate = currentdate.format(callfordate.getTime());


                                    Calendar callfortime = Calendar.getInstance();
                                    SimpleDateFormat currenttime = new
                                            SimpleDateFormat("HH:mm:ss:ms");
                                    final String savetime = currenttime.format(callfortime.getTime());

                                    String time = savedate + ":" + savetime;

                                    member.setType("i");
                                    member.setTitle(title);
                                    member.setNote("note");
                                     member.setUriimage(imageUrl.toString());
 //                                    member.setTime(time);
                                    member.setSearch(title.toLowerCase());
                                     member.setDelete(System.currentTimeMillis());
                                    String key = imageref.push().getKey();
                                    imageref.child(key).setValue(member);

                                    Toast.makeText(DrawActivity.this, "Saved", Toast.LENGTH_SHORT).show();

                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {

                                            Intent intent = new Intent(DrawActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }, 1000);


                                }
                            }
                        });

                    }
                });
            }
        });

    }


    public void redclicked(View view) {

        paint_brush.setColor(Color.RED);
        currentColor(paint_brush.getColor());
    }

    public void yellowclicked(View view) {

        paint_brush.setColor(Color.YELLOW);
        currentColor(paint_brush.getColor());
    }

    public void blackclicked(View view) {

        paint_brush.setColor(Color.GREEN);
        currentColor(paint_brush.getColor());
    }

    public void blueclicked(View view) {

        paint_brush.setColor(Color.BLUE);
        currentColor(paint_brush.getColor());
    }

    public void whiteclicked(View view) {

        paint_brush.setColor(Color.WHITE);
        currentColor(paint_brush.getColor());
    }

    public void currentColor(int c){
        current_brush = c;
        path = new Path();
    }



}