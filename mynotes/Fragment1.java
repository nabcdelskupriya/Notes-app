package com.example.mynotes;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class Fragment1 extends Fragment implements View.OnClickListener{


    ImageButton sortbtn;
    FloatingActionButton fmain,fone,ftwo,fdraw;
    Float translationYaxis = 100f;
    Boolean menuOpen = false;
    EditText editText;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    OvershootInterpolator interpolator = new OvershootInterpolator();
    RecyclerView recyclerView;
    DatabaseReference reference,roomRef,roomlist,nameref;
    private Uri imageUri;
    private static final int PICK_IMAGE= 1;
    FirebaseAuth mAuth;
    String currentuid,name,category;
    RoomNoteMember member;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment1, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        editText = getActivity().findViewById(R.id.et_search);
        recyclerView = getActivity().findViewById(R.id.rv_main);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        nameref = database.getReference("users");
         currentuid= user.getUid();
        reference = database.getReference("Notes").child(currentuid);
        reference.keepSynced(true);
        sortbtn = getActivity().findViewById(R.id.filterbtn);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager( new LinearLayoutManager(getContext()));

        member = new RoomNoteMember();

        ShowMenu();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                search();
            }
        });

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
        sortbtn.setOnClickListener(this);
    }

    private void search() {

        String query = editText.getText().toString().toLowerCase();
        Query search = reference.orderByChild("search").startAt(query).endAt(query+"\uf0ff");

        FirebaseRecyclerOptions<NotesMember> options1 =
                new FirebaseRecyclerOptions.Builder<NotesMember>()
                        .setQuery(search,NotesMember.class)
                        .build();

        FirebaseRecyclerAdapter<NotesMember,NotesViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<NotesMember, NotesViewHolder>(options1) {
                    @Override
                    protected void onBindViewHolder(@NonNull NotesViewHolder holder, int position, @NonNull NotesMember model) {

                        final String  postkey = getRef(position).getKey();
                        holder.setimage(getActivity(),model.getTitle(),model.getNote(),
                                model.getTime(),model.getType(),model.getSearch(),model.getUriimage());


                        String  time = getItem(position).getTime();
                        String   title = getItem(position).getTitle();
                        String   notes = getItem(position).getNote();
                        String  url = getItem(position).getUriimage();
                        long deletetime = getItem(position).getDelete();
                        String   type = getItem(position).getType();

                        holder.imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(getActivity(),ExpandAct.class);
                                intent.putExtra("t",title);
                                intent.putExtra("n",notes);
                                intent.putExtra("u",url);

                                startActivity(intent);
                            }
                        });

                        holder.moreOptions.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Optionsheet(time,title,notes,url, deletetime, type,postkey);

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.notes_layoutimage,parent,false);

                        return new NotesViewHolder(view);
                    }
                };
        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    private void ShowMenu() {
        fmain = getActivity().findViewById(R.id.fab_main);
        fone = getActivity().findViewById(R.id.fab_one);
        ftwo = getActivity().findViewById(R.id.fab_two);
        fdraw = getActivity().findViewById(R.id.fab_draw);

        fone.setAlpha(0f);
        ftwo.setAlpha(0f);
        fdraw.setAlpha(0f);


        fone.setTranslationY(translationYaxis);
        ftwo.setTranslationY(translationYaxis);
        fdraw.setTranslationY(translationYaxis);

        fmain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (menuOpen){
                    Closemenu();
                }else {
                    OpenMenu();
                }
            }
        });

        fone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,PICK_IMAGE);
                Closemenu();
            }
        });
        ftwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), Textactivity.class);
                startActivity(intent);
                Closemenu();
            }
        });
        fdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(),DrawActivity.class);
                startActivity(intent);
                Closemenu();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {

            if (requestCode == PICK_IMAGE || resultCode == RESULT_OK ||
                    data != null || data.getData() != null){
                imageUri = data.getData();

                if (imageUri.toString().contains("image")){
                    String url = imageUri.toString();
                    String  type= "iv";
                    Intent intent = new Intent(getActivity(),ImageActivity.class);
                    intent.putExtra("u",url);
                    startActivity(intent);

                }}
        }catch (Exception e){

        }


    }

    private void OpenMenu() {


        menuOpen = ! menuOpen;
        fmain.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24);
        fone.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        ftwo.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        fdraw.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();


    }
    private void Closemenu() {

        menuOpen = ! menuOpen;
        fmain.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24);
        fone.animate().translationY(translationYaxis).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        ftwo.animate().translationY(translationYaxis).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        fdraw.animate().translationY(translationYaxis).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
    }

    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<NotesMember> options1 =
                new FirebaseRecyclerOptions.Builder<NotesMember>()
                        .setQuery(reference,NotesMember.class)
                        .build();

        FirebaseRecyclerAdapter<NotesMember,NotesViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<NotesMember, NotesViewHolder>(options1) {
                    @Override
                    protected void onBindViewHolder(@NonNull NotesViewHolder holder, int position, @NonNull NotesMember model) {

                        final String  postkey = getRef(position).getKey();

                        holder.setimage(getActivity(),model.getTitle(),model.getNote(),model.getTime(),
                                model.getType(),model.getSearch(),model.getUriimage());

                        String  time = getItem(position).getTime();
                        String  title = getItem(position).getTitle();
                        String  notes = getItem(position).getNote();
                        String  url = getItem(position).getUriimage();
                        long deletetime = getItem(position).getDelete();
                        String type = getItem(position).getType();
                        holder.imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(getActivity(),ExpandAct.class);
                                intent.putExtra("t",title);
                                intent.putExtra("n",notes);
                                intent.putExtra("u",url);

                                startActivity(intent);
                            }
                        });

                        holder.moreOptions.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Optionsheet(time,title,notes,url, deletetime, type,postkey);

                            }
                        });


                    }


                    @NonNull
                    @Override
                    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.notes_layoutimage,parent,false);

                        return new NotesViewHolder(view);
                    }
                };

        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }

    private void Optionsheet(String time, String title, String notes, String url, long deletetime, String type,String postkey) {

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.more_options);

        TextView copy = dialog.findViewById(R.id.copy_op);
        TextView share = dialog.findViewById(R.id.share_op);
        TextView download = dialog.findViewById(R.id.download_op);
        TextView delete = dialog.findViewById(R.id.delete_op);
        TextView edit = dialog.findViewById(R.id.edit_op);

        if (type.equals("t")){
            download.setVisibility(View.GONE);
            copy.setVisibility(View.GONE);
        }else if (type.equals("i")){
            download.setVisibility(View.VISIBLE);
            copy.setVisibility(View.VISIBLE);

        }

        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("String",url);
                clipboardManager.setPrimaryClip(clip);
                clip.getDescription();
                Toast.makeText(getActivity(), "Copied", Toast.LENGTH_SHORT).show();

                dialog.dismiss();

            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                String sharetext = title +"\n"+ notes +"\n"+ url;
