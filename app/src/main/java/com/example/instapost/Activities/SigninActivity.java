package com.example.instapost.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.instapost.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SigninActivity extends AppCompatActivity {


    private TextView registerHere;
    private ProgressBar progressBar;
    private Button signInButton;
    private FirebaseAuth mAuth;
    private EditText email, password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        FirebaseApp.initializeApp(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.emailReg);
        password = findViewById(R.id.passwordReg);
        signInButton = findViewById(R.id.signin);
        progressBar = findViewById(R.id.pregressSignIn);
        progressBar.setVisibility(View.INVISIBLE);
        registerHere = findViewById(R.id.registerHere);
        registerHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(v.getContext(), RegisterActivity.class);
                startActivity(registerIntent);
            }
        });
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                signInButton.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                password.onEditorAction(EditorInfo.IME_ACTION_DONE);

                String userEmail = email.getText().toString().trim();
                String userPassword = password.getText().toString().trim();
                if(userEmail.isEmpty() || userPassword.isEmpty()){

                }
                else{
                    mAuth.signInWithEmailAndPassword(userEmail,userPassword)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        signInButton.setVisibility(View.VISIBLE);
                                        progressBar.setVisibility(View.INVISIBLE);

                                        Intent selectionIntent = new Intent(v.getContext(), HomeActivity.class);
                                        selectionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        selectionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(selectionIntent);
                                    }
                                    else{
                                        showMessage("SignIn Failed "+ task.getException().getMessage());
                                        signInButton.setVisibility(View.VISIBLE);
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                }



            }
        });
    }

    private void showMessage(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }
}
