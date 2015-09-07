package com.dry.idk.idk;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.dry.POCO.Facility;
import com.dry.POCO.Subject;
import com.dry.POCO.Tag;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends Activity {

    //EditTexts define
    EditText txb_Username;
    EditText txb_Password;
    //Buttons define
    ImageButton btn_Login;
    Button btn_Register;
    Button btn_Guest;
    CheckBox login_rememberMe;
    SharedPreferences loggedInUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //*not recommended* - fix NetworkCrashed by overriding the android regular behaviour
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //*not recommended*

        loggedInUser = getSharedPreferences(GlobalVar.userdata, 0);

//        new Thread(new Runnable(){
//            @Override
//            public void run() {
//                try {
//                    HttpClient client = new DefaultHttpClient();
//                    HttpGet request = new HttpGet();
//                    HttpResponse response;
//                    try {
//                        response = null;
//                        request.setURI(new URI("http://drycorporations.wc.lt/select.php?table=Facility"));
//                        response = client.execute(request);
//                        String result = GlobalMethods.convertStreamToString(response.getEntity().getContent());
//                        if(!result.equals("null")) {
//                            JSONObject root = new JSONObject(result);
//                            JSONArray facility = root.getJSONArray("Facility");
//
//                            for(int i = 0 ; i < facility.length(); i++) {
//                                JSONObject facilityObj = facility.getJSONObject(i);
//                                Facility fac = new Facility();
//                                fac.setNUM(facilityObj.getInt("Num"));
//                                fac.setName(getResources().getString(getResources().getIdentifier(facilityObj.getString("Name"), "string", getPackageName())));
//                                GlobalVar.facilityList.add(fac);
//                            }
//                        }
//                    } catch (URISyntaxException e) {
//                        e.printStackTrace();
//                    } catch (ClientProtocolException e) {
//                        Toast.makeText(getApplicationContext(), "ישנה בעיה בחיבור לאינטרנט", Toast.LENGTH_SHORT).show();
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                    try {
//                        response = null;
//                        request.setURI(new URI("http://drycorporations.wc.lt/select.php?table=Subject"));
//                        response = client.execute(request);
//                        String result = GlobalMethods.convertStreamToString(response.getEntity().getContent());
//                        if(!result.equals("null")) {
//                            JSONObject root = new JSONObject(result);
//                            JSONArray subject = root.getJSONArray("Subject");
//                            for(int i = 0 ; i < subject.length(); i++) {
//                                JSONObject subjectObj = subject.getJSONObject(i);
//                                Subject sub = new Subject();
//                                sub.setNUM(subjectObj.getInt("Num"));
//                                sub.setName(subjectObj.getString("Name"));
//                                GlobalVar.subjectsList.add(sub);
//                                Log.d("tag",subjectObj.getString("Name"));
//                            }
//                        }
//                    } catch (URISyntaxException e) {
//                        e.printStackTrace();
//                    } catch (ClientProtocolException e) {
//                        Toast.makeText(getApplicationContext(), "ישנה בעיה בחיבור לאינטרנט", Toast.LENGTH_SHORT).show();
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                    try {
//                        response = null;
//                        request.setURI(new URI("http://drycorporations.wc.lt/select.php?table=Tags"));
//                        response = client.execute(request);
//                        String result = GlobalMethods.convertStreamToString(response.getEntity().getContent());
//                        if(!result.equals("null")) {
//                            JSONObject root = new JSONObject(result);
//                            JSONArray tags = root.getJSONArray("Tags");
//                            for(int i = 0 ; i < tags.length(); i++) {
//                                JSONObject tagsJSONObject = tags.getJSONObject(i);
//                                Tag tag = new Tag();
//                                tag.setNUM(tagsJSONObject.getInt("Num"));
//                                tag.setName(tagsJSONObject.getString("Name"));
//                                GlobalVar.tagsList.add(tag);
//                                Log.d("tags", tagsJSONObject.getString("Name"));
//                            }
//                        }
//                    } catch (URISyntaxException e) {
//                        e.printStackTrace();
//                    } catch (ClientProtocolException e) {
//                        Toast.makeText(getApplicationContext(), "ישנה בעיה בחיבור לאינטרנט", Toast.LENGTH_SHORT).show();
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
//        }).start();

        txb_Username = (EditText) this.findViewById(R.id.login_txbUsername);
        txb_Password = (EditText) this.findViewById(R.id.login_txbPassword);
        btn_Login = (ImageButton) this.findViewById(R.id.login_btnLogin);
        btn_Register = (Button) this.findViewById(R.id.login_btnRegister);
        btn_Guest = (Button) this.findViewById(R.id.login_btnGuest);
        login_rememberMe = (CheckBox) this.findViewById(R.id.login_cbxRememberMe);

        btn_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CheckValues()){
                    HttpResponse response = null;
                    try {
                        HttpClient client;
                        HttpGet request;

                        client = new DefaultHttpClient();
                        request = new HttpGet();
                        request.setURI(new URI("http://drycorporations.wc.lt/login.php?user="+txb_Username.getText()+"&&password="+txb_Password.getText()));
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

                            if(login_rememberMe.isChecked()){
                                SharedPreferences.Editor editor = loggedInUser.edit();
                                editor.putBoolean("isConnected",true);
                                editor.putString("Username",GlobalVar.LoggedInUser.getUsername());
                                editor.putString("Password",GlobalVar.LoggedInUser.getPassword());
                                editor.commit();
                            }

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), LoginActivity.this.getString(R.string.toast_login_error), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getApplicationContext(), LoginActivity.this.getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

    }

    private boolean CheckValues() {
        boolean flag = true;
        if( !ValidateAccess.CheckValue(txb_Username.getText().toString(),"^[0-9a-zA-Z]+$") || !ValidateAccess.CheckLength(txb_Username.getText().toString(),3,20)){
            flag = false;
        }
        if(!ValidateAccess.CheckLength(txb_Password.getText().toString(),6,20)){
            flag = false;
        }

        return flag;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
