package com.dry.idk.idk;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Image;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.EventLogTags;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dry.POCO.Facility;
import com.dry.POCO.Subject;
import com.dry.POCO.Tag;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yakov on 12/08/2015.
 */
enum Mode{Text,Image}

public class newTextPostActivity extends Activity {

    Button sendButton;
    Button cancelButton;
    Button btnAddNewTag;
    EditText txbTitle;
    EditText txbContect;
    ArrayList<Tag> selectedTags;
    LayoutInflater inflater;
    LinearLayout tagsLayout;
    AutoCompleteTextView txbTags;
    ImageView imgView;
    Mode postMode;
    Bitmap photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newtextpost_layout);

        Bundle extras = getIntent().getExtras();
        postMode = (Mode) extras.getSerializable("Mode");
        photo = null;

        selectedTags = new ArrayList<Tag>();
        inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        tagsLayout = (LinearLayout) this.findViewById(R.id.TagsLayout);

        //*not recommended* - fix NetworkCrashed by overriding the android regular behaviour
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //*not recommended*

        sendButton = (Button) this.findViewById(R.id.newTextPost_btnAccept);
        cancelButton = (Button) this.findViewById(R.id.newTextPost_btnCancel);
        btnAddNewTag = (Button) this.findViewById(R.id.newTextPost_btnNewTag);
        txbTitle = (EditText) this.findViewById(R.id.newTextPost_txbTitle);
        txbContect = (EditText) this.findViewById(R.id.newTextPost_txbContext);
        imgView = (ImageView) this.findViewById(R.id.newTextPost_imgView);

        if(postMode == Mode.Image) {
            //photo = (Bitmap) extras.getParcelable("Picture");
            //photo = GlobalMethods.base64ToBitmap(extras.getString("Picture"));
            photo = GlobalVar.photoStatic;
            imgView.setImageBitmap(photo);
        } else if(postMode == Mode.Text){
            imgView.setVisibility(View.GONE);
        }

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final AutoCompleteTextView txbSubject = (AutoCompleteTextView) findViewById(R.id.newTextPost_txbSubject);
        ArrayAdapter Subadapter =
                new ArrayAdapter(this, android.R.layout.simple_list_item_1, GlobalVar.subjectsList);
        txbSubject.setThreshold(1);
        txbSubject.setAdapter(Subadapter);

        txbTags = (AutoCompleteTextView) findViewById(R.id.newTextPost_txbTags);
        ArrayAdapter TagAdapter =
                new ArrayAdapter(this, android.R.layout.simple_list_item_1, GlobalVar.tagsList);
        txbTags.setThreshold(1);
        txbTags.setAdapter(TagAdapter);

        txbTags.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(selectedTags.size() < 2){
                    selectedTags.add(GlobalMethods.getTagFromName(String.valueOf(txbTags.getText())));
                    updateTagsView();
                    txbTags.setText("");
                    txbTags.setEnabled(selectedTags.size() < 2);
                }
            }
        });

        btnAddNewTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!txbTags.getText().toString().trim().equals("") && selectedTags.size() < 2) {
                    if (GlobalMethods.getTagFromName(String.valueOf(txbTags.getText())) == null) {
                        Tag newTag = new Tag();
                        newTag.setName(txbTags.getText().toString());
                        selectedTags.add(newTag);
                        updateTagsView();
                        txbTags.setText("");
                        txbTags.setEnabled(selectedTags.size() < 2);
                    } else {
                        selectedTags.add(GlobalMethods.getTagFromName(String.valueOf(txbTags.getText())));
                        updateTagsView();
                        txbTags.setText("");
                        txbTags.setEnabled(selectedTags.size() < 2);
                    }
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Subject selectedSub = GlobalMethods.getSubjectFromName(String.valueOf(txbSubject.getText()));
                if(selectedSub == null){
                    //error
                }
                HttpResponse response = null;
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet();
                    HttpPost requestP = new HttpPost();
                    String photoStr="";
                    if(postMode == Mode.Text){
                        photoStr = "";
                    } else if(postMode == Mode.Image) {
                        photoStr = GlobalMethods.bitmapToBase64(photo);
                    }

                    Log.d("photoStr", photoStr);

                    //request.setURI(new URI("http://drycorporations.wc.lt/newTopic.php?title=" + String.valueOf(txbTitle.getText()).replaceAll(" ", "%20") + "&subject=" + selectedSub.getNUM() + "&contect=" + String.valueOf(txbContect.getText()).replaceAll(" ", "%20") + "&user=" + GlobalVar.LoggedInUser.getNUM() + "&img=" + photoStr));
                    requestP.setURI(new URI("http://drycorporations.wc.lt/newTopic.php"));

                    List<NameValuePair> nameValuePairs = new ArrayList<>();
                    nameValuePairs.add(new BasicNameValuePair("title", String.valueOf(txbTitle.getText())));
                    nameValuePairs.add(new BasicNameValuePair("subject", String.valueOf(selectedSub.getNUM())));
                    nameValuePairs.add(new BasicNameValuePair("contect", String.valueOf(txbContect.getText())));
                    nameValuePairs.add(new BasicNameValuePair("user", String.valueOf(GlobalVar.LoggedInUser.getNUM())));
                    nameValuePairs.add(new BasicNameValuePair("img", photoStr));
                    requestP.setEntity(new UrlEncodedFormEntity(nameValuePairs,"utf-8"));

                    response = client.execute(requestP);
                    String newTopicID = GlobalMethods.convertStreamToString(response.getEntity().getContent());

                    for(Tag t : selectedTags){
                        if(t.getNUM() == 0){
                            request.setURI(new URI("http://drycorporations.wc.lt/newTag.php?name=" + String.valueOf(t.getName()).replaceAll(" ","%20")));
                            client.execute(request);
                        }
                    }

                    GlobalVar.tagsList.removeAll(GlobalVar.tagsList);
                    try {
                        response = null;
                        request.setURI(new URI("http://drycorporations.wc.lt/select.php?table=Tags"));
                        response = client.execute(request);
                        String result = GlobalMethods.convertStreamToString(response.getEntity().getContent());
                        if(!result.equals("null")) {
                            JSONObject root = new JSONObject(result);
                            JSONArray tags = root.getJSONArray("Tags");
                            for(int i = 0 ; i < tags.length(); i++) {
                                JSONObject tagsJSONObject = tags.getJSONObject(i);
                                Tag tag = new Tag();
                                tag.setNUM(tagsJSONObject.getInt("Num"));
                                tag.setName(tagsJSONObject.getString("Name"));
                                GlobalVar.tagsList.add(tag);
                                Log.d("tags", tagsJSONObject.getString("Name"));
                            }
                        }
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    } catch (ClientProtocolException e) {
                        Toast.makeText(getApplicationContext(), "ישנה בעיה בחיבור לאינטרנט", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.d("test",String.valueOf(GlobalVar.tagsList.size()));

                    for(Tag t : selectedTags) {
                        request.setURI(new URI("http://drycorporations.wc.lt/newTopicTag.php?topic=" + Integer.parseInt(newTopicID) + "&tag=" + GlobalMethods.getTagFromName(t.getName()).getNUM()));
                        client.execute(request);
                    }

                    Toast.makeText(getApplicationContext(), "נושא נוסף בהצלחה!", Toast.LENGTH_SHORT).show();

                    finish();

                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    Toast.makeText(getApplicationContext(), "ישנה בעיה בחיבור לאינטרנט", Toast.LENGTH_SHORT).show();
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateTagsView(){
        tagsLayout.removeAllViewsInLayout();
        for(Tag t : selectedTags){
            TextView tv =(TextView) inflater.inflate(R.layout.tag, null);
            tv.setText(t.getName());
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView tx = (TextView) v;
                    selectedTags.remove(GlobalMethods.getTagFromName(String.valueOf(tx.getText()),selectedTags));
                    txbTags.setEnabled(selectedTags.size() < 2);
                    txbContect.requestFocus();
                    tagsLayout.removeAllViewsInLayout();
                    updateTagsView();
                    txbTags.requestFocus();
                    Log.d("tag",String.valueOf(selectedTags.size()));
                }
            });
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            llp.setMargins(5, 5, 5, 5);
            tv.setLayoutParams(llp);
            tagsLayout.addView(tv);
        }
    }

}
