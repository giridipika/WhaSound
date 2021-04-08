package com.example.se;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Sign_up_activity extends Login_page {
    Button button;
    // Firebase database and user_signup information
    private DatabaseReference user_information;
    private FirebaseAuth user_signup;

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

        button = (Button) findViewById(R.id.sign_up_signup_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewActivity();
            }
        });
    }

    // Discussed in SRA; making use of the same feature as after sign up the user is navigated to the login screen
    public void openNewActivity(){
        Intent intent = new Intent(this, Login_page.class);
        startActivity(intent);
    }
}

