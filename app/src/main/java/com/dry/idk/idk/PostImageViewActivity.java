package com.dry.idk.idk;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by yakov on 02/09/2015.
 */
public class PostImageViewActivity extends Activity {

    TouchImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_imageview);

        img = (TouchImageView) findViewById(R.id.postimg);
        if(GlobalVar.photoStatic != null) {
            img.setImageBitmap(GlobalVar.photoStatic);
        }
    }
}
