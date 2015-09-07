package com.dry.POCO;

/**
 * Created by yakov on 24/04/2015.
 */
public class Subject extends BaseEntry {
    private String name;
//    private Tag[] tags;

    public Subject(){}

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

//    public void setTags(Tag[] tags){
//        this.tags = tags;
//    }
//
//    public Tag[] getTags(){
//        return this.tags;
//    }
}
