package com.dry.POCO;

/**
 * Created by yakov on 24/04/2015.
 */
public class User extends BaseEntry {
    private String username;
    private String password;
    private String email;
    private Facility facility;

    public User(){}

    public void setUsername(String name){
        this.username = name;
    }

    public String getUsername(){
        return this.username;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getPassword(){
        return this.password;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getEmail(){
        return this.email;
    }

    public void setFacility(Facility facility){
        this.facility = facility;
    }

    public Facility getFacility(){
        return this.facility;
    }
}
