package com.example.mynotes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

public class Fragment2 extends Fragment implements View.OnClickListener{


    TextView createTv;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference roomRef,nameRef,memberRef;
    String currentuid;
    EditText searchEt;
    ImageButton filterbtn;
    String roomname,time,Uname,Ucat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment2, container, false);
        return view;


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentuid = user.getUid();
        createTv = getActivity().findViewById(R.id.f2createroom);
        filterbtn = getActivity().findViewById(R.id.filterbtn);
        createTv.setOnClickListener(this);
        filterbtn.setOnClickListener(this);
        roomRef = database.getReference("Rooms").child(currentuid);

        recyclerView = getActivity().findViewById(R.id.rv_roomf2);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        nameRef = database.getReference("users");


        searchEt = getActivity().findViewById(R.id.et_searchf2);

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

        try {

            nameRef.child(currentuid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists()){
                        Uname = (String) snapshot.child("name").getValue();
                        Ucat = snapshot.child("category").getKey();
                    }else {
//                        Intent intent = new Intent(getActivity(),Fragment3.class);
//                        startActivity(intent);
                        Toast.makeText(getActivity(), "Add username and category", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }catch (Exception e){

            Toast.makeText(getActivity(), "Please add username ", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Modal_room> options1 =
                new FirebaseRecyclerOptions.Builder<Modal_room>()
                        .setQuery(roomRef,Modal_room.class)
                        .build();

        FirebaseRecyclerAdapter<Modal_room,ViewHolderRoom> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Modal_room, ViewHolderRoom>(options1) {
                    @Override
                    protected void onBindViewHolder(@NonNull ViewHolderRoom holder, int position, @NonNull Modal_room model) {

                        holder.setRoom(getActivity(),model.getRoomname(),model.getAdminid(),model.getCreated(),model.getTime()
                                ,model.getSearch(),model.getMembers());

                        String roomname = getItem(position).getRoomname();
                        String adminId = getItem(position).getAdminid();
                        String address = getItem(position).getAddress();

                        holder.showmembersNo(address);


                        holder.jointv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(getActivity(),OpenRoon.class);
                                intent.putExtra("rn",roomname);
                                intent.putExtra("ai",adminId);
                                intent.putExtra("a",address);
                                startActivity(intent);
                            }
                        });

                    }

                    @NonNull
                    @Override
                    public ViewHolderRoom onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.room_layout,parent,false);

                        return new ViewHolderRoom(view);
                    }
                };
        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case  R.id.f2createroom:

                showBottomsheet();
                break;

                case  R.id.filterbtn:

                    LayoutInflater inflater = LayoutInflater.from(getActivity());
                    View view2 = inflater.inflate(R.layout.filter_dialog, null);
                    TextView createdtv = view2.findViewById(R.id.createdfilterTv);
                    TextView joinedtv = view2.findViewById(R.id.joinedfilterTv);


                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                            .setView(view2)
                            .create();

                    alertDialog.show();


                    createdtv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Query search = roomRef.orderByChild("adminid").startAt(currentuid).endAt(currentuid+"\uf0ff");

                            FirebaseRecyclerOptions<Modal_room> options1 =
                                    new FirebaseRecyclerOptions.Builder<Modal_room>()
                                            .setQuery(search,Modal_room.class)
                                            .build();

                            FirebaseRecyclerAdapter<Modal_room,ViewHolderRoom> firebaseRecyclerAdapter =
                                    new FirebaseRecyclerAdapter<Modal_room, ViewHolderRoom>(options1) {
                                        @Override
                                        protected void onBindViewHolder(@NonNull ViewHolderRoom holder, int position, @NonNull Modal_room model) {

                                            holder.setRoom(getActivity(),model.getRoomname(),model.getAdminid(),model.getCreated(),model.getTime()
                                                    ,model.getSearch(),model.getMembers());

                                        }

                                        @NonNull
                                        @Override
                                        public ViewHolderRoom onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                            View view = LayoutInflater.from(parent.getContext())
                                                    .inflate(R.layout.room_layout,parent,false);

                                            return new ViewHolderRoom(view);
                                        }
                                    };
                            firebaseRecyclerAdapter.startListening();
                            recyclerView.setAdapter(firebaseRecyclerAdapter);
                            alertDialog.dismiss();

                        }
                    });


                    joinedtv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            String query  = "member :"+currentuid;
                            Query search = roomRef.orderByChild("adminid").startAt(query).endAt(query+"\uf0ff");

                            FirebaseRecyclerOptions<Modal_room> options1 =
                                    new FirebaseRecyclerOptions.Builder<Modal_room>()
                                            .setQuery(search,Modal_room.class)
                                            .build();

                            FirebaseRecyclerAdapter<Modal_room,ViewHolderRoom> firebaseRecyclerAdapter =
                                    new FirebaseRecyclerAdapter<Modal_room, ViewHolderRoom>(options1) {
                                        @Override
                                        protected void onBindViewHolder(@NonNull ViewHolderRoom holder, int position, @NonNull Modal_room model) {

                                            holder.setRoom(getActivity(),model.getRoomname(),model.getAdminid(),model.getCreated(),model.getTime()
                                                    ,model.getSearch(),model.getMembers());

                                        }

                                        @NonNull
                                        @Override
                                        public ViewHolderRoom onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                            View view = LayoutInflater.from(parent.getContext())
                                                    .inflate(R.layout.room_layout,parent,false);

                                            return new ViewHolderRoom(view);
                                        }
                                    };
                            firebaseRecyclerAdapter.startListening();
                            recyclerView.setAdapter(firebaseRecyclerAdapter);
                            alertDialog.dismiss();

                        }
                    });



                break;
        }

    }

    private void showBottomsheet() {

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.room_bottom);
        Button createBtn  = dialog.findViewById(R.id.btn_createname);
        EditText roomEt = dialog.findViewById(R.id.roomnameEt);
        Modal_room modal_room = new Modal_room();





        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Name_modal name_modal = new Name_modal();

                Calendar callfordate = Calendar.getInstance();
                SimpleDateFormat currentdate = new
                        SimpleDateFormat("dd-MMMM-yyyy");
                final  String savedate = currentdate.format(callfordate.getTime());


                Calendar callfortime = Calendar.getInstance();
                SimpleDateFormat currenttime = new
                        SimpleDateFormat("HH:mm:ss a");
                final  String savetime = currenttime.format(callfortime.getTime());


                roomname= roomEt.getText().toString().toUpperCase().trim();
                time = savedate+":"+savetime;
                 final String address = roomname+currentuid+System.currentTimeMillis();

                if (!TextUtils.isEmpty(roomname)){

                    modal_room.setAdminid(currentuid);
                    modal_room.setCreated(Uname);
                    modal_room.setMembers("0");
                    modal_room.setSearch(roomname.toLowerCase());
                    modal_room.setRoomname(roomname);
                    modal_room.setTime(time);
                    modal_room.setAddress(address);

                    memberRef = database.getReference("members");


                    String key = roomRef.push().getKey();

                    roomRef.child(address).setValue(modal_room);

                   // creating members here

                    MemberModal memberModal = new MemberModal();

                    memberModal.setCat(Ucat);
                    memberModal.setDate(savedate+" Time:"+savetime);
                    memberModal.setStatus("Admin");
                    memberModal.setName(Uname);

                    memberRef.child(address)
                            .child(currentuid).setValue(memberModal);




                    dialog.dismiss();

                    Toast.makeText(getActivity(), "Room Created Successfully", Toast.LENGTH_SHORT).show();

                }else {
                    Toast.makeText(getActivity(), "Please give a name", Toast.LENGTH_SHORT).show();
                }

            }
        });


        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomAnim;
        dialog.getWindow().setGravity(Gravity.BOTTOM);


    }

    private void Search(){
        String query = searchEt.getText().toString().toLowerCase();
        Query search = roomRef.orderByChild("search").startAt(query).endAt(query+"\uf0ff");

        FirebaseRecyclerOptions<Modal_room> options1 =
                new FirebaseRecyclerOptions.Builder<Modal_room>()
                        .setQuery(search,Modal_room.class)
                        .build();

        FirebaseRecyclerAdapter<Modal_room,ViewHolderRoom> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Modal_room, ViewHolderRoom>(options1) {
                    @Override
                    protected void onBindViewHolder(@NonNull ViewHolderRoom holder, int position, @NonNull Modal_room model) {

                        holder.setRoom(getActivity(),model.getRoomname(),model.getAdminid(),model.getCreated(),model.getTime()
                                ,model.getSearch(),model.getMembers());

                    }

                    @NonNull
                    @Override
                    public ViewHolderRoom onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.room_layout,parent,false);

                        return new ViewHolderRoom(view);
                    }
                };
        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }
}
