package com.aditya.unicornattendance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
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

import com.google.android.gms.common.internal.FallbackServiceBroker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AllEmployeesPage extends AppCompatActivity {

    DataSnapshot emps = null;
    DataSnapshot leaves = null;
    DatabaseReference dbremp = FirebaseDatabase.getInstance().getReference("Employees");
    DatabaseReference dbrvac = FirebaseDatabase.getInstance().getReference("Leaves");
    TextView addempbtn, noresult;
    ImageView backbtn;
    EditText editsearch;
    KProgressHUD khud;

    LinearLayout cardcontainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_employees_page);

        getSupportActionBar().hide();

        khud=KProgressHUD.create(AllEmployeesPage.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);
        khud.show();
        getleaves();
        //getemployees();

        addempbtn = (TextView) findViewById(R.id.addemp);
        cardcontainer = (LinearLayout) findViewById(R.id.empcontainer);
        editsearch = (EditText) findViewById(R.id.editsearch);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        noresult = (TextView) findViewById(R.id.noresult);

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        addempbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddempPop();
            }
        });

        editsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String st = s.toString().toLowerCase();
                ArrayList<DataSnapshot> temp = new ArrayList<>();
                for (DataSnapshot emp : emps.getChildren()) {
                    if (emp.child("name").getValue().toString().toLowerCase().contains(st) || emp.getKey().toString().toLowerCase().contains(st)) {
                        temp.add(emp);
                    }
                }

                createSearchCards(temp);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void getleaves() {
        dbrvac.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                leaves = snapshot;
                getemployees();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void createSearchCards(ArrayList<DataSnapshot> temp) {
        cardcontainer.removeAllViews();
        if (temp.size() != 0) {
            noresult.setVisibility(View.GONE);
            for (DataSnapshot ds : temp) {
                LayoutInflater inflater = LayoutInflater.from(AllEmployeesPage.this);
                LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.employee_card, null, false);
                cardcontainer.addView(ll);

                TextView name = ll.findViewById(R.id.empname);
                name.setText(ds.child("name").getValue().toString());

                TextView code = ll.findViewById(R.id.empcode);
                code.setText(ds.getKey().toString());

                ImageView vacicon = ll.findViewById(R.id.vacicon);

                if (checkifonleave(ds.getKey().toString())) {
                    vacicon.setVisibility(View.VISIBLE);
                }

                ll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EmpPop(ds, "edit");
                    }
                });
            }
        } else {
            noresult.setVisibility(View.VISIBLE);
        }
    }

    private void showAddempPop() {
        Dialog dialog = new Dialog(AllEmployeesPage.this);
        dialog.setContentView(R.layout.addemp_pop);
        dialog.show();

        Window window = dialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        EditText empname = dialog.findViewById(R.id.empname);
        EditText empcode = dialog.findViewById(R.id.empcode);
        EditText empdesig = dialog.findViewById(R.id.empdesig);
        EditText emphour = dialog.findViewById(R.id.emphour);
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
                khud.show();
                String nm = empname.getText().toString().trim();
                String cd = empcode.getText().toString().trim();
                String des = empdesig.getText().toString().trim();
                String wh = emphour.getText().toString().trim();

                save_employee se = new save_employee(nm, des, wh);
                dbremp.child(cd).setValue(se).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        khud.dismiss();
                        dialog.dismiss();
//                        Intent intent = getIntent();
//                        finish();
//                        startActivity(intent);
                        getleaves();
                        Toast.makeText(AllEmployeesPage.this, "Saved", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void getemployees() {
        dbremp.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                emps = snapshot;
                createcards();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String gettime(int len) {
        Calendar instance = Calendar.getInstance();

        int mod = instance.get(Calendar.MINUTE) % 15;
        instance.add(Calendar.MINUTE, mod < 8 ? -mod : (15 - mod));

        String year = String.valueOf(instance.get(Calendar.YEAR));
        String month = String.valueOf(instance.get(Calendar.MONTH) + 1);
        String day = String.valueOf(instance.get(Calendar.DATE));
        String hour = String.valueOf(instance.get(Calendar.HOUR_OF_DAY));
        String minute = String.valueOf(instance.get(Calendar.MINUTE));

        if (month.length() == 1) {
            month = "0" + month;
        }
        if (day.length() == 1) {
            day = "0" + day;
        }
        if (hour.length() == 1) {
            hour = "0" + hour;
        }
        if (minute.length() == 1) {
            minute = "0" + minute;
        }

        if (len == 1) {
            return day + "/" + month + "/" + year + " " + hour + ":" + minute + ":00";
        } else {
            return year + month + day;
        }
    }

    private boolean checkifonleave(String ec) {
        boolean ch = false;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date cd = null;
        try {
            cd = dateFormat.parse(gettime(0));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (cd != null) {
            for (DataSnapshot ds : leaves.getChildren()) {
                if (ds.getKey().toString().equals(ec)) {
                    for (DataSnapshot ddd : ds.getChildren()) {
                        String fd = ddd.child("fromdate").getValue().toString();
                        String td = ddd.child("todate").getValue().toString();
                        Date fdd = null, tdd = null;
                        try {
                            fdd = dateFormat.parse(fd);

                            if (td.length() > 2) {
                                tdd = dateFormat.parse(td);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if (fdd != null && tdd != null) {
                            if ((cd.after(fdd) || cd.compareTo(fdd) == 0) && (cd.before(tdd) || cd.compareTo(tdd) == 0)) {
                                ch = true;
                            }
                        } else if (fdd != null && tdd == null) {
                            if (cd.compareTo(fdd) == 0) {
                                ch = true;
                            }
                        }
                    }
                }
            }
        }

        return ch;
    }

    private void createcards() {
        cardcontainer.removeAllViews();
        if (emps != null) {
            for (DataSnapshot ds : emps.getChildren()) {
                LayoutInflater inflater = LayoutInflater.from(AllEmployeesPage.this);
                LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.employee_card, null, false);
                cardcontainer.addView(ll);

                TextView name = ll.findViewById(R.id.empname);
                name.setText(ds.child("name").getValue().toString());

                TextView code = ll.findViewById(R.id.empcode);
                code.setText(ds.getKey().toString());

                ImageView vacicon = ll.findViewById(R.id.vacicon);

                if (checkifonleave(ds.getKey().toString())) {
                    vacicon.setVisibility(View.VISIBLE);
                }

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
        Dialog dialog = new Dialog(AllEmployeesPage.this);
        dialog.setContentView(R.layout.addemp_pop);
        dialog.show();

        Window window = dialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView label = dialog.findViewById(R.id.toplabel);
        EditText empname = dialog.findViewById(R.id.empname);
        EditText empcode = dialog.findViewById(R.id.empcode);
        EditText empdesig = dialog.findViewById(R.id.empdesig);
        EditText emphour = dialog.findViewById(R.id.emphour);

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
        empdesig.setText(emp.child("designation").getValue().toString());
        emphour.setText(emp.child("workhours").getValue().toString());

        if (type.equals("edit")) {
            label.setText("Edit Employee");
        }

        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                khud.show();
                String nm = empname.getText().toString().trim();
                String cd = empcode.getText().toString().trim();
                String des = empdesig.getText().toString().trim();
                String wh = emphour.getText().toString().trim();

                save_employee se = new save_employee(nm, des, wh);
                dbremp.child(cd).setValue(se).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        khud.dismiss();
                        dialog.dismiss();
//                        Intent intent = getIntent();
//                        finish();
//                        startActivity(intent);
                        getleaves();
                        Toast.makeText(AllEmployeesPage.this, "Saved", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}