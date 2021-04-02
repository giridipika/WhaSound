package com.example.se;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;


public class Home_page extends Sign_up_activity {
    // Sign_up activity extends MainActivity this should be resolved
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);
    }
}
