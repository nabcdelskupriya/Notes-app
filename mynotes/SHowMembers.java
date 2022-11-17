package com.example.mynotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class SHowMembers extends AppCompatActivity {

    LinearLayoutManager manager;
    RecyclerView recyclerView;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference nameRef,roomRef,memberRef;
    Name_modal name_modal;
    String address;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_s_how_members);

        recyclerView = findViewById(R.id.rv_members);
        name_modal = new Name_modal();

        manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            address = extras.getString("a");

        } else {

            Toast.makeText(this, "null", Toast.LENGTH_SHORT).show();

        }

        nameRef = database.getReference("members").child(address);



    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<MemberModal> options1 =
                new FirebaseRecyclerOptions.Builder<MemberModal>()
                        .setQuery(nameRef,MemberModal.class)
                        .build();

        FirebaseRecyclerAdapter<MemberModal,ViewHolderName> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<MemberModal, ViewHolderName>(options1) {
                    @Override
                    protected void onBindViewHolder(@NonNull ViewHolderName holder, int position, @NonNull MemberModal model) {

                        holder.setmembers(getApplication(),model.getName(),model.getCat(),model.getStatus(),model.getDate());

                    }


                    @NonNull
                    @Override
                    public ViewHolderName onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.members_layout,parent,false);

                        return new ViewHolderName(view);
                    }
                };
        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);

    }
}
