package com.aditya.unicornattendance;

public class attendanceInTime {
    String Projectcode;
    String in_time;
    String supervisor_code;
    double latitude, longitude;
    String address;

    public attendanceInTime(String projectcode, String in_time, String supervisor_code, double latitude, double longitude, String address) {
        Projectcode = projectcode;
        this.in_time = in_time;
        this.supervisor_code = supervisor_code;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public String getProjectcode() {
        return Projectcode;
    }

    public void setProjectcode(String projectcode) {
        Projectcode = projectcode;
    }

    public String getIn_time() {
        return in_time;
    }

    public void setIn_time(String in_time) {
        this.in_time = in_time;
    }

    public String getSupervisor_code() {
        return supervisor_code;
    }

    public void setSupervisor_code(String supervisor_code) {
        this.supervisor_code = supervisor_code;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
