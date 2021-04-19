package com.aditya.unicornattendance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class loginpage extends AppCompatActivity {

    EditText username, password;
    TextView loginbtn;
    DatabaseReference dbruser = FirebaseDatabase.getInstance().getReference("Supervisors");
    ArrayList<DataSnapshot> allusers = new ArrayList<>();
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginpage);

        getSupportActionBar().hide();

        sharedPreferences = getSharedPreferences("user",MODE_PRIVATE);
        getallusers();

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        loginbtn = (TextView)findViewById(R.id.loginbtn);

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String un = username.getText().toString().trim();
                String pw = password.getText().toString().trim();

                verify(un, pw);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }

    private void verify(String un, String pw) {
        String unm="";
        for (DataSnapshot ds:allusers){
            if(ds.child("username").getValue().toString().equals(un) && ds.child("password").getValue().toString().equals(pw)){
                unm = ds.getKey().toString();
            }
        }

        if(!unm.equals("")) {
            sharedPreferences.edit().putString("username", unm).commit();
            if(unm.equals("admin")){
                startActivity(new Intent(loginpage.this, admin_page.class));
            }else {
                startActivity(new Intent(loginpage.this, MainActivity.class));
            }
        }else {
            Toast.makeText(this, "Wrong username or password", Toast.LENGTH_SHORT).show();
        }
    }

    private void getallusers() {
        dbruser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot user:snapshot.getChildren()){
                    allusers.add(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}