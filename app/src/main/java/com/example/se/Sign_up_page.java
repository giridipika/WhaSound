package com.example.se;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Sign_up_page extends Login_page {
    private Button cancel_sign_up;
    private Button sign_up_button;
    // Firebase database and user_signup information
    private DatabaseReference user_information;
    private FirebaseAuth user_signup;
    private EditText user_email, user_password, user_name, user_id_no, user_phone;
    private String email,password, name,id,phone;
    private Boolean check_condition; // Need to be implemented later

    public void createAccount(String email, String password){
        user_signup.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(Sign_up_page.this,"Sign up successful !",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(Sign_up_page.this,"Sign up failed !",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user_information = FirebaseDatabase.getInstance().getReference();
        setContentView(R.layout.signup_page);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        // To allow the user to return to the login page; can also return back using the back button
        cancel_sign_up = (Button) findViewById(R.id.cancel_sign_up);
        cancel_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginPage();
            }
        });

        sign_up_button = (Button) findViewById(R.id.sign_up_signup_button);
        sign_up_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_email = (EditText) findViewById(R.id.signup_email);
                email = user_email.getText().toString();

                user_password = (EditText) findViewById(R.id.signup_password);
                password = user_password.getText().toString();

                user_name = (EditText) findViewById(R.id.signup_name);
                name = user_name.getText().toString();

                user_id_no = (EditText) findViewById(R.id.signup_id_no);
                id = user_id_no.getText().toString();

                user_phone = (EditText) findViewById(R.id.signup_phone);
                phone = user_phone.getText().toString();

                check_condition = Boolean.TRUE;

                if (check_condition){
                    // Means all the data is working and all conditions are met

                }
                else{
                    // To return back to the user
                    openLoginPage();
                }
            }
        });
    }

    // Discussed in SRA; making use of the same feature as after sign up the user is navigated to the login screen
    public void openLoginPage(){
        Intent intent = new Intent(this, Login_page.class);
        startActivity(intent);
    }
}

