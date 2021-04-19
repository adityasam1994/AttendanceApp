package com.aditya.unicornattendance;

public class vacation_save {

    String type;
    String fromdate;
    String todate;

    public vacation_save(String type, String fromdate, String todate) {
        this.type = type;
        this.fromdate = fromdate;
        this.todate = todate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFromdate() {
        return fromdate;
    }

    public void setFromdate(String fromdate) {
        this.fromdate = fromdate;
    }

    public String getTodate() {
        return todate;
    }

    public void setTodate(String todate) {
        this.todate = todate;
    }
}
