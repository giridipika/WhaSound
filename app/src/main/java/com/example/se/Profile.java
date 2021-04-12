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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;
import java.util.Map;

public class Profile extends Fragment {
    Button log_out;
    private FirebaseAuth user_login;
    private DatabaseReference user_information;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile,container,false);
        // Recovering sharedPreferences email here
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.file_name),Context.MODE_PRIVATE);
        String defaultValue = "Random";
        String users_email = sharedPref.getString("Email",defaultValue); // Similar to map; email is the key and defaultValue is what it implies
        Log.i(users_email,users_email); // Logging the value of user_email

        user_login = FirebaseAuth.getInstance();
        user_information = FirebaseDatabase.getInstance().getReference();

        Query user_details = user_information.child("users").orderByChild("user_email").equalTo(users_email); // child means we have users branch; we then order by user_email and then
        // Compare to our user_email stored previously and pick the only first; the next add value listener is to display the value
        user_details.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Since we have only one value we don't iterate through the list and sign_up_page has similar class
                Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                Log.d("Tag","Value is "+ map);
                // To iterate through the map
                // n^2 sorry !! - but will run on 1 time - should work flawlessly
                Iterator hmIterator = map.entrySet().iterator();
                while (hmIterator.hasNext()){
                    Map.Entry mapElement = (Map.Entry) hmIterator.next();
                    System.out.println(mapElement.getKey() + " : " + mapElement.getValue());
                    System.out.println(" Value " + mapElement.getValue());
                    Map <String,String> database_val = (Map<String,String>) mapElement.getValue();
                    System.out.println(" Value of email:" + database_val.get("user_name"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Log.i("Value", user_details.toString());
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

