package com.example.se;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class Sign_up_activity extends MainActivity {
    Button button;
    // This starts the firebase auth
    private FirebaseAuth user_signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    public void openNewActivity(){
        Intent intent = new Intent(this, Home_page.class);
        startActivity(intent);
    }
}

