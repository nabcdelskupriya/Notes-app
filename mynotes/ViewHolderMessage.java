package com.example.mynotes;

import android.app.Application;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ViewHolderMessage extends RecyclerView.ViewHolder {

    TextView unametv,typetv,timetv,filenametv,texttv;
    ImageView ivdownload;
    LinearLayout m_ll;

    public ViewHolderMessage(@NonNull View itemView) {
        super(itemView);
    }

    public void setmessage(Application application,  String sendername,
            String senderuid,
            String note, String time, String type, String url,int code){

        unametv = itemView.findViewById(R.id.message_uname);
        typetv = itemView.findViewById(R.id.mtype_tv);
        timetv = itemView.findViewById(R.id.mtime_tv);
        filenametv = itemView.findViewById(R.id.mfile_nametv);
        texttv = itemView.findViewById(R.id.texttv);
        ivdownload = itemView.findViewById(R.id.downloadmbtn);
        m_ll = itemView.findViewById(R.id.m_ll);

        switch (type) {
            case "TXT":
                unametv.setText(sendername);
                texttv.setText(note);
                timetv.setText(time);
                typetv.setText(type);
                m_ll.setVisibility(View.GONE);

                break;
            case "PPT":
                m_ll.setVisibility(View.VISIBLE);
                unametv.setText(sendername);
                filenametv.setText(note);
                timetv.setText(time);
                typetv.setText(type);
                texttv.setVisibility(View.GONE);

                filenametv.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_baselineppt, //left
                        0, //top
                        0, //right
                        0);//bottom

                break;
            case "PDF":
                m_ll.setVisibility(View.VISIBLE);
                unametv.setText(sendername);
                filenametv.setText(note);
                timetv.setText(time);
                typetv.setText(type);
                texttv.setVisibility(View.GONE);

                filenametv.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_baseline_pdf, //left
                        0, //top
                        0, //right
                        0);//bottom
                break;
            case "DOCX":
                m_ll.setVisibility(View.VISIBLE);
                unametv.setText(sendername);
                filenametv.setText(note);
                timetv.setText(time);
                typetv.setText(type);
                texttv.setVisibility(View.GONE);

                filenametv.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_baseline_msword, //left
                        0, //top
                        0, //right
                        0);//bottom

                break;
            case "IMG":
                m_ll.setVisibility(View.VISIBLE);
                unametv.setText(sendername);
                filenametv.setText(note);
                timetv.setText(time);
                typetv.setText(type);
                texttv.setVisibility(View.GONE);

                filenametv.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_baseline_image_24, //left
                        0, //top
                        0, //right
                        0);//bottom

                break;

        }

        }

    }
