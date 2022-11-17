package com.example.mynotes;

import android.app.Application;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

public class NotesViewHolder extends RecyclerView.ViewHolder {

    TextView textView,tvtitle;
    ImageButton moreOptions;
    ImageView imageView;
    public NotesViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void setimage(FragmentActivity application, String title, String note, String time, String type, String search,
                         String uriimage){

         imageView = itemView.findViewById(R.id.iv_item);
        textView = itemView.findViewById(R.id.tv_note_img);
        tvtitle = itemView.findViewById(R.id.tv_title_img);
        moreOptions = itemView.findViewById(R.id.ib_options);


        if (type.equals("TXT")){
            imageView.setVisibility(View.GONE);
            textView.setText(note);
            tvtitle.setText(title);

        }else if (type.equals("IMG")){
            imageView.setVisibility(View.VISIBLE);
            textView.setText(note);
            tvtitle.setText(title);

            Picasso.get().load(uriimage).into(imageView);
        }


    }

}
