package com.example.mynotes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class OpenRoon extends AppCompatActivity {


    String roomname,address,adminId,name,category,currentuid,userstatus;
    Button adduserbtn;
    TextView rnametv;
    ImageButton ibadd,ibsend;
    int PICK_IMAGE = 1;
    Uri selectedUri,imageUrl;
    EditText etsendnotes;
    DatabaseReference nameref,roomlist,memberRef,roomRef;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference;
    RoomNoteMember member ;
    RecyclerView recyclerView;
    LinearLayoutManager manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_roon);

        storageReference = storage.getReference("files");
        rnametv = findViewById(R.id.roomname);
        adduserbtn = findViewById(R.id.adduser_btn);
        ibadd = findViewById(R.id.ib_add);
        ibsend = findViewById(R.id.ib_send);
        etsendnotes = findViewById(R.id.et_sendnotes);
        recyclerView = findViewById(R.id.rv_message);

        manager = new LinearLayoutManager(this);
        //manager.setStackFromEnd(true);
       // manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        nameref = database.getReference("users");
        member = new RoomNoteMember();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentuid = user.getUid();

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            roomname = extras.getString("rn");
            address = extras.getString("a");
            adminId = extras.getString("ai");

        } else {

        }

        FirebaseMessaging.getInstance().subscribeToTopic(address);

        if (adminId.equals(currentuid)){
            adduserbtn.setText("Add users");
            userstatus = "add";
        }else {
            adduserbtn.setText("leave");
            userstatus="leave";
        }

        roomlist = database.getReference().child("notelist").child(address);



        nameref.child(currentuid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    name = (String) snapshot.child("name").getValue();
                    category = (String) snapshot.child("category").getValue();
                }else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        rnametv.setText(roomname);

        adduserbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (userstatus.equals("add")){

                    Intent intent = new Intent(OpenRoon.this,RoomActivity.class);
                    intent.putExtra("rn",roomname);
                    intent.putExtra("ai",adminId);
                    intent.putExtra("a",address);
                    startActivity(intent);
                }else if (userstatus.equals("leave")){

                    memberRef = database.getReference("members").child(address);
                    roomRef = database.getReference("Rooms").child(currentuid);

                   roomRef.child(address).removeValue();

                    memberRef.child(currentuid).removeValue();

//                    Intent intent = new Intent(OpenRoon.this,Fragment2.class);
//                    startActivity(intent);


                }




            }
        });

        rnametv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(OpenRoon.this,SHowMembers.class);
                intent.putExtra("a",address);
                startActivity(intent);

            }
        });

        ibadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               openBs();

            }
        });

        ibsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Calendar callfortime = Calendar.getInstance();
                SimpleDateFormat currenttime = new
                        SimpleDateFormat("HH:mm:ss a");
                final String savetime = currenttime.format(callfortime.getTime());
                String data = etsendnotes.getText().toString().trim();

                member.setSendername(name);
                member.setSenderuid(currentuid);
                member.setType("TXT");
                member.setNote(data);
                member.setTime(savetime);
                member.setUrl("url");
                member.setCode(1);

                String key = roomlist.push().getKey();
                roomlist.child(key).setValue(member);

                etsendnotes.setText("");

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String token = "/topics/"+address;

                        FcmNotificationsSender notificationsSender =
                                new FcmNotificationsSender(token, "Notes app",  "[" +roomname+"]"+ name+ ":"+ data,
                                        getApplicationContext(), OpenRoon.this);

                        notificationsSender.SendNotifications();

                    }
                }, 1000);


            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<RoomNoteMember> options1 =
                new FirebaseRecyclerOptions.Builder<RoomNoteMember>()
                        .setQuery(roomlist,RoomNoteMember.class)
                        .build();

        FirebaseRecyclerAdapter<RoomNoteMember,ViewHolderMessage> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<RoomNoteMember, ViewHolderMessage>(options1) {
                    @Override
                    protected void onBindViewHolder(@NonNull ViewHolderMessage holder, int position, @NonNull RoomNoteMember model) {

                        holder.setmessage(getApplication(),model.getSendername(),model.getSenderuid(),model.getNote(),
                                model.getTime(),model.getType(),model.getUrl(),model.getCode());

                        String type = getItem(position).getType();
                        String url = getItem(position).getUrl();
                        String filename = getItem(position).getNote();


                        holder.ivdownload.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                download(type,url,filename);
                            }
                        });

                        holder.texttv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {
                                    Uri uri = Uri.parse(filename); // missing 'http://' will cause crashed
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    startActivity(intent);

                                }catch (Exception e){
                                    Toast.makeText(OpenRoon.this, "Only for Links", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }


                    @NonNull
                    @Override
                    public ViewHolderMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.message_layout,parent,false);

                        return new ViewHolderMessage(view);
                    }
                };
        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    private void download(String type, String url,String filename) {

        switch (type) {
            case "TXT":


                break;
            case "PPT":

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                        DownloadManager.Request.NETWORK_MOBILE);
                request.setTitle("Download");
                request.setDescription("Downloading image....");
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename + System.currentTimeMillis() + ".pptx");
                DownloadManager manager = (DownloadManager) OpenRoon.this.getSystemService(Context.DOWNLOAD_SERVICE);
                manager.enqueue(request);

                Toast.makeText(getApplicationContext(), "Downloading", Toast.LENGTH_SHORT).show();

                break;
            case "PDF":

                DownloadManager.Request pdf = new DownloadManager.Request(Uri.parse(url));
                pdf.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                        DownloadManager.Request.NETWORK_MOBILE);
                pdf.setTitle("Download");
                pdf.setDescription("Downloading image....");
                pdf.allowScanningByMediaScanner();
                pdf.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                pdf.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename + System.currentTimeMillis() + ".pdf");
                DownloadManager manager2 = (DownloadManager) OpenRoon.this.getSystemService(Context.DOWNLOAD_SERVICE);
                manager2.enqueue(pdf);

                Toast.makeText(getApplicationContext(), "Downloading", Toast.LENGTH_SHORT).show();

                break;
            case "DOCX":

                DownloadManager.Request docx = new DownloadManager.Request(Uri.parse(url));
                docx.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                        DownloadManager.Request.NETWORK_MOBILE);
                docx.setTitle("Download");
                docx.setDescription("Downloading image....");
                docx.allowScanningByMediaScanner();
                docx.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                docx.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename + System.currentTimeMillis() + ".docx");
                DownloadManager managerdocs = (DownloadManager) OpenRoon.this.getSystemService(Context.DOWNLOAD_SERVICE);
                managerdocs.enqueue(docx);

                Toast.makeText(getApplicationContext(), "Downloading", Toast.LENGTH_SHORT).show();

                break;
            case "IMG":

                DownloadManager.Request image = new DownloadManager.Request(Uri.parse(url));
                image.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                        DownloadManager.Request.NETWORK_MOBILE);
                image.setTitle("Download");
                image.setDescription("Downloading image....");
                image.allowScanningByMediaScanner();
                image.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                image.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename + System.currentTimeMillis() + ".jpg");
                DownloadManager managerimg = (DownloadManager) OpenRoon.this.getSystemService(Context.DOWNLOAD_SERVICE);
                managerimg.enqueue(image);

                Toast.makeText(getApplicationContext(), "Downloading", Toast.LENGTH_SHORT).show();

                break;

            case "DRIVE":

                Toast.makeText(this, "Cannot be downloaded", Toast.LENGTH_SHORT).show();
                break;

        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == PICK_IMAGE || resultCode == RESULT_OK ||
                    data != null || data.getData() != null){

                selectedUri = data.getData();

                switch (requestCode){
                    case 2:
                        previewpdf(selectedUri);
                        break;
                    case 3:
                        previewDoc(selectedUri);
                        Toast.makeText(this, "docx", Toast.LENGTH_SHORT).show();
                        break;

                    case 4:
                        previewPPt(selectedUri);
                        Toast.makeText(this, "ppt", Toast.LENGTH_SHORT).show();
                        break;

                }

            }else {

                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            }

        }catch (Exception e){

            Toast.makeText(this, "please select any file", Toast.LENGTH_SHORT).show();
        }
    }

    private void previewPPt(Uri selectedUri) {

        LayoutInflater inflater  = LayoutInflater.from(OpenRoon.this);
        View view = inflater.inflate(R.layout.m_preview,null);

        AlertDialog alertDialog = new AlertDialog.Builder(OpenRoon.this)
                .setView(view)
                .create();
        alertDialog.show();

        ImageView imageView = view.findViewById(R.id.iv_preview);
        Button sendbtn = view.findViewById(R.id.btn_previewsend);
        ProgressBar pb = view.findViewById(R.id.pb_preview);
        EditText previewEt = view.findViewById(R.id.filename_et_preview);
        imageView.setVisibility(View.GONE);

        previewEt.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getApplication(),
                R.drawable.ic_baselineppt),
                null,null,null);

        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pb.setVisibility(View.VISIBLE);
                final StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + "pptx");

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

                            member.setType("PPT");
                            member.setNote(previewEt.getText().toString().trim());
                            member.setUrl(imageUrl.toString());
                            member.setTime(savetime);
                            member.setSendername(name);
                            member.setSenderuid(currentuid);
                            member.setCode(2);

                            String key = roomlist.push().getKey();
                            roomlist.child(key).setValue(member);

                            Toast.makeText(OpenRoon.this, "Sent", Toast.LENGTH_SHORT).show();
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    String token = "/topics/"+address;

                                    FcmNotificationsSender notificationsSender =
                                            new FcmNotificationsSender(token, "Notes app",  "[" +roomname+"] "+ name+ "sent PPT",
                                                    getApplicationContext(), OpenRoon.this);

                                    notificationsSender.SendNotifications();

                                }
                            }, 1000);


                            pb.setVisibility(View.GONE);



                        }
                    }
                });

            }
        });


    }

    private void previewDoc(Uri selectedUri) {

        LayoutInflater inflater  = LayoutInflater.from(OpenRoon.this);
        View view = inflater.inflate(R.layout.m_preview,null);

        AlertDialog alertDialog = new AlertDialog.Builder(OpenRoon.this)
                .setView(view)
                .create();
        alertDialog.show();

        ImageView imageView = view.findViewById(R.id.iv_preview);
        Button sendbtn = view.findViewById(R.id.btn_previewsend);
        ProgressBar pb = view.findViewById(R.id.pb_preview);
        EditText previewEt = view.findViewById(R.id.filename_et_preview);
        imageView.setVisibility(View.GONE);

        previewEt.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getApplication(),
                R.drawable.ic_baseline_msword),
                null,null,null);

        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pb.setVisibility(View.VISIBLE);
                final StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + "docx");

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

                            member.setType("DOCX");
                            member.setNote(previewEt.getText().toString().trim());
                            member.setUrl(imageUrl.toString());
                            member.setTime(savetime);
                            member.setSendername(name);
                            member.setSenderuid(currentuid);
                            member.setCode(4);

                            String key = roomlist.push().getKey();
                            roomlist.child(key).setValue(member);

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    String token = "/topics/"+address;

                                    FcmNotificationsSender notificationsSender =
                                            new FcmNotificationsSender(token, "Notes app",  "[" +roomname+"] "+ name+ "Sent Document",
                                                    getApplicationContext(), OpenRoon.this);

                                    notificationsSender.SendNotifications();

                                }
                            }, 1000);

                            Toast.makeText(OpenRoon.this, "Sent", Toast.LENGTH_SHORT).show();
                            pb.setVisibility(View.GONE);



                        }
                    }
                });



            }
        });


    }

    private void previewpdf(Uri selectedUri) {

        LayoutInflater inflater  = LayoutInflater.from(OpenRoon.this);
        View view = inflater.inflate(R.layout.m_preview,null);

        AlertDialog alertDialog = new AlertDialog.Builder(OpenRoon.this)
                .setView(view)
                .create();
        alertDialog.show();

        ImageView imageView = view.findViewById(R.id.iv_preview);
        Button sendbtn = view.findViewById(R.id.btn_previewsend);
        ProgressBar pb = view.findViewById(R.id.pb_preview);
        EditText previewEt = view.findViewById(R.id.filename_et_preview);
        imageView.setVisibility(View.GONE);

        previewEt.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getApplication(),R.drawable.ic_baseline_pdf),
                null,null,null);

        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pb.setVisibility(View.VISIBLE);
                final StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + "pdf");

                UploadTask uploadTask = reference.putFile(selectedUri);


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

                            member.setType("PDF");
                            member.setNote(previewEt.getText().toString().trim());
                            member.setUrl(imageUrl.toString());
                            member.setTime(savetime);
                            member.setSendername(name);
                            member.setSenderuid(currentuid);
                            member.setCode(3);
                            String key = roomlist.push().getKey();
                            roomlist.child(key).setValue(member);

                            Toast.makeText(OpenRoon.this, "Sent", Toast.LENGTH_SHORT).show();
                            pb.setVisibility(View.GONE);
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    String token = "/topics/"+address;

                                    FcmNotificationsSender notificationsSender =
                                            new FcmNotificationsSender(token, "Notes app",  "[" +roomname+"] "+ name+ "Sent PDF",
                                                    getApplicationContext(), OpenRoon.this);

                                    notificationsSender.SendNotifications();

                                }
                            }, 1000);



                        }
                    }
                });



            }
        });




    }

    private void openBs() {

        final Dialog dialog = new Dialog(OpenRoon.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_note_bs);

        TextView imagetv= dialog.findViewById(R.id.pick_image);
        TextView pdftv = dialog.findViewById(R.id.pick_pdf);
        TextView documenttv = dialog.findViewById(R.id.pick_document);
        TextView ppttv = dialog.findViewById(R.id.pick_PPT);


        imagetv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               Intent intent = new Intent(OpenRoon.this,ImageUploadActivity.class);
               intent.putExtra("a",address);
               intent.putExtra("n",name);
               intent.putExtra("rn",roomname);
               startActivity(intent);

                dialog.dismiss();
            }
        });


        pdftv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                // We will be redirected to choose pdf
                galleryIntent.setType("application/pdf");
                startActivityForResult(galleryIntent, 2);
                dialog.dismiss();

            }
        });

        documenttv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                // We will be redirected to choose pdf
                galleryIntent.setType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                startActivityForResult(galleryIntent, 3);
                dialog.dismiss();
            }
        });



        ppttv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                // We will be redirected to choose pdf
                galleryIntent.setType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
                startActivityForResult(galleryIntent, 4);
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomAnim;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }
}