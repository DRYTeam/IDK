package com.dry.idk.idk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.dry.POCO.BaseEntry;
import com.dry.POCO.Subject;
import com.dry.POCO.Tag;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GlobalMethods {
    public static String convertStreamToString(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"),1024);
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                inputStream.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }

    public static Subject getSubjectFromName(String Name){
        for(Subject sub : GlobalVar.subjectsList){
            if(sub.getName().equals(Name)){
                return sub;
            }
        }
        return null;
    }

    public static BaseEntry getObjectByNum(int num, ArrayList<? extends BaseEntry> list){
        for(BaseEntry entry : list){
            if(entry.getNUM() == num){
                return entry;
            }
        }
        return null;
    }

    public static Tag getTagFromName(String Name){
        for(Tag tag : GlobalVar.tagsList){
            if(tag.getName().equals(Name)){
                return tag;
            }
        }
        return null;
    }

    public static Tag getTagFromName(String Name,ArrayList<Tag> tags){
        for(Tag tag : tags){
            if(tag.getName().equals(Name)){
                return tag;
            }
        }
        return null;
    }

    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public static Bitmap base64ToBitmap(String b64) {
        byte[] imageAsBytes = Base64.decode(b64, 0);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }

    public static String Base64EncodeUrl(String str){
        return str.replaceAll("\\+","-").replaceAll("/","_").replaceAll("=",",");
    }
}
