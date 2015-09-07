package com.dry.idk.idk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yakov on 02/09/2015.
 */
public class newComment extends Activity {

    Button sendButton;
    Button cancelButton;
    Button addImgButton;
    EditText txbContect;
    ImageView imgComment;

    int topicNum;
    Bitmap p;

    static final int REQUEST_IMAGE_CAPTURE=1;
    private static int RESULT_LOAD_IMG = 1;
    String imgDecodableString;
    private int decision=0;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_comment_layout);

        Intent i = getIntent();
        topicNum = i.getIntExtra("cmTopicNum",-1);

        sendButton = (Button) this.findViewById(R.id.comment_btnSend);
        cancelButton = (Button) this.findViewById(R.id.comment_btnCancel);
        addImgButton = (Button) this.findViewById(R.id.comment_btnAddimage);
        txbContect = (EditText) this.findViewById(R.id.comment_contect);
        imgComment = (ImageView) this.findViewById(R.id.comment_image);
        imgComment.setVisibility(View.GONE);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("בחר פעולה")
                .setItems(R.array.newcomment_dialog, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:
                                //new picture code
                                LaunchCamera();
                                break;
                            case 1:
                                //load picture code
                                loadImageFromGallery();
                                break;
                        }
                    }
                });
        builder.create();

        addImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.show();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpPost requestP = new HttpPost();
                    String photoStr = "";
                    //image.
                    if (p != null) {
                        photoStr = GlobalMethods.bitmapToBase64(p);
                    }

                    //request.setURI(new URI("http://drycorporations.wc.lt/newTopic.php?title=" + String.valueOf(txbTitle.getText()).replaceAll(" ", "%20") + "&subject=" + selectedSub.getNUM() + "&contect=" + String.valueOf(txbContect.getText()).replaceAll(" ", "%20") + "&user=" + GlobalVar.LoggedInUser.getNUM() + "&img=" + photoStr));
                    requestP.setURI(new URI("http://drycorporations.wc.lt/newComment.php"));

                    List<NameValuePair> nameValuePairs = new ArrayList<>();
                    nameValuePairs.add(new BasicNameValuePair("user", String.valueOf(GlobalVar.LoggedInUser.getNUM())));
                    nameValuePairs.add(new BasicNameValuePair("contect", String.valueOf(txbContect.getText())));
                    nameValuePairs.add(new BasicNameValuePair("topic", String.valueOf(topicNum)));
                    nameValuePairs.add(new BasicNameValuePair("img", photoStr));
                    requestP.setEntity(new UrlEncodedFormEntity(nameValuePairs, "utf-8"));

                    client.execute(requestP);

                    finish();

                } catch (Exception ex) {

                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private  boolean HasCamera(){
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    public void LaunchCamera(){

        if(HasCamera()) {

            decision = 1;
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "New Picture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
            imageUri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    //Load the image from the device gallery
    public void loadImageFromGallery() {
        decision=2;
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    private void updatePicture(){
        if(p != null){
            imgComment.setVisibility(View.VISIBLE);
            imgComment.setImageBitmap(p);
        } else {
            imgComment.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (decision){
            case 1:
                if(requestCode==REQUEST_IMAGE_CAPTURE && resultCode==RESULT_OK){
                    Bitmap thumbnail;
                    try {
                        thumbnail = MediaStore.Images.Media.getBitmap(
                                getContentResolver(), imageUri);
                        Bitmap photo = Bitmap.createScaledBitmap(thumbnail,thumbnail.getWidth()/4,thumbnail.getHeight()/4,false);
                        p = photo;
                        updatePicture();
                        decision=0;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 2:
                super.onActivityResult(requestCode, resultCode, data);
                try {
                    // When an Image is picked
                    if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                            && null != data) {
                        // Get the Image from data

                        Uri selectedImage = data.getData();
                        String[] filePathColumn = { MediaStore.Images.Media.DATA };

                        // Get the cursor
                        Cursor cursor = getContentResolver().query(selectedImage,
                                filePathColumn, null, null, null);
                        // Move to first row
                        cursor.moveToFirst();

                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        imgDecodableString = cursor.getString(columnIndex);
                        cursor.close();

                        //ImageView imgView = (ImageView) findViewById(R.id.myImageView);
                        // Set the Image in ImageView after decoding the String
                        //imgView.setImageBitmap(BitmapFactory
                        //        .decodeFile(imgDecodableString));

                        BitmapFactory.Options imageOpts = new BitmapFactory.Options ();
                        imageOpts.inSampleSize = 4;

                        Bitmap photo = BitmapFactory.decodeFile(imgDecodableString,imageOpts);

                        p = photo;
                        updatePicture();

                    } else {
                        Toast.makeText(this, "You haven't picked Image",
                                Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                            .show();
                    Log.d("loadImageError",e.getMessage());
                    decision=0;
                }
                break;

        }
    }
}
