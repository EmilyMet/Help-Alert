package com.example.helpalert;
import java.util.ArrayList;

public class User {
    private String name;
    private String email;
    private int numContacts;
    private ArrayList<Contact> contacts;

    public User(){
        contacts = new ArrayList<Contact>();
        numContacts = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail(){ return email; }

    public void setEmail(String email){this.email = email;}

}
