package com.example.se;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;


public class Home_page extends Sign_up_activity {
    // Sign_up activity extends MainActivity this should be resolved

    // Disables back button on Home page; this means that the user cannot go to login page or sign up page
    // Also cannot go back and forth in the tab structure
    @Override
    public void onBackPressed() {
        // Do nothing
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
    }
}
