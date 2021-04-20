package com.aditya.unicornattendance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.util.ArrayList;

public class Manage_projects extends AppCompatActivity {

    DataSnapshot projs = null;
    DatabaseReference dbrprojects = FirebaseDatabase.getInstance().getReference("Projects");

    ArrayList<DataSnapshot> projData = new ArrayList<>();

    LinearLayout container;
    TextView addprojects, noresult;
    EditText editsearch;
    ImageView backbtn;

    KProgressHUD khud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_projects);

        getSupportActionBar().hide();

        khud=KProgressHUD.create(Manage_projects.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);

        khud.show();

        getprojects();

        container = (LinearLayout) findViewById(R.id.container);
        addprojects = (TextView) findViewById(R.id.addproj);
        editsearch = (EditText) findViewById(R.id.editsearch);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        noresult = (TextView)findViewById(R.id.noresult);

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        addprojects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddprojPop();
            }
        });

        editsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String st = editsearch.getText().toString().trim().toLowerCase();
                ArrayList<DataSnapshot> temp = new ArrayList<>();
                for (DataSnapshot emp : projData) {
                    if (emp.child("name").getValue().toString().toLowerCase().contains(st) || emp.getKey().toString().toLowerCase().contains(st)) {
                        if (emp.child("status").getValue().toString().equals("active")) {
                            temp.add(emp);
                        }
                    }

                    if (emp.child("name").getValue().toString().toLowerCase().contains(st) || emp.getKey().toString().toLowerCase().contains(st)) {
                        if (emp.child("status").getValue().toString().equals("inactive")) {
                            temp.add(emp);
                        }
                    }
                }

                createSearchCards(temp);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void createSearchCards(ArrayList<DataSnapshot> temp) {
        container.removeAllViews();
        if (temp.size() != 0) {
            noresult.setVisibility(View.GONE);
            for (DataSnapshot ds : temp) {
                LayoutInflater inflater = LayoutInflater.from(Manage_projects.this);
                LinearLayout ll;
                if (ds.child("status").getValue().toString().equals("active")) {
                    ll = (LinearLayout) inflater.inflate(R.layout.active_project_card, null, false);
                } else {
                    ll = (LinearLayout) inflater.inflate(R.layout.inactive_project_card, null, false);
                }
                container.addView(ll);

                TextView name = ll.findViewById(R.id.projname);
                name.setText(ds.child("name").getValue().toString());

                TextView code = ll.findViewById(R.id.projcode);
                code.setText(ds.getKey().toString());

                ll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EmpPop(ds, "edit");
                    }
                });
            }
        }else {
            noresult.setVisibility(View.VISIBLE);
        }
    }

    private void showAddprojPop() {
        Dialog dialog = new Dialog(Manage_projects.this);
        dialog.setContentView(R.layout.addproj_pop);
        dialog.show();

        Window window = dialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        EditText projname = dialog.findViewById(R.id.projname);
        EditText projcode = dialog.findViewById(R.id.projcode);
        EditText projstatus = dialog.findViewById(R.id.projstatus);
        EditText projpass = dialog.findViewById(R.id.projpass);

        ImageView close = dialog.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        TextView savebtn = dialog.findViewById(R.id.save);

        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nm = projname.getText().toString().trim();
                String cd = projcode.getText().toString().trim();
                String sta = projstatus.getText().toString().trim();
                String pass = projpass.getText().toString().trim();

                proj_save se = new proj_save(nm, sta, pass);
                dbrprojects.child(cd).setValue(se).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dialog.dismiss();
//                        Intent intent = getIntent();
//                        finish();
//                        startActivity(intent);
                        getprojects();
                        Toast.makeText(Manage_projects.this, "Saved", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void getprojects() {
        dbrprojects.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                projs = snapshot;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.child("status").getValue().toString().equals("active")) {
                        projData.add(ds);
                    }
                }
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.child("status").getValue().toString().equals("inactive")) {
                        projData.add(ds);
                    }
                }
                createcards();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void createcards() {
        container.removeAllViews();
        if (projData != null) {
            for (DataSnapshot ds : projData) {
                LayoutInflater inflater = LayoutInflater.from(Manage_projects.this);
                LinearLayout ll;
                if (ds.child("status").getValue().toString().equals("active")) {
                    ll = (LinearLayout) inflater.inflate(R.layout.active_project_card, null, false);
                } else {
                    ll = (LinearLayout) inflater.inflate(R.layout.inactive_project_card, null, false);
                }
                container.addView(ll);

                TextView name = ll.findViewById(R.id.projname);
                name.setText(ds.child("name").getValue().toString());

                TextView code = ll.findViewById(R.id.projcode);
                code.setText(ds.getKey().toString());

                ll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EmpPop(ds, "edit");
                    }
                });
            }
        }

        khud.dismiss();
    }

    private void EmpPop(DataSnapshot emp, String type) {
        Dialog dialog = new Dialog(Manage_projects.this);
        dialog.setContentView(R.layout.addproj_pop);
        dialog.show();

        Window window = dialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView label = dialog.findViewById(R.id.toplabel);
        EditText empname = dialog.findViewById(R.id.projname);
        EditText empcode = dialog.findViewById(R.id.projcode);
        EditText empdestatus = dialog.findViewById(R.id.projstatus);
        EditText emppass = dialog.findViewById(R.id.projpass);

        ImageView close = dialog.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        TextView savebtn = dialog.findViewById(R.id.save);

        empname.setText(emp.child("name").getValue().toString());
        empcode.setText(emp.getKey().toString());
        empdestatus.setText(emp.child("status").getValue().toString());
        emppass.setText(emp.child("password").getValue().toString());

        if (type.equals("edit")) {
            label.setText("Edit Employee");
        }

        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nm = empname.getText().toString().trim();
                String cd = empcode.getText().toString().trim();
                String des = empdestatus.getText().toString().trim();
                String pass = emppass.getText().toString().trim();

                proj_save ps = new proj_save(nm, des, pass);
                dbrprojects.child(cd).setValue(ps).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dialog.dismiss();
//                        Intent intent = getIntent();
//                        finish();
//                        startActivity(intent);
                        getprojects();
                        Toast.makeText(Manage_projects.this, "Saved", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}