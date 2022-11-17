package com.example.mynotes;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Splashscreen extends AppCompatActivity {

    FirebaseAuth mauth;
    ImageView imageView;
    long animationduration = 1000;
    TextView nametv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splashscreen);

        imageView =  findViewById(R.id.iv_icon);
        nametv = findViewById(R.id.tv_name);
        mauth = FirebaseAuth.getInstance();

        ObjectAnimator animatory = ObjectAnimator.ofFloat(imageView,"y",500f);
        ObjectAnimator animatorname = ObjectAnimator.ofFloat(nametv,"x",350f);
        animatory.setDuration(animationduration);
        animatorname.setDuration(animationduration);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animatory,animatorname);
        animatorSet.start();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Splashscreen.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        },1000);
    }

    }