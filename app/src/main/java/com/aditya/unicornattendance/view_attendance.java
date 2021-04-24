package com.aditya.unicornattendance;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class view_attendance extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);

        getSupportActionBar().hide();


    }
}