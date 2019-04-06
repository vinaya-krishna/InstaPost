package com.example.instapost.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.instapost.Models.User;
import com.example.instapost.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText userName, userNickName, userEmail, userPassword;
    private Button regButton, signinButton;
    private ProgressBar loadingBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userName = findViewById(R.id.nameReg);
        userNickName = findViewById(R.id.nickNameReg);
        userEmail = findViewById(R.id.emailReg);
        userPassword = findViewById(R.id.passwordReg);
        loadingBar = findViewById(R.id.progressBar);
        loadingBar.setVisibility(View.INVISIBLE);
        regButton = findViewById(R.id.buttonReg);


        mAuth = FirebaseAuth.getInstance();

        regButton.setOnClickListener( new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                regButton.setVisibility(View.INVISIBLE);
                loadingBar.setVisibility(View.VISIBLE);
                final String name = userName.getText().toString().trim();
                final String nickName = userNickName.getText().toString().trim();
                final String email = userEmail.getText().toString().trim();
                final String password = userPassword.getText().toString().trim();

                if(name.isEmpty() || nickName.isEmpty() || email.isEmpty() || password.isEmpty()){
                    showMessage("Please Fill All Fields");
                    regButton.setVisibility(View.VISIBLE);
                    loadingBar.setVisibility(View.INVISIBLE);
                }
                else{
                    createUserAccount(name, nickName, email,password);
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() != null){
            //already login
        }
    }

    private void createUserAccount(final String name, final String nickName, final String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            showMessage("Account Created");
                            User user = new User(name, nickName, email);
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        //go to next Screen

                                        regButton.setVisibility(View.VISIBLE);
                                        loadingBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                        }
                        else{
                            showMessage("Account Creation Failed "+ task.getException().getMessage());
                            regButton.setVisibility(View.VISIBLE);
                            loadingBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }

    private void showMessage(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }
}
