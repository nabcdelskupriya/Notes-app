package com.example.mynotes;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Fragment3 extends Fragment implements View.OnClickListener {


    ImageView logoutiv,editiv;
    TextView nametv,categorytv,notetv;
    FirebaseAuth mAuth;
    Name_modal modal;
    DatabaseReference userRef,roomref;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    String currentuid,name,category;
    int count ;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment3, container, false);
        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        modal = new Name_modal();

        logoutiv = getActivity().findViewById(R.id.logoutbtn);
        editiv = getActivity().findViewById(R.id.editbtn);nametv = getActivity().findViewById(R.id.nametv);
        categorytv = getActivity().findViewById(R.id.catetv);
        mAuth = FirebaseAuth.getInstance();
        notetv = getActivity().findViewById(R.id.tv_note);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentuid = user.getUid();

        userRef = database.getReference("users");
        roomref = database.getReference("Rooms");

        logoutiv.setOnClickListener(this);
        editiv.setOnClickListener(this);

        userRef.child(currentuid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                     name = (String) snapshot.child("name").getValue();
                     category = (String) snapshot.child("category").getValue();
                    notetv.setVisibility(View.GONE);

                    nametv.setText(name);
                    categorytv.setText(category);

                }else {

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(getActivity());
        if (signInAccount != null){
            name = signInAccount.getDisplayName();
            nametv.setText(name);
        }else {

        }

    }
    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.logoutbtn:

                mAuth.signOut();
                FirebaseDatabase.getInstance().getReference("Token").child(currentuid).removeValue();
                Intent intent = new Intent(getActivity(),Login.class);
                startActivity(intent);
                break;


                case R.id.editbtn:
                showBottomsheet();
                break;

        }
    }

    private void showBottomsheet() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.name_bottom);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        TextView addunametv = dialog.findViewById(R.id.addunametv);
        EditText addnameet = dialog.findViewById(R.id.nameEt);
        EditText catet = dialog.findViewById(R.id.catEt);
        Button button = dialog.findViewById(R.id.saveUname);

        addnameet.setText(name);


        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(uid)) {

                    addunametv.setText("Edit Username");
                    button.setText("Update");
                }else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        userRef.child(currentuid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    addnameet.setText(name);
                    catet.setText(category);


                }else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = addnameet.getText().toString().trim();
                String category = catet.getText().toString().trim();
                String search = addnameet.getText().toString().toLowerCase();


                modal.setSearch(search);
                modal.setUid(uid);
                modal.setCategory(category);
                modal.setName(name);


                userRef.child(uid).setValue(modal).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(), "Name Added Successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });



            }
        });


        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomAnim;
        dialog.getWindow().setGravity(Gravity.BOTTOM);


    }
}
