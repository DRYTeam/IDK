package com.dry.POCO;

/**
 * Created by yakov on 24/04/2015.
 */
public class Tag extends BaseEntry {
    private String name;

    public Tag(){}

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
