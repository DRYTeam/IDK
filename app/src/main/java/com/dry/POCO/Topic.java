package com.dry.POCO;

import android.graphics.Bitmap;
import android.util.EventLogTags;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yakov on 24/04/2015.
 */
public class Topic extends BaseEntry {
    private String title;
    private ArrayList<Tag> tags;
    private Subject subject;
    private String content;
    private ArrayList<Comment> comments;
    private int votesup;
    private int userVoteValue;
    private User username;

    private Bitmap image;


    public Topic(){tags = new ArrayList<>();
    comments = new ArrayList<>();
        image = null;}

    public void setTitle(String title){
        this.title = title;
    }

    public String getTitle(){
        return this.title;
    }

    public void setTags(ArrayList<Tag> tags){
        this.tags.removeAll(this.tags);
        for(Tag t : tags){
            if(this.tags.size() < 2) {
                this.tags.add(t);
            }
        }
    }

    public User getUsername() {
        return username;
    }

    public void setUsername(User username) {
        this.username = username;
    }

    public ArrayList<Tag> getTags(){
        return this.tags;
    }

    public void setSubject(Subject subject){
        this.subject = subject;
    }

    public Subject getSubject(){
        return this.subject;
    }

    public void setContent(String content){
        this.content = content;
    }

    public String getContent(){
        return this.content;
    }

    public void setComments(ArrayList<Comment> comments){
        for(Comment t : comments){
                this.comments.add(t);
        }
    }

    public ArrayList<Comment> getComments(){
        return this.comments;
    }

    public void setVotesup(int votesup){
        this.votesup = votesup;
    }

    public int getVotesup(){
        return this.votesup;
    }

    public int getUserVoteValue() {
        return userVoteValue;
    }

    public void setUserVoteValue(int userVoteValue) {
        this.userVoteValue = userVoteValue;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

}
