package com.example.mynotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RoomActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference nameRef,roomRef,memberRef,notifyRef;
    EditText searchEt;
    LinearLayoutManager manager;
    RecyclerView recyclerView;
    Modal_room modal_room ;
    Name_modal name_modal;
    String currentuid,roomname,time,Uname,Ucat,rname,address,adminId,usertoken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            rname = extras.getString("rn");
            address = extras.getString("a");
            adminId = extras.getString("ai");

        } else {

        }

        searchEt = findViewById(R.id.searchRoomEt);

        recyclerView = findViewById(R.id.rv_room);
        name_modal = new Name_modal();
        modal_room = new Modal_room();
        manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentuid = user.getUid();

     //  roomRef = database.getReference("Rooms").child(currentuid);//.child("members");
        memberRef = database.getReference("members").child(address);
        nameRef = database.getReference("users");
        notifyRef = database.getReference("notify");



        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }


            @Override
            public void afterTextChanged(Editable editable) {

                Search();
            }
        });


    }

    private void createMembers(String uname,String cat,String uid){

        FirebaseDatabase.getInstance().getReference("Token").child(uid).child("token")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        usertoken = snapshot.getValue(String.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        try {

            nameRef.child(adminId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists()){
                        Uname = (String) snapshot.child("name").getValue();
                        Ucat = snapshot.child("category").getKey();
                    }else {

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }catch (Exception e){

        }


        Calendar callfordate = Calendar.getInstance();
         SimpleDateFormat currentdate = new
                SimpleDateFormat("dd-MMMM-yyyy");
        final  String savedate = currentdate.format(callfordate.getTime());


        Calendar callfortime = Calendar.getInstance();
        SimpleDateFormat currenttime = new
                SimpleDateFormat("HH:mm:ss a");
        final  String savetime = currenttime.format(callfortime.getTime());

        MemberModal memberModal = new MemberModal();

        memberModal.setCat(cat);
        memberModal.setDate(" Date: "+savedate+" Time: " +savetime);
        memberModal.setStatus("Member");
        memberModal.setName(uname);

        memberRef.child(uid).setValue(memberModal);
        // adding members in room

        modal_room.setAdminid("member:"+uid);
        modal_room.setCreated(Uname);
        modal_room.setMembers("0");
        modal_room.setSearch(rname.toLowerCase());
        modal_room.setRoomname(rname);
        modal_room.setTime(time);
        modal_room.setAddress(address);

        roomRef = database.getReference("Rooms").child(uid);
        roomRef.child(address).setValue(modal_room);


        // sending notifcation users

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                FcmNotificationsSender notificationsSender =
                        new FcmNotificationsSender(usertoken, "Notes app", Uname + "Added you in Room "+ rname,
                                getApplicationContext(), RoomActivity.this);

                notificationsSender.SendNotifications();

            }
        }, 1000);

        // setting notify status


//        Map<String,Object> map = new HashMap<>();
//        map.put("members","");
//
//
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        String currentuid= user.getUid();
//        FirebaseDatabase.getInstance().getReference()
//                .child("Notes").child(currentuid)
//                .child(postkey)
//                .updateChildren(map)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//
//                        Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
//                    }
//                });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Name_modal> options1 =
                new FirebaseRecyclerOptions.Builder<Name_modal>()
                        .setQuery(nameRef,Name_modal.class)
                        .build();

        FirebaseRecyclerAdapter<Name_modal,ViewHolderName> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Name_modal, ViewHolderName>(options1) {
                    @Override
                    protected void onBindViewHolder(@NonNull ViewHolderName holder, int position, @NonNull Name_modal model) {


                        holder.setName(getApplication(),model.getName(),model.getCategory(),model.getUid(),model.getSearch());

                        String postkey = getRef(position).getKey();
                        String uname = getItem(position).getName();
                        String cat = getItem(position).getCategory();
                        String uid = getItem(position).getUid();

                        holder.checkuser(postkey,uid,address);

                        if (adminId.equals(uid)){
                            holder.checkBox.setVisibility(View.GONE);
                        }else {
                            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                    if (b){
                                        createMembers(uname,cat,uid);
                                    }else {
                                       memberRef.child(uid).removeValue();
                                        roomRef = database.getReference("Rooms").child(uid).child(address);
                                        roomRef.removeValue();
                                    }
                                }
                            });
                        }

                    }
                    @NonNull
                    @Override
                    public ViewHolderName onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.name_layout,parent,false);

                        return new ViewHolderName(view);
                    }
                };
        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);

    }

    private void Search(){
        String query = searchEt.getText().toString().toLowerCase();
        Query search = nameRef.orderByChild("search").startAt(query).endAt(query+"\uf0ff");

        FirebaseRecyclerOptions<Name_modal> options1 =
                new FirebaseRecyclerOptions.Builder<Name_modal>()
                        .setQuery(search,Name_modal.class)
                        .build();

        FirebaseRecyclerAdapter<Name_modal,ViewHolderName> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Name_modal, ViewHolderName>(options1) {
                    @Override
                    protected void onBindViewHolder(@NonNull ViewHolderName holder, int position, @NonNull Name_modal model) {

                        holder.setName(getApplication(),model.getName(),model.getCategory(),model.getUid(),model.getSearch());

                        String postkey = getRef(position).getKey();
                        String uname = getItem(position).getName();
                        String cat = getItem(position).getCategory();
                        String uid = getItem(position).getUid();

                        holder.checkuser(postkey,uid,address);

                        if (adminId.equals(uid)){
                            holder.checkBox.setVisibility(View.GONE);
                        }else {
                            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                    if (b){
                                        createMembers(uname,cat,uid);
                                    }else {
                                        memberRef.child(uid).removeValue();
                                        roomRef = database.getReference("Rooms").child(uid).child(address);
                                        roomRef.removeValue();
                                    }
                                }
                            });
                        }
                    }

                    @NonNull
                    @Override
                    public ViewHolderName onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.name_layout,parent,false);

                        return new ViewHolderName(view);
                    }
                };
        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }
}