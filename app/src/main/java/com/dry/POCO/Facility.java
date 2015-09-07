package com.dry.POCO;

/**
 * Created by yakov on 24/04/2015.
 */
public class Facility extends BaseEntry {
    private String name;

    public Facility(){}

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    @Override
    public String toString(){
        return this.getName();
    }
}
