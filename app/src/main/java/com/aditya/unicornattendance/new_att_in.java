package com.aditya.unicornattendance;

public class new_att_in {
    String status;
    String current_date;
    String project;
    String transits;

    public new_att_in(String status, String current_date, String project, String transits) {
        this.status = status;
        this.current_date = current_date;
        this.project = project;
        this.transits = transits;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurrent_date() {
        return current_date;
    }

    public void setCurrent_date(String current_date) {
        this.current_date = current_date;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getTransits() {
        return transits;
    }

    public void setTransits(String transits) {
        this.transits = transits;
    }
}
