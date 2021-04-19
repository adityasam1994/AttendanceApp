package com.aditya.unicornattendance;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class option_page extends AppCompatActivity {

    private static final int REQUEST_CODE_QR_SCAN = 101;
    TextView intime, transout, transin, outtime;
    private static int REQUEST_LOC_ORDER = 123;
    String project_code;
    ArrayList<DataSnapshot> projectsdata = new ArrayList<>();
    ArrayList<DataSnapshot> empdata = new ArrayList<>();
    ArrayList<DataSnapshot> supdata = new ArrayList<>();
    DataSnapshot todaysData = null;

    ImageView backbtn;


    String action = "IN";

    SharedPreferences sharedPreferences;

    DatabaseReference dbr = FirebaseDatabase.getInstance().getReference("Attendance");
    DatabaseReference dbrprojects = FirebaseDatabase.getInstance().getReference("Projects");
    DatabaseReference dbremp = FirebaseDatabase.getInstance().getReference("Employees");
    DatabaseReference dbrusers = FirebaseDatabase.getInstance().getReference("Supervisors");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option_page);
        getSupportActionBar().hide();

        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);

        checkcamera();
        getprojects();
        getemployees();
        getsups();
        gettodaysdata();

        intime = (TextView) findViewById(R.id.intime);
        transout = (TextView) findViewById(R.id.tranout);
        transin = (TextView) findViewById(R.id.transin);
        outtime = (TextView) findViewById(R.id.outtime);
        backbtn = (ImageView) findViewById(R.id.backbtn);

        project_code = getIntent().getExtras().getString("pcode");

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        intime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action = "IN";
                startscanner();
            }
        });

        transout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action = "TRANSOUT";
                startscanner();
            }
        });

        outtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action = "OUT";
                startscanner();
            }
        });

        transin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action = "TRANSIN";
                startscanner();
            }
        });
    }

    private void gettodaysdata() {
        dbr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.getKey().toString().equals(gettime(0))) {
                        todaysData = ds;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getsups() {
        dbrusers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    supdata.add(ds);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getemployees() {
        dbremp.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot d : snapshot.getChildren()) {
                    empdata.add(d);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getprojects() {
        dbrprojects.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot db : snapshot.getChildren()) {
                    projectsdata.add(db);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkcamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            String[] requestloc = new String[]{Manifest.permission.CAMERA};
            requestPermissions(requestloc, REQUEST_LOC_ORDER);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOC_ORDER) {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void startscanner() {
        IntentIntegrator intentIntegrator = new IntentIntegrator(option_page.this);
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.setPrompt("Scan the QR code");
        intentIntegrator.setBeepEnabled(true);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(this, "No code found", Toast.LENGTH_SHORT).show();
            } else {
                if (action.equals("IN")) {
                    String ec = intentResult.getContents();
                    mark_attendance(ec);
                } else if (action.equals("TRANSOUT")) {
                    String ec = intentResult.getContents();
                    mark_transout(ec);
                } else if (action.equals("TRANSIN")) {
                    String ec = intentResult.getContents();
                    mark_transin(ec);
                } else if (action.equals("OUT")) {
                    String ec = intentResult.getContents();
                    mark_out(ec);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void mark_out(String ec) {
        String tm = gettime(1);
        DataSnapshot empstatus = getstatus(ec);
        if (empstatus.hasChild("CURRENT_STATUS")) {
            String cc = empstatus.child("CURRENT_STATUS").child("status").getValue().toString();
            String project = empstatus.child("CURRENT_STATUS").child("project").getValue().toString();

            if (cc.equals("working")) {
                if (project.equals(project_code)) {
                    String lastdate = empstatus.child("CURRENT_STATUS").child("current_date").getValue().toString();

                    attendanceInTime ait = new attendanceInTime(project_code, gettime(1), sharedPreferences.getString("username", ""));

                    dbr.child(lastdate).child(ec).child("OUT").setValue(ait)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    dbremp.child(ec).child("CURRENT_STATUS").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            createdonedialog(ec, tm);
                                        }
                                    });
                                }
                            });
                }
            } else if (cc.equals("transit")) {
                String msg = "Can't transit " + getempname(ec) + " (" + ec + ") because he is under transit. First take him in";
                alertpop(msg);
            }
        } else {
            String msg = getempname(ec) + " (" + ec + ") has not signed in yet. First sign in";
            alertpop(msg);
        }

    }

    private void mark_transin(String ec) {
        String tm = gettime(1);
        DataSnapshot empstatus = getstatus(ec);

        if (empstatus.hasChild("CURRENT_STATUS")) {
            String cc = empstatus.child("CURRENT_STATUS").child("status").getValue().toString();
            String project = empstatus.child("CURRENT_STATUS").child("project").getValue().toString();

            if (cc.equals("working")) {
                String msg = getempname(ec) + " (" + ec + ") is currently working at " + getprojname_new(project) + ". First transit out from there to take him in";
                alertpop(msg);
            } else if (cc.equals("transit")) {
                int lt = Integer.parseInt(empstatus.child("CURRENT_STATUS").child("transits").getValue().toString());

                String lastdate = empstatus.child("CURRENT_STATUS").child("current_date").getValue().toString();

                attendanceInTime ait = new attendanceInTime(project_code, gettime(1), sharedPreferences.getString("username", ""));

                dbr.child(lastdate).child(ec).child("TRANSIT").child(String.valueOf(lt)).child("IN").setValue(ait)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                new_att_in nai = new new_att_in("working", lastdate, project_code, String.valueOf(lt));
                                dbremp.child(ec).child("CURRENT_STATUS").setValue(nai)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                createdonedialog(ec, tm);
                                            }
                                        });
                            }
                        });
            }
        } else {
            String msg = getempname(ec) + " (" + ec + ") has not signed in yet hence cannot be taken in";
            alertpop(msg);
        }
    }

    private void mark_transout(String ec) {
        String tm = gettime(1);
        DataSnapshot empstatus = getstatus(ec);

        if (empstatus.hasChild("CURRENT_STATUS")) {
            String cc = empstatus.child("CURRENT_STATUS").child("status").getValue().toString();
            String project = empstatus.child("CURRENT_STATUS").child("project").getValue().toString();

            if (cc.equals("working")) {
                if (project.equals(project_code)) {
                    int lt = Integer.parseInt(empstatus.child("CURRENT_STATUS").child("transits").getValue().toString()) + 1;

                    String lastdate = empstatus.child("CURRENT_STATUS").child("current_date").getValue().toString();

                    attendanceInTime ait = new attendanceInTime(project_code, gettime(1), sharedPreferences.getString("username", ""));

                    dbr.child(lastdate).child(ec).child("TRANSIT").child(String.valueOf(lt)).child("OUT").setValue(ait)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    new_att_in nai = new new_att_in("transit", lastdate, project_code, String.valueOf(lt));
                                    dbremp.child(ec).child("CURRENT_STATUS").setValue(nai)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    createdonedialog(ec, tm);
                                                }
                                            });
                                }
                            });
                } else {
                    String msg = "Can't transit out from this project because " + getempname(ec) + " (" + ec + ") signed in to " + getprojname_new(project);
                    alertpop(msg);
                }
            } else if (cc.equals("transit")) {
                String msg = getempname(ec) + " (" + ec + ") is already in transit from " + getprojname_new(project);
                alertpop(msg);
            }
        } else {
            String msg = getempname(ec) + " (" + ec + ") has not signed in. First sign in to transit out";
            alertpop(msg);
        }
    }

    private String getprojname_new(String code) {
        String nm = "";
        for (DataSnapshot ds : projectsdata) {
            if (ds.getKey().toString().equals(code)) {
                nm = ds.child("name").getValue().toString();
            }
        }

        return nm;
    }

    private void createdonedialog(String ec, String tm) {
        Dialog dialog = new Dialog(option_page.this);
        dialog.setContentView(R.layout.att_success);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
        dialog.show();

        Window window = dialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView projName = dialog.findViewById(R.id.project);
        TextView ename = dialog.findViewById(R.id.name);
        TextView wcode = dialog.findViewById(R.id.wcode);
        TextView intime = dialog.findViewById(R.id.time);
        TextView done = dialog.findViewById(R.id.done);
        TextView supname = dialog.findViewById(R.id.supname);

        projName.setText(getprojectname() + " : " + project_code);
        ename.setText(getempname(ec));
        wcode.setText(ec);
        intime.setText(tm);
        supname.setText(getsupname(sharedPreferences.getString("username", "")));

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
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

    private void mark_attendance(String ec) {
        String tm = gettime(1);
        DataSnapshot empstatus = getstatus(ec);

        if (empstatus == null) {
            alertpop("This employee doesn't exist in the database");
        } else {

            if (!empstatus.hasChild("CURRENT_STATUS")) {
                if (todaysData != null) {
                    if (todaysData.hasChild(ec)) {
                        String msg = getempname(ec) + " (" + ec + ") has already been signed out for today. Can't sign in today agian";
                        alertpop(msg);
                    } else {
                        attendanceInTime ait = new attendanceInTime(project_code, tm, sharedPreferences.getString("username", ""));
                        dbr.child(gettime(0)).child(ec).child("IN").setValue(ait)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        new_att_in nai = new new_att_in("working", gettime(0), project_code, "0");
                                        dbremp.child(ec).child("CURRENT_STATUS").setValue(nai)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        createdonedialog(ec, tm);
                                                    }
                                                });

                                    }
                                });
                    }
                } else {
                    attendanceInTime ait = new attendanceInTime(project_code, tm, sharedPreferences.getString("username", ""));
                    dbr.child(gettime(0)).child(ec).child("IN").setValue(ait)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    new_att_in nai = new new_att_in("working", gettime(0), project_code, "0");
                                    dbremp.child(ec).child("CURRENT_STATUS").setValue(nai)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    createdonedialog(ec, tm);
                                                }
                                            });

                                }
                            });
                }
            } else {
                String proj = empstatus.child("CURRENT_STATUS").child("project").getValue().toString();
                String msg = getempname(ec) + " (" + ec + ") has already signed in to " + getprojname_new(proj);
                alertpop(msg);
            }
        }
    }

    private void alertpop(String msgs) {
        Dialog dialog = new Dialog(option_page.this);
        dialog.setContentView(R.layout.alreadyin_pop);
        dialog.show();

        Window window = dialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView msg = dialog.findViewById(R.id.msg);
        TextView okay = dialog.findViewById(R.id.okay);

        msg.setText(msgs);
        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private DataSnapshot getstatus(String ec) {
        DataSnapshot result = null;

        for (DataSnapshot emp : empdata) {
            if (emp.getKey().toString().equals(ec)) {
                result = emp;
            }
        }

        return result;
    }

    private String getsupname(String username) {
        String un = "";
        for (DataSnapshot ds : supdata) {
            if (ds.getKey().toString().equals(username)) {
                un = ds.child("name").getValue().toString();
            }
        }

        return un;
    }

    private String getempname(String ec) {
        String c = "";
        for (DataSnapshot d : empdata) {
            if (d.getKey().toString().equals(ec)) {
                c = d.child("name").getValue().toString();
            }
        }

        return c;
    }

    private String getprojectname() {
        String c = "";
        for (DataSnapshot d : projectsdata) {
            if (d.getKey().toString().equals(project_code)) {
                c = d.child("name").getValue().toString();
            }
        }

        return c;
    }
}