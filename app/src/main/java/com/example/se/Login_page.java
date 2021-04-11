package com.example.se;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// Main Activity is the login screen

public class Login_page extends AppCompatActivity {
    // This code opens new activity when Sign up is clicked
    Button sign_up_button;
    Button sign_in_button;
    private FirebaseAuth user_login;
    private EditText user_email, user_password;
    String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Firebase is initialized
        user_login = FirebaseAuth.getInstance();

        setContentView(R.layout.login_page);

        // Removes the title bar
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        sign_in_button = (Button) findViewById(R.id.login_log_in_button);
        sign_up_button = (Button) findViewById(R.id.login_signup_button);

        sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_email = (EditText) findViewById(R.id.login_email);
                user_password = (EditText) findViewById(R.id.login_password);

                email = user_email.getText().toString();
                password = user_password.getText().toString();

                Log.i(email,password);
                try{
                    signIn(email,password);
                } catch (Exception Easd){
                    Toast.makeText(Login_page.this,"Log in Failed, try again! ",Toast.LENGTH_LONG).show();
                    Log.i("Error at login","Error");
                }
            }
        });

        sign_up_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSignUpPage();
            }
        });
    }

    // Logs the user out if they are already logged in - discussed under SRA
    @Override
    public void onStart(){
        super.onStart();
        // Checks if the user is already signed in; if so takes to the home page
        FirebaseUser currentUser = user_login.getCurrentUser();
        if (currentUser != null){
            FirebaseAuth.getInstance().signOut();
        }
    }

    // This method will attempt to sign in
    private void signIn(String email, String password){
        // This means current activity
        user_login.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // Login is successful; take the user ahead to the Home Page
                if(task.isSuccessful()){
                    Toast.makeText(Login_page.this,"Welcome !",Toast.LENGTH_SHORT).show();
                    openHomePage();
                }
                else{
                    Toast.makeText(Login_page.this,"Log in Failed.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // This is to navigate to the homepage
    public void openHomePage(){
        Intent intent = new Intent(this, Homepage.class);
        startActivity(intent);
    }

    // To navigate to the Signup page
    public void openSignUpPage(){
        Intent intent = new Intent(this, Sign_up_page.class);
        startActivity(intent);
    }
}