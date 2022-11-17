package com.example.mynotes;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class ExpandAct extends AppCompatActivity {

    ImageView ivexpand;
    TextView tvtitle,tvnotes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expand);

        tvnotes = findViewById(R.id.tv_exp);
        tvtitle = findViewById(R.id.tv_title_exp);
        ivexpand = findViewById(R.id.iv_exp);



        Bundle bundle = getIntent().getExtras();
        if (bundle!= null){
            String url = bundle.getString("u");
            String title = bundle.getString("t");
            String notes = bundle.getString("n");

            Picasso.get().load(url).into(ivexpand);
            tvtitle.setText(title);
            tvnotes.setText(notes);

        }else {
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }



    }
}