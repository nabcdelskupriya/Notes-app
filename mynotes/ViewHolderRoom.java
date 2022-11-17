package com.example.mynotes;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewHolderRoom extends RecyclerView.ViewHolder {

    TextView roomNametv,memberTv,createdBy,jointv;
    DatabaseReference memberRef;
    int count ;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    public ViewHolderRoom(@NonNull View itemView) {
        super(itemView);
    }
    public void setRoom(FragmentActivity activity, String roomname,
                        String adminid, String created, String time, String search, String members){


        roomNametv = itemView.findViewById(R.id.roomitem);
        memberTv = itemView.findViewById(R.id.roomNoitm);
        createdBy = itemView.findViewById(R.id.roomCreated);
        jointv = itemView.findViewById(R.id.joinRoomitm);

        createdBy.setText("Created By: "+created);
        roomNametv.setText(roomname);
        memberTv.setText("Total Members: " +members);

    }




    public void setRoomforward(FragmentActivity activity, String roomname,
                        String adminid, String created, String time, String search, String members){


        roomNametv = itemView.findViewById(R.id.roomitemf);
        memberTv = itemView.findViewById(R.id.roomNoitmf);
        createdBy = itemView.findViewById(R.id.roomCreatedf);
        jointv = itemView.findViewById(R.id.send_btn_f);

        createdBy.setText("Created By: "+created);
        roomNametv.setText(roomname);
        memberTv.setText("Total Members: " +members);

    }

    public void showmembersNo(String address){


        memberRef = database.getReference("members");

        memberRef.child(address).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    count = (int) snapshot.getChildrenCount();
                    memberTv.setText("Total members: "+count);
                }else {

                    count = (int) snapshot.getChildrenCount();
                    memberTv.setText("Total members: "+count);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
