package com.example.mynotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class Register extends AppCompatActivity {

    private EditText register_email_field;
    private EditText register_pass_field;
    private EditText register_confir_pass_field;
    private FirebaseAuth mAuth;
    ImageView ivgoogle;
    private GoogleSignInClient mGoogleSignInClient;
    private  final  static int RC_SIGN_IN  = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);

        register_email_field = findViewById(R.id.email_et_su);
        register_pass_field = findViewById(R.id.pass_et_su);
        ivgoogle = findViewById(R.id.iv_google);
        register_confir_pass_field = findViewById(R.id.confirmpass_et_su);

        openAccounts();
        mAuth = FirebaseAuth.getInstance();

        ivgoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                signIn();
            }
        });

    }

    public void SignupUser(View view) {

        String email = register_email_field.getText().toString();
        String pass = register_pass_field.getText().toString();
        String confirm_pass = register_confir_pass_field.getText().toString();

        if (!TextUtils.isEmpty(email) || !TextUtils.isEmpty(pass) || !TextUtils.isEmpty(confirm_pass)) {


            if (pass.equals(confirm_pass)) {

             //   progressBar.setVisibility(View.VISIBLE);

                mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sendtoMain();
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(getApplicationContext(), "Error :" + error, Toast.LENGTH_LONG).show();
                        }

//                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "Confirm password and password field doesn't Match", Toast.LENGTH_LONG).show();

            }


        }


    }

    public void SigntoLogin(View view) {

        Intent intent = new Intent(Register.this, Login.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            sendtoMain();

        }

    }

    private void sendtoMain(){

        Intent intent = new Intent(Register.this,Splashscreen.class);
        startActivity(intent);
        finish();

    }

    private void openAccounts(){

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "error 1", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();

                            //  updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            //   Log.w(TAG, "signInWithCredential:failure", task.getException());
                            //  updateUI(null);
                            Toast.makeText(Register.this, "error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}