//                Intent intent = new Intent(Intent.ACTION_SEND);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.putExtra(Intent.EXTRA_TEXT,sharetext);
//                intent.setType("text/plain");
//                startActivity(intent.createChooser(intent,"Share via"));
//
//                dialog.dismiss();


                forwardrooms(time,deletetime,url,notes,title,type);
                dialog.dismiss();


            }
        });

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PermissionListener permissionListener = new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {



                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                                DownloadManager.Request.NETWORK_MOBILE);
                        request.setTitle("Download");
                        request.setDescription("Downloading file");
                        request.allowScanningByMediaScanner();
                        ;
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title + System.currentTimeMillis() + ".jpg");

                        DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                        manager.enqueue(request);

                        Toast.makeText(getActivity(), "Downloading", Toast.LENGTH_SHORT).show();



                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                };
                TedPermission.with(getActivity())
                        .setPermissionListener(permissionListener)
                        .setPermissions(Manifest.permission.INTERNET,Manifest.permission.READ_EXTERNAL_STORAGE)
                        .check();

                dialog.dismiss();



            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                deleteNote(time,url,deletetime,type);
                dialog.dismiss();
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                showEditDialog(postkey,title,notes);
                dialog.dismiss();

            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomAnim;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    private void forwardrooms(String time, long deletetime, String url, String notes, String title, String type) {

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.forward_bottom);

        recyclerView = dialog.findViewById(R.id.rv_forward);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);


        roomRef = database.getReference("Rooms").child(currentuid);

        FirebaseRecyclerOptions<Modal_room> options1 =
                new FirebaseRecyclerOptions.Builder<Modal_room>()
                        .setQuery(roomRef,Modal_room.class)
                        .build();

        FirebaseRecyclerAdapter<Modal_room,ViewHolderRoom> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Modal_room, ViewHolderRoom>(options1) {
                    @Override
                    protected void onBindViewHolder(@NonNull ViewHolderRoom holder, int position, @NonNull Modal_room model) {

                        holder.setRoomforward(getActivity(),model.getRoomname(),model.getAdminid(),model.getCreated(),model.getTime()
                                ,model.getSearch(),model.getMembers());

                        String roomname = getItem(position).getRoomname();
                        String adminId = getItem(position).getAdminid();
                        String address = getItem(position).getAddress();

                        holder.showmembersNo(address);


                        holder.jointv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                holder.jointv.setText("Sent");
                                Calendar callfortime = Calendar.getInstance();
                                SimpleDateFormat currenttime = new
                                        SimpleDateFormat("HH:mm:ss a");
                                final String savetime = currenttime.format(callfortime.getTime());

                                roomlist = database.getReference().child("notelist").child(address);


                                member.setSendername(name);
                                member.setSenderuid(currentuid);
                                member.setType(type);
                                member.setNote(notes);
                                member.setTime(savetime);
                                member.setUrl(url);
                                member.setCode(1);

                                String key = roomlist.push().getKey();
                                roomlist.child(key).setValue(member);


                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        String token = "/topics/"+address;

                                        FcmNotificationsSender notificationsSender =
                                                new FcmNotificationsSender(token, "Notes app",  "[" +roomname+"]"+
                                                        name+ ":"+ notes, getContext(), getActivity());

                                        notificationsSender.SendNotifications();
                                        dialog.dismiss();
                                    }
                                }, 1000);
                            }
                        });

                    }

                    @NonNull
                    @Override
                    public ViewHolderRoom onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.room_layout_forward,parent,false);

                        return new ViewHolderRoom(view);
                    }
                };
        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);



        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomAnim;
        dialog.getWindow().setGravity(Gravity.BOTTOM);




    }

    private void showEditDialog(String postkey,String title,String notes) {
        LayoutInflater inflater  = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.edit_layout,null);
        EditText titleEt = view.findViewById(R.id.edit_title);
        EditText noteEt = view.findViewById(R.id.edit_note);
        Button button = view.findViewById(R.id.btn_edit);

        titleEt.setText(title);
        noteEt.setText(notes);


        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();

        alertDialog.show();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String,Object> map = new HashMap<>();
                map.put("title",titleEt.getText().toString());
                map.put("note",noteEt.getText().toString());
                map.put("search",titleEt.getText().toString().toLowerCase());

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String currentuid= user.getUid();
                FirebaseDatabase.getInstance().getReference()
                        .child("Notes").child(currentuid)
                        .child(postkey)
                        .updateChildren(map)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }

    private void showFilterdialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.sort_bottomsheet);

        TextView text_ll = dialog.findViewById(R.id.ll_text);
        TextView all_ll = dialog.findViewById(R.id.ll_all);
        TextView image_ll = dialog.findViewById(R.id.ll_image);

        text_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Query search2 = reference.orderByChild("type").equalTo("t");
                FirebaseRecyclerOptions<NotesMember> options123 =
                        new FirebaseRecyclerOptions.Builder<NotesMember>()
                                .setQuery(search2,NotesMember.class)
                                .build();

                FirebaseRecyclerAdapter<NotesMember,NotesViewHolder> firebaseRecyclerAdapter2 =
                        new FirebaseRecyclerAdapter<NotesMember, NotesViewHolder>(options123) {
                            @Override
                            protected void onBindViewHolder(@NonNull NotesViewHolder holder, int position, @NonNull NotesMember model) {

                                final String postkey = getRef(position).getKey();
                                holder.setimage(getActivity(),model.getTitle(),model.getNote(),model.getTime()
                                        ,model.getType(),model.getSearch(),model.getUriimage());


                                String time = getItem(position).getTime();
                                String title = getItem(position).getTitle();
                                String notes = getItem(position).getNote();
                                String   url = getItem(position).getUriimage();
                                long deletetime = getItem(position).getDelete();
                                String  type = getItem(position).getType();


                                holder.imageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(getActivity(),ExpandAct.class);
                                        intent.putExtra("t",title);
                                        intent.putExtra("n",notes);
                                        intent.putExtra("u",url);

                                        startActivity(intent);
                                    }
                                });
                                holder.moreOptions.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Optionsheet(time,title,notes,url,deletetime,type,postkey);

                                    }
                                });

                            }


                            @NonNull
                            @Override
                            public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                View view = LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.notes_layoutimage,parent,false);

                                return new NotesViewHolder(view);
                            }
                        };


                firebaseRecyclerAdapter2.startListening();
                recyclerView.setAdapter(firebaseRecyclerAdapter2);
                dialog.dismiss();
            }
        });

        all_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseRecyclerOptions<NotesMember> options1 =
                        new FirebaseRecyclerOptions.Builder<NotesMember>()
                                .setQuery(reference,NotesMember.class)
                                .build();

                FirebaseRecyclerAdapter<NotesMember,NotesViewHolder> firebaseRecyclerAdapter =
                        new FirebaseRecyclerAdapter<NotesMember, NotesViewHolder>(options1) {
                            @Override
                            protected void onBindViewHolder(@NonNull NotesViewHolder holder, int position, @NonNull NotesMember model) {

                                final String  postkey = getRef(position).getKey();

                                holder.setimage(getActivity(),model.getTitle(),model.getNote(),model.getTime(),
                                        model.getType(),model.getSearch(),model.getUriimage());

                                String  time = getItem(position).getTime();
                                String  title = getItem(position).getTitle();
                                String  notes = getItem(position).getNote();
                                String  url = getItem(position).getUriimage();
                                long deletetime = getItem(position).getDelete();
                                String type = getItem(position).getType();
                                holder.imageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(getActivity(),ExpandAct.class);
                                        intent.putExtra("t",title);
                                        intent.putExtra("n",notes);
                                        intent.putExtra("u",url);

                                        startActivity(intent);
                                    }
                                });

                                holder.moreOptions.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Optionsheet(time,title,notes,url, deletetime, type,postkey);

                                    }
                                });


                            }


                            @NonNull
                            @Override
                            public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                View view = LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.notes_layoutimage,parent,false);

                                return new NotesViewHolder(view);
                            }
                        };

                firebaseRecyclerAdapter.startListening();
                recyclerView.setAdapter(firebaseRecyclerAdapter);
                firebaseRecyclerAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });

        image_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                Query search = reference.orderByChild("type").equalTo("i");
                FirebaseRecyclerOptions<NotesMember> options12 =
                        new FirebaseRecyclerOptions.Builder<NotesMember>()
                                .setQuery(search,NotesMember.class)
                                .build();

                FirebaseRecyclerAdapter<NotesMember,NotesViewHolder> firebaseRecyclerAdapter12 =
                        new FirebaseRecyclerAdapter<NotesMember, NotesViewHolder>(options12) {
                            @Override
                            protected void onBindViewHolder(@NonNull NotesViewHolder holder, int position, @NonNull NotesMember model) {

                                final String  postkey = getRef(position).getKey();
                                holder.setimage(getActivity(),model.getTitle(),model.getNote(),model.getTime()
                                        ,model.getType(),model.getSearch(),model.getUriimage());


                                String   time = getItem(position).getTime();
                                String  title = getItem(position).getTitle();
                                String  notes = getItem(position).getNote();
                                String  url = getItem(position).getUriimage();
                                long deletetime = getItem(position).getDelete();
                                String  type = getItem(position).getType();

                                holder.moreOptions.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Optionsheet(time,title,notes,url, deletetime, type,postkey);

                                    }
                                });
                                holder.imageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(getActivity(),ExpandAct.class);
                                        intent.putExtra("t",title);
                                        intent.putExtra("n",notes);
                                        intent.putExtra("u",url);

                                        startActivity(intent);
                                    }
                                });

                            }


                            @NonNull
                            @Override
                            public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                View view = LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.notes_layoutimage,parent,false);

                                return new NotesViewHolder(view);
                            }
                        };


                firebaseRecyclerAdapter12.startListening();
                recyclerView.setAdapter(firebaseRecyclerAdapter12);
                dialog.dismiss();
            }
        });


        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomAnim;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void deleteNote(String time, String url, long deletetime, String type) {


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete");
        builder.setMessage("Are you Sure to Delete this data");
        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                if (type.equals("t")){

                    Query query1 = reference.orderByChild("delete").equalTo(deletetime);
                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                                dataSnapshot1.getRef().removeValue();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            ///
                        }
                    });
                }else if (type.equals("i")){

                    Query query1 = reference.orderByChild("delete").equalTo(deletetime);
                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                                dataSnapshot1.getRef().removeValue();
                            }
                            //  Toast.makeText(YourQuestions.this, "Deleted", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            ///
                        }
                    });


                    StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(url);
                    reference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Toast.makeText(getActivity(), "deleted", Toast.LENGTH_SHORT).show();
                        }
                    });
                }


            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){

                case  R.id.filterbtn:
                showFilterdialog();
                break;

        }
}
    }