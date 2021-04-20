package com.aditya.unicornattendance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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

public class vacations extends AppCompatActivity {

    DataSnapshot emps = null;
    DatabaseReference dbremp = FirebaseDatabase.getInstance().getReference("Employees");
    DatabaseReference dbrleave = FirebaseDatabase.getInstance().getReference("Leaves");
    TextView addempbtn, noresult;
    ImageView backbtn;
    EditText editsearch;
    String datename = "oneday";

    DataSnapshot allleaves = null;
    KProgressHUD khud;

    Calendar c;
    DatePickerDialog datePickerDialog;

    LinearLayout cardcontainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacations);

        getSupportActionBar().hide();

        khud=KProgressHUD.create(vacations.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);

        khud.show();

        getleaves();

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
        dbrleave.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allleaves = snapshot;
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
                LayoutInflater inflater = LayoutInflater.from(vacations.this);
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
                        EmpPop(ds);
                    }
                });
            }
        } else {
            noresult.setVisibility(View.VISIBLE);
        }
    }

    private void showAddempPop() {
        Dialog dialog = new Dialog(vacations.this);
        dialog.setContentView(R.layout.addemp_pop);
        dialog.show();

        Window window = dialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        EditText empname = dialog.findViewById(R.id.empname);
        EditText empcode = dialog.findViewById(R.id.empcode);
        EditText empdesig = dialog.findViewById(R.id.empdesig);
        EditText emphour = dialog.findViewById(R.id.emphour);

        TextView savebtn = dialog.findViewById(R.id.save);

        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nm = empname.getText().toString().trim();
                String cd = empcode.getText().toString().trim();
                String des = empdesig.getText().toString().trim();
                String wh = emphour.getText().toString().trim();

                save_employee se = new save_employee(nm, des, wh);
                dbremp.child(cd).setValue(se).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dialog.dismiss();
//                        Intent intent = getIntent();
//                        finish();
//                        startActivity(intent);
                        getleaves();
                        Toast.makeText(vacations.this, "Saved", Toast.LENGTH_SHORT).show();
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
            for (DataSnapshot ds : allleaves.getChildren()) {
                if (ds.getKey().toString().equals(ec)) {
                    for (DataSnapshot ddd : ds.getChildren()) {
                        String fd = ddd.child("fromdate").getValue().toString();
                        String td = ddd.child("todate").getKey().toString();
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
                LayoutInflater inflater = LayoutInflater.from(vacations.this);
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
                        optionpop(ds);
                        //EmpPop(ds);
                    }
                });
            }
        }

        khud.dismiss();
    }

    private void optionpop(DataSnapshot emp){
        Dialog dialog = new Dialog(vacations.this);
        dialog.setContentView(R.layout.vacation_pop);
        dialog.show();

        Window window = dialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        final String[] op = {"op"};

        TextView label = dialog.findViewById(R.id.label);
        TextView empname = dialog.findViewById(R.id.empname);
        TextView onedate = dialog.findViewById(R.id.onedate);
        TextView fromdate = dialog.findViewById(R.id.fromdate);
        TextView todate = dialog.findViewById(R.id.todate);
        TextView onesave = dialog.findViewById(R.id.onesave);
        TextView vacsave = dialog.findViewById(R.id.vacsave);
        ImageView close = dialog.findViewById(R.id.close);

        LinearLayout addlayout = dialog.findViewById(R.id.addlayout);
        LinearLayout optionlayout = dialog.findViewById(R.id.btnlayout);
        ScrollView leavelist = dialog.findViewById(R.id.leavelist);

        TextView history = dialog.findViewById(R.id.history);
        TextView addleave = dialog.findViewById(R.id.addleave);

        LinearLayout leavelayout = dialog.findViewById(R.id.leavelayout);
        LinearLayout vacationlayout = dialog.findViewById(R.id.vacationlayout);

        TextView leave = dialog.findViewById(R.id.leave);
        TextView vacation = dialog.findViewById(R.id.vacation);

        LinearLayout leavelinear = dialog.findViewById(R.id.leavelinear);

        empname.setText(emp.child("name").getValue().toString() + " : " + emp.getKey().toString());

        for(DataSnapshot lv:allleaves.getChildren()){
            if(lv.getKey().toString().equals(emp.getKey().toString())){
                int cc=0;
                leavelinear.removeAllViews();
                for(DataSnapshot l:lv.getChildren()){
                    LayoutInflater inflater = LayoutInflater.from(vacation.getContext());
                    LinearLayout ll;
                    ll = (LinearLayout) inflater.inflate(R.layout.leavelist_element, null, false);

                    leavelinear.addView(ll);

                    TextView count = ll.findViewById(R.id.count);
                    TextView type = ll.findViewById(R.id.type);
                    TextView dates = ll.findViewById(R.id.dates);

                    cc++;
                    String tp=l.child("type").getValue().toString();
                    count.setText(cc+":");
                    if(tp.equals("leave")) {
                        type.setText("Leave");
                        dates.setText(formatdate(l.child("fromdate").getValue().toString()));
                    }else if(tp.equals("vacation")){
                        type.setText("Vacation");
                        dates.setText(formatdate(l.child("fromdate").getValue().toString())+" - "+formatdate(l.child("todate").getValue().toString()));
                    }
                }
            }
        }

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                op[0] ="his";
                optionlayout.setVisibility(View.GONE);
                addlayout.setVisibility(View.GONE);
                leavelist.setVisibility(View.VISIBLE);
            }
        });
        addleave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                op[0] = "add";
                optionlayout.setVisibility(View.GONE);
                addlayout.setVisibility(View.VISIBLE);
                leavelist.setVisibility(View.GONE);
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(op[0].equals("op")) {
                    dialog.dismiss();
                }else{
                    op[0] = "op";
                    optionlayout.setVisibility(View.VISIBLE);
                    addlayout.setVisibility(View.GONE);
                    leavelist.setVisibility(View.GONE);
                }
            }
        });

        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leave.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_bg_active));
                vacation.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_bg_inactive));
                label.setText("Leave");
                leavelayout.setVisibility(View.VISIBLE);
                vacationlayout.setVisibility(View.GONE);
            }
        });

        vacation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leave.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_bg_inactive));
                vacation.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_bg_active));
                label.setText("Vacation");
                leavelayout.setVisibility(View.GONE);
                vacationlayout.setVisibility(View.VISIBLE);
            }
        });

        onedate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int day = c.get(Calendar.DAY_OF_MONTH);
                int month = c.get(Calendar.MONTH);

                datePickerDialog = new DatePickerDialog(vacation.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int myear, int mmonth, int mdayOfMonth) {
                        String m = String.valueOf(month + 1);
                        if (m.length() == 1) {
                            m = "0" + m;
                        }
                        onedate.setText(mdayOfMonth + "/" + m + "/" + myear);
                    }
                }, year, month, day);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });

        fromdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int day = c.get(Calendar.DAY_OF_MONTH);
                int month = c.get(Calendar.MONTH);

                datePickerDialog = new DatePickerDialog(vacation.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int myear, int mmonth, int mdayOfMonth) {
                        String m = String.valueOf(month + 1);
                        if (m.length() == 1) {
                            m = "0" + m;
                        }
                        fromdate.setText(mdayOfMonth + "/" + m + "/" + myear);
                    }
                }, year, month, day);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });

        todate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int day = c.get(Calendar.DAY_OF_MONTH);
                int month = c.get(Calendar.MONTH);

                datePickerDialog = new DatePickerDialog(vacation.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int myear, int mmonth, int mdayOfMonth) {
                        String m = String.valueOf(month + 1);
                        if (m.length() == 1) {
                            m = "0" + m;
                        }
                        todate.setText(mdayOfMonth + "/" + m + "/" + myear);
                    }
                }, year, month, day);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });

        onesave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveleave(onedate.getText().toString(), "","leave", emp.getKey().toString(), dialog);
            }
        });

        vacsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveleave(fromdate.getText().toString(), todate.getText().toString(),"vacation", emp.getKey().toString(), dialog);
            }
        });
    }

    private String formatdate(String fromdate) {
        return fromdate.substring(6,8)+"/"+fromdate.substring(4,6)+"/"+fromdate.substring(0,4);
    }

    private void EmpPop(DataSnapshot emp) {
        Dialog dialog = new Dialog(vacations.this);
        dialog.setContentView(R.layout.vacation_pop);
        dialog.show();

        Window window = dialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView label = dialog.findViewById(R.id.label);
        TextView empname = dialog.findViewById(R.id.empname);
        TextView onedate = dialog.findViewById(R.id.onedate);
        TextView fromdate = dialog.findViewById(R.id.fromdate);
        TextView todate = dialog.findViewById(R.id.todate);
        TextView onesave = dialog.findViewById(R.id.onesave);
        TextView vacsave = dialog.findViewById(R.id.vacsave);
        ImageView close = dialog.findViewById(R.id.close);

        LinearLayout leavelayout = dialog.findViewById(R.id.leavelayout);
        LinearLayout vacationlayout = dialog.findViewById(R.id.vacationlayout);

        TextView leave = dialog.findViewById(R.id.leave);
        TextView vacation = dialog.findViewById(R.id.vacation);

        empname.setText(emp.child("name").getValue().toString() + " : " + emp.getKey().toString());

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leave.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_bg_active));
                vacation.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_bg_inactive));
                label.setText("Leave");
                leavelayout.setVisibility(View.VISIBLE);
                vacationlayout.setVisibility(View.GONE);
            }
        });

        vacation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leave.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_bg_inactive));
                vacation.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_bg_active));
                label.setText("Vacation");
                leavelayout.setVisibility(View.GONE);
                vacationlayout.setVisibility(View.VISIBLE);
            }
        });

        onedate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int day = c.get(Calendar.DAY_OF_MONTH);
                int month = c.get(Calendar.MONTH);

                datePickerDialog = new DatePickerDialog(vacation.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int myear, int mmonth, int mdayOfMonth) {
                        String m = String.valueOf(month + 1);
                        if (m.length() == 1) {
                            m = "0" + m;
                        }
                        onedate.setText(mdayOfMonth + "/" + m + "/" + myear);
                    }
                }, year, month, day);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });

        fromdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int day = c.get(Calendar.DAY_OF_MONTH);
                int month = c.get(Calendar.MONTH);

                datePickerDialog = new DatePickerDialog(vacation.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int myear, int mmonth, int mdayOfMonth) {
                        String m = String.valueOf(month + 1);
                        if (m.length() == 1) {
                            m = "0" + m;
                        }
                        fromdate.setText(mdayOfMonth + "/" + m + "/" + myear);
                    }
                }, year, month, day);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });

        todate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int day = c.get(Calendar.DAY_OF_MONTH);
                int month = c.get(Calendar.MONTH);

                datePickerDialog = new DatePickerDialog(vacation.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int myear, int mmonth, int mdayOfMonth) {
                        String m = String.valueOf(month + 1);
                        if (m.length() == 1) {
                            m = "0" + m;
                        }
                        todate.setText(mdayOfMonth + "/" + m + "/" + myear);
                    }
                }, year, month, day);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });

        onesave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                khud.show();
                saveleave(onedate.getText().toString(), "","leave", emp.getKey().toString(), dialog);
            }
        });

        vacsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                khud.show();
                saveleave(fromdate.getText().toString(), todate.getText().toString(),"vacation", emp.getKey().toString(), dialog);
            }
        });
    }

    private void saveleave(String fromdate, String todate, String type, String ec, Dialog dialog) {
        String[] sp = fromdate.split("/");
        String fd = sp[2]+sp[1]+sp[0];

        String td="";
        if(!todate.equals("")) {
            String[] sp1 = todate.split("/");
            td = sp1[2] + sp1[1] + sp1[0];
        }

        vacation_save vs = new vacation_save(type, fd, td);

        String fc = "1";

        if(allleaves.getChildrenCount() > 0) {
            if (allleaves.hasChild(ec)) {
                int cc = (int) allleaves.child(ec).getChildrenCount() + 1;
                fc = String.valueOf(cc);
            }
        }

        dbrleave.child(ec).child(fc).setValue(vs).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                khud.dismiss();
                dialog.dismiss();
                Toast.makeText(vacations.this, "Saved", Toast.LENGTH_SHORT).show();
//                Intent intent = getIntent();
//                finish();
//                startActivity(intent);
                getleaves();
            }
        });
    }

}