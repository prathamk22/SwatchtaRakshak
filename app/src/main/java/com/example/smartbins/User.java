package com.example.smartbins;

public class User {
    private String name;
    private String phone;
    private String eMail;


    public User() {

    }

    public User(String Name, String phone, String eMail) {
        this.name = Name;
        this.phone = phone;
        this.eMail = eMail;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String geteMail() {
        return eMail;
    }

}
