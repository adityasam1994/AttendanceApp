package com.aditya.unicornattendance;

public class attendanceInTime {
    String Projectcode;
    String in_time;
    String supervisor_code;

    public attendanceInTime(String Projectcode, String in_time, String supervisor_code) {
        this.Projectcode = Projectcode;
        this.in_time = in_time;
        this.supervisor_code = supervisor_code;
    }

    public String getCode() {
        return Projectcode;
    }

    public void setCode(String code) {
        this.Projectcode = code;
    }

    public String getin_time() {
        return in_time;
    }

    public void setin_time(String in_time) {
        this.in_time = in_time;
    }

    public String getsupervisor_code() {
        return supervisor_code;
    }

    public void setsupervisor_code(String supervisor_code) {
        this.supervisor_code = supervisor_code;
    }
}
