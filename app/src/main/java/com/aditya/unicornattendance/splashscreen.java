package com.aditya.unicornattendance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class splashscreen extends AppCompatActivity {

    DatabaseReference dbruser = FirebaseDatabase.getInstance().getReference("Supervisors");
    DatabaseReference dbrexec = FirebaseDatabase.getInstance().getReference("Others");
    SharedPreferences sharedPreferences;
    TextView lockedtext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        getSupportActionBar().hide();

        sharedPreferences = getSharedPreferences("user",MODE_PRIVATE);

        ImageView logo = (ImageView)findViewById(R.id.logo);
        lockedtext = (TextView)findViewById(R.id.locked);

        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);
        logo.startAnimation(animation);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                checkexec();
            }
        }, 2000);

    }

    private void checkexec() {
        dbrexec.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String ex = snapshot.child("exec").getValue().toString();
                if(ex.equals("1")){
                    checkuser();
                }else {
                    lockedtext.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkuser() {
        dbruser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (sharedPreferences.contains("username")){
                    String unm = sharedPreferences.getString("username","");
                    if(!unm.equals("")) {
                        if(unm.equals("admin")){
                            startActivity(new Intent(splashscreen.this, admin_page.class));
                        }else {
                            startActivity(new Intent(splashscreen.this, MainActivity.class));
                        }
                    }else {
                        startActivity(new Intent(splashscreen.this, loginpage.class));
                    }
                }else {
                    startActivity(new Intent(splashscreen.this, loginpage.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}