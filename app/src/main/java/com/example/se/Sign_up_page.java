package com.example.se;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// User has access to back button on this page

public class Sign_up_page extends Login_page {
    private Button cancel_sign_up;
    private Button sign_up_button;
    // Firebase database and user_signup information
    private DatabaseReference user_information;
    private FirebaseAuth user_signup;
    private EditText user_email, user_password, user_name, user_id_no, user_phone;
    private String email,password, name,id,phone;
    private Boolean check_condition = Boolean.FALSE; // False by default

    public void createAccount(String email, String password, String name, String id,String phone){
        user_signup.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    check_condition = Boolean.TRUE;
                    Toast.makeText(Sign_up_page.this,"Sign up successful !",Toast.LENGTH_LONG).show();
                    // To add the user on database
                    user_information = FirebaseDatabase.getInstance().getReference();
                    userDetails new_user = new userDetails(email,password,name,id,phone);
                    // We will have access to email everywhere in  the app due to login screen.
                    user_information.child("users").child(id).setValue(new_user);
                    // After everything login page opened; verifies sign in otherwise display toast
                    openLoginPage();
                }
                else{
                    check_condition = Boolean.FALSE;
                    // Will allow the user to retry will occur if account already exists
                    Toast.makeText(Sign_up_page.this,"Sign up failed, try again !",Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        user_signup = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
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
                Boolean create_account_voo = true;

                user_email = (EditText) findViewById(R.id.signup_email);
                email = user_email.getText().toString();
                if (email.length() == 0){
                    create_account_voo = false;
                    Toast.makeText(Sign_up_page.this,"User email cannot be Null.\n Try again !",Toast.LENGTH_SHORT).show();
                }
                if(!isValid(email)){
                    create_account_voo = false;
                    Toast.makeText(Sign_up_page.this,"Wrong email format. Try again !",Toast.LENGTH_SHORT).show();
                }
                user_password = (EditText) findViewById(R.id.signup_password);
                password = user_password.getText().toString(); // Can be anything
                if (password.length() == 0){
                    create_account_voo = false;
                    Toast.makeText(Sign_up_page.this,"Password cannot be Null.\n Try again !",Toast.LENGTH_SHORT).show();
                }
                user_name = (EditText) findViewById(R.id.signup_name);
                name = user_name.getText().toString(); // Can be anything
                if (name.length() == 0){
                    create_account_voo = false;
                    Toast.makeText(Sign_up_page.this,"User Name cannot be Null.\n Try again !",Toast.LENGTH_SHORT).show();
                }
                user_id_no = (EditText) findViewById(R.id.signup_id_no);
                id = user_id_no.getText().toString(); // Can be anything (Student Id is already unique), will replace if existing
                if (id.length() == 0){
                    create_account_voo = false;
                    Toast.makeText(Sign_up_page.this,"ID cannot be Null.\n Try again !",Toast.LENGTH_SHORT).show();
                }
                user_phone = (EditText) findViewById(R.id.signup_phone);
                phone = user_phone.getText().toString();
                if (phone.length() == 0){
                    create_account_voo = false;
                    Toast.makeText(Sign_up_page.this,"Phone number cannot be Null.\n Try again !",Toast.LENGTH_SHORT).show();
                }
                if(!isPhoneNumberCorrect(phone)){
                    create_account_voo = false;
                    Toast.makeText(Sign_up_page.this,"Wrong phone number format. \n Make sure you have +1 in the beginning and the number is 10 digits long. \n Try again !",Toast.LENGTH_SHORT).show();
                }

                if (create_account_voo){
                    createAccount(email,password,name,id,phone);
                }
            }
        });
    }
    // Checks if phone number is correct or not
    private boolean isPhoneNumberCorrect(String pPhoneNumber) {

        Pattern pattern = Pattern
                .compile("((\\+[1-9]{3,4}|0[1-9]{4}|00[1-9]{3})\\-?)?\\d{8,20}");
        Matcher matcher = pattern.matcher(pPhoneNumber);

        if (matcher.matches()) return true;


        return false;
    }
    // Checks if the email is valid or not
    public static boolean isValid(String email)
    {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }
    // Discussed in SRA; making use of the same feature as after sign up the user is navigated to the login screen
    public void openLoginPage(){
        Intent intent = new Intent(this, Login_page.class);
        startActivity(intent);
    }
}

// For firebase creating an object
class userDetails{
    public String user_email, user_password, user_name, user_id_no, user_phone;
    public userDetails(){}; // Default constructor
    public  userDetails(String email,String password,String name,String id,String phone){
        this.user_email = email;
        this.user_password = password;
        this.user_name = name;
        this.user_id_no = id;
        this.user_phone = phone;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public String getUser_password() {
        return user_password;
    }

    public void setUser_password(String user_password) {
        this.user_password = user_password;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_id_no() {
        return user_id_no;
    }

    public void setUser_id_no(String user_id_no) {
        this.user_id_no = user_id_no;
    }

    public String getUser_phone() {
        return user_phone;
    }

    public void setUser_phone(String user_phone) {
        this.user_phone = user_phone;
    }
}