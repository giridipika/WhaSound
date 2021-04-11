package com.example.se;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Profile extends Fragment {
    Button log_out;
    private FirebaseAuth user_login;
    private DatabaseReference user_information;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile,container,false);
        user_login = FirebaseAuth.getInstance();
        user_information = FirebaseDatabase.getInstance().getReference();

        // Recovering sharedPreferences email here
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String defaultValue = "Error";
        String user_email = sharedPref.getString("ASR",defaultValue);
        Log.i(user_email,user_email);

        log_out = (Button) view.findViewById(R.id.profile_logout); // Since our view is the inflated view, when findViewById is used android is confused which view to be used; so specifying
        // and view has findViewById
        log_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginPage();
            }
        });
        return view;
    }
    public void openLoginPage(){
        Intent intent = new Intent(Profile.this.getContext(), Login_page.class); // The same logic as above, is using
        // Profile as an view and constructing that and getting the context
        startActivity(intent);
    }
}
