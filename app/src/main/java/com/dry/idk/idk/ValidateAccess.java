package com.dry.idk.idk;

public class ValidateAccess {
    public static boolean CheckValue(String entry,String regex){
        return !IsEmpty(entry) && entry.matches(regex);
    }

    public static boolean CheckLength(String entry,int minLength,int maxLength){
        return !IsEmpty(entry) && entry.length() >= minLength && entry.length() <= maxLength;
    }

    public static boolean IsEmpty(String entry){
        return entry.length() == 0;
    }
}
