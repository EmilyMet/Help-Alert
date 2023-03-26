package com.example.helpalert;
import java.util.HashMap;

public class User {
    private String name;
    private String email;
    private HashMap<String,String> contactDetails;

    public User(){
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail(){ return email; }

    public void setEmail(String email){this.email = email;}

    public void addContact(String cName, String cNumber){
        contactDetails.put(cName, cNumber);
    }

    public HashMap<String, String> getContacts(){
        return contactDetails;
    }

}
