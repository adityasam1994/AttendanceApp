package com.aditya.unicornattendance;

public class save_employee {

    String name;
    String designation;
    String workhours;

    public save_employee(String name, String designation, String workhours) {
        this.name = name;
        this.designation = designation;
        this.workhours = workhours;
    }

    public String getname() {
        return name;
    }

    public void setname(String name) {
        this.name = name;
    }

    public String getdesignation() {
        return designation;
    }

    public void setdesignation(String designation) {
        this.designation = designation;
    }

    public String getworkhours() {
        return workhours;
    }

    public void setworkhours(String workhours) {
        this.workhours = workhours;
    }
}
