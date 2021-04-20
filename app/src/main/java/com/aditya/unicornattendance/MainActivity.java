package com.aditya.unicornattendance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.service.autofill.Dataset;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    LinearLayout cardcontainer;
    DatabaseReference fdb = FirebaseDatabase.getInstance().getReference("Projects");
    ArrayList<DataSnapshot> temp = new ArrayList<>();
    ImageView logout;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        sharedPreferences = getSharedPreferences("user",MODE_PRIVATE);
        cardcontainer = (LinearLayout) findViewById(R.id.cardcontainer);
        logout = (ImageView)findViewById(R.id.logout);

        retireve_projects();

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean t = sharedPreferences.edit().putString("username","").commit();
                if(t) {
                    startActivity(new Intent(MainActivity.this, loginpage.class));
                }
            }
        });
        
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }

    private void retireve_projects() {
        fdb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot db : snapshot.getChildren()) {
                    if(db.child("status").getValue().toString().equals("active")) {
                        temp.add(db);
                    }
                }
                createcards();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ask_pass(String pass, String code){
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.project_pop);
        dialog.show();

        Window window = dialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        EditText editpass = dialog.findViewById(R.id.password);
        TextView proceed = dialog.findViewById(R.id.proceed_btn);

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String passenter = editpass.getText().toString().trim();
                if(passenter.equals(pass)){
                    dialog.dismiss();
                    Intent i = new Intent(MainActivity.this, option_page.class);
                    i.putExtra("pcode", code);
                    startActivity(i);

                }else {
                    editpass.setText("");
                    Toast.makeText(MainActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void create_project_cards(DataSnapshot proj,DataSnapshot proj1, int count){
        if(count == 2) {
            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.projects_template, null, false);
            cardcontainer.addView(ll);

            TextView name = ll.findViewById(R.id.projectname1);
            name.setText(proj.child("name").getValue().toString());

            TextView code = ll.findViewById(R.id.projectcode1);
            code.setText(proj.getKey().toString());

            TextView name2 = ll.findViewById(R.id.projectname2);
            name2.setText(proj1.child("name").getValue().toString());

            TextView code2 = ll.findViewById(R.id.projectcode2);
            code2.setText(proj1.getKey().toString());

            LinearLayout ll1 = ll.findViewById(R.id.ll1);
            LinearLayout ll2 = ll.findViewById(R.id.ll2);

            ll1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ask_pass(proj.child("password").getValue().toString(), proj.getKey().toString());
                }
            });

            ll2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ask_pass(proj1.child("password").getValue().toString(), proj1.getKey().toString());
                }
            });
        }else{
            LayoutInflater inflaterz = LayoutInflater.from(MainActivity.this);
            LinearLayout llz = (LinearLayout) inflaterz.inflate(R.layout.projects_template, null, false);
            cardcontainer.addView(llz);

            TextView name = llz.findViewById(R.id.projectname1);
            name.setText(proj.child("name").getValue().toString());

            TextView code = llz.findViewById(R.id.projectcode1);
            code.setText(proj.getKey().toString());

            LinearLayout lr = llz.findViewById(R.id.ll2);
            lr.setVisibility(View.INVISIBLE);

            LinearLayout ll1 = llz.findViewById(R.id.ll1);
            ll1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ask_pass(proj.child("password").getValue().toString(), proj.getKey().toString());
                }
            });
        }
    }

    private void createcards() {
        cardcontainer.removeAllViews();
        if (temp.size() % 2 == 0) {
            for (int i = 0; i < temp.size() / 2; i++) {
                DataSnapshot proj = temp.get(2 * i);
                DataSnapshot proj1 = temp.get(2 * i + 1);

                create_project_cards(proj, proj1, 2);
            }
        }else {
            for (int i = 0; i < (temp.size()-1) / 2; i++) {
                DataSnapshot proj = temp.get(2 * i);
                DataSnapshot proj1 = temp.get(2 * i + 1);

                create_project_cards(proj, proj1, 2);

            }

            DataSnapshot projz = temp.get(temp.size()-1);
            create_project_cards(projz, projz,1);
        }

    }
}