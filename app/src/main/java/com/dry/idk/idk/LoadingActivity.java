package com.dry.idk.idk;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.dry.POCO.Facility;
import com.dry.POCO.Subject;
import com.dry.POCO.Tag;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class LoadingActivity extends Activity {

    SharedPreferences loggedInUser;
    boolean isConnected;
    HttpClient client;
    HttpGet request;

    final Handler myHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_layout);

        //*not recommended* - fix NetworkCrashed by overriding the android regular behaviour
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //*not recommended*

        loggedInUser = getSharedPreferences(GlobalVar.userdata, 0);
        isConnected = loggedInUser.getBoolean("isConnected",false);

        client = new DefaultHttpClient();
        request = new HttpGet();

            new Thread(new Runnable() {
                @Override
                public void run() {


                    try {
                        HttpResponse response;
                        try {
                            GlobalVar.facilityList.removeAll(GlobalVar.facilityList);
                            response = null;
                            request.setURI(new URI("http://drycorporations.wc.lt/select.php?table=Facility"));
                            response = client.execute(request);
                            String result = GlobalMethods.convertStreamToString(response.getEntity().getContent());
                            if (!result.equals("null")) {
                                JSONObject root = new JSONObject(result);
                                JSONArray facility = root.getJSONArray("Facility");

                                for (int i = 0; i < facility.length(); i++) {
                                    JSONObject facilityObj = facility.getJSONObject(i);
                                    Facility fac = new Facility();
                                    fac.setNUM(facilityObj.getInt("Num"));
                                    fac.setName(getResources().getString(getResources().getIdentifier(facilityObj.getString("Name"), "string", getPackageName())));
                                    GlobalVar.facilityList.add(fac);
                                }
                            }
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        } catch (ClientProtocolException e) {
                            Toast.makeText(getApplicationContext(), "ישנה בעיה בחיבור לאינטרנט", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        try {
                            GlobalVar.subjectsList.removeAll(GlobalVar.subjectsList);
                            response = null;
                            request.setURI(new URI("http://drycorporations.wc.lt/select.php?table=Subject"));
                            response = client.execute(request);
                            String result = GlobalMethods.convertStreamToString(response.getEntity().getContent());
                            if (!result.equals("null")) {
                                JSONObject root = new JSONObject(result);
                                JSONArray subject = root.getJSONArray("Subject");
                                for (int i = 0; i < subject.length(); i++) {
                                    JSONObject subjectObj = subject.getJSONObject(i);
                                    Subject sub = new Subject();
                                    sub.setNUM(subjectObj.getInt("Num"));
                                    sub.setName(subjectObj.getString("Name"));
                                    GlobalVar.subjectsList.add(sub);
                                    Log.d("tag", subjectObj.getString("Name"));
                                }
                            }
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        } catch (ClientProtocolException e) {
                            Toast.makeText(getApplicationContext(), "ישנה בעיה בחיבור לאינטרנט", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        try {
                            GlobalVar.tagsList.removeAll(GlobalVar.tagsList);
                            response = null;
                            request.setURI(new URI("http://drycorporations.wc.lt/select.php?table=Tags"));
                            response = client.execute(request);
                            String result = GlobalMethods.convertStreamToString(response.getEntity().getContent());
                            if (!result.equals("null")) {
                                JSONObject root = new JSONObject(result);
                                JSONArray tags = root.getJSONArray("Tags");
                                for (int i = 0; i < tags.length(); i++) {
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
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    myHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(isConnected){
                                HttpResponse response = null;
                                try {
                                    request.setURI(new URI("http://drycorporations.wc.lt/login.php?user="+loggedInUser.getString("Username",null)+"&&password="+loggedInUser.getString("Password",null)));
                                    response = client.execute(request);
                                    String result = GlobalMethods.convertStreamToString(response.getEntity().getContent());
                                    if(!result.equals("null")) {
                                        JSONObject json = new JSONObject(result);
                                        GlobalVar.LoggedInUser.setNUM(json.getInt("Num"));
                                        GlobalVar.LoggedInUser.setUsername(json.getString("Username"));
                                        GlobalVar.LoggedInUser.setPassword(json.getString("Password"));
                                        GlobalVar.LoggedInUser.setEmail(json.getString("Email"));
                                        for (Facility fac : GlobalVar.facilityList){
                                            if(fac.getNUM() == json.getInt("Facility")) {
                                                GlobalVar.LoggedInUser.setFacility(fac);
                                            }
                                        }
                                        Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "שם המשתמש והסיסמה שנשמרו אינם נכונים!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(LoadingActivity.this,LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
//                        if(result.equals("1")){
//                            Toast.makeText(getApplicationContext(), "התחברת בהצלחה", Toast.LENGTH_SHORT).show();
//                        } else {
//                            Toast.makeText(getApplicationContext(), "שם משתמש או סיסמה שגויים", Toast.LENGTH_SHORT).show();
//                        }

                                } catch (URISyntaxException e) {
                                    e.printStackTrace();
                                } catch (ClientProtocolException e) {
                                    // TODO Auto-generated catch block
                                    Toast.makeText(getApplicationContext(), "ישנה בעיה בחיבור לאינטרנט", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Intent intent = new Intent(LoadingActivity.this,LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        }

                    });
                }
                }).start();


    }

}
