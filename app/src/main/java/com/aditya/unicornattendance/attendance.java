package com.aditya.unicornattendance;

public class attendance {
    String sn;
    String name;
    String code;
    String intime;
    String outtime;
    String workhours;

    public attendance(String sn, String name, String code, String intime, String outtime, String workhours) {
        this.sn = sn;
        this.name = name;
        this.code = code;
        this.intime = intime;
        this.outtime = outtime;
        this.workhours = workhours;
    }
}
