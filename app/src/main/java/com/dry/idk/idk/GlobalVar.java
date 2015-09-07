package com.dry.idk.idk;

import android.graphics.Bitmap;

import com.dry.POCO.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yakov on 29/07/2015.
 */
public class GlobalVar {
    final static public String userdata = "User_Data";
    static public User LoggedInUser = new User();
    static public ArrayList<Facility> facilityList = new ArrayList<Facility>();
    static public ArrayList<Subject> subjectsList = new ArrayList<Subject>();
    static public ArrayList<Tag> tagsList = new ArrayList<Tag>();

    static public Bitmap photoStatic = null;
}
