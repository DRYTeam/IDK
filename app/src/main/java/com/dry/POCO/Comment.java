package com.dry.POCO;

/**
 * Created by yakov on 24/04/2015.
 */
public class Comment extends BaseEntry {
    private User user;
    private String content;
    private int votesup;
    private boolean didithelp;

    public Comment(){}

    public void setUser(User user){
        this.user = user;
    }

    public User getUser(){
        return this.user;
    }

    public void setContent(String content){
        this.content = content;
    }

    public String getContent(){
        return this.content;
    }

    public void setVotesup(int votesup){
        this.votesup = votesup;
    }

    public int getVotesup(){
        return this.votesup;
    }

    public void setDidithelp(boolean didiphelp){
        this.didithelp = didithelp;
    }

    public boolean getDidithelp(){
        return this.didithelp;
    }
}
