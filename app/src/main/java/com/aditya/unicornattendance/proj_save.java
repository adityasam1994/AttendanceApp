package com.aditya.unicornattendance;

public class proj_save {
    String name;
    String status;
    String password;

    public proj_save(String name, String status, String password) {
        this.name = name;
        this.status = status;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
