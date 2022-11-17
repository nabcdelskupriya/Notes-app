package com.example.mynotes;

import android.app.Application;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewHolderName  extends RecyclerView.ViewHolder {

    TextView nametv,cattv;
    CheckBox checkBox;
    LinearLayout constraintLayout;
    LinearLayout linearLayoutcode;
    Button buttinsighn;
     Modal_room modal_room;
     

    public ViewHolderName(@NonNull View itemView) {
        super(itemView);
    }

    public void setName(Application application,String name,String category,String uid,String search){

        nametv  = itemView.findViewById(R.id.nameItem);
        cattv = itemView.findViewById(R.id.cateItem);
        checkBox = itemView.findViewById(R.id.name_checkbox);

        nametv.setText(name);
        cattv.setText(category);

    }


    public void setmembers(Application application,  String name,String cat,String status,String date){

        TextView nametv = itemView.findViewById(R.id.m_nametv);
        TextView timetv = itemView.findViewById(R.id.m_timetv);
        TextView statustv = itemView.findViewById(R.id.m_statustv);


        nametv.setText(name);
       timetv.setText("Joined on "+date);
        statustv.setText(status);

    }

    public void checkuser(String postkey,String uid,String address){

        constraintLayout = itemView.findViewById(R.id.cl_namelayout);
        DatabaseReference memberRef;
        memberRef = FirebaseDatabase.getInstance().getReference("members").child(address);

        memberRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChild(uid)){
                  constraintLayout.setVisibility(View.GONE);
                  nametv.setVisibility(View.GONE);
                  checkBox.setVisibility(View.GONE);
                  cattv.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
