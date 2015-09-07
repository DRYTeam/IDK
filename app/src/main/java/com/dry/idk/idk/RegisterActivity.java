package com.dry.idk.idk;


import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.dry.POCO.Facility;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class RegisterActivity extends Activity {
    EditText txb_Username;
    EditText txb_Password;
    EditText txb_ConfirmPassword;
    EditText txb_Email;
    //Buttons define
    Button btn_Register;
    Spinner spin_Facility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);

        //*not recommended* - fix NetworkCrashed by overriding the android regular behaviour
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //*not recommended*

        txb_Username = (EditText) this.findViewById(R.id.register_txbUsername);
        txb_Password = (EditText) this.findViewById(R.id.register_txbPassword);
        txb_ConfirmPassword = (EditText) this.findViewById(R.id.register_txbConfirmPassword);
        txb_Email = (EditText) this.findViewById(R.id.register_txbEmail);
        btn_Register = (Button) this.findViewById(R.id.register_btnRegister);
        spin_Facility = (Spinner) this.findViewById(R.id.register_spinFacility);

        //ArrayAdapter<String> karant_adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, GlobalVar.facilityList);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, GlobalVar.facilityList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_Facility.setAdapter(adapter);

        btn_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckValues()) {
                    HttpResponse response = null;
                    try {
                        HttpClient client = new DefaultHttpClient();
                        HttpGet request = new HttpGet();
                        request.setURI(new URI("http://drycorporations.wc.lt/register.php?user=" + txb_Username.getText() + "&password=" + txb_Password.getText() + "&email="+txb_Email.getText()+ "&facility="+String.valueOf(((Facility)spin_Facility.getSelectedItem()).getNUM())));
                        response = client.execute(request);
                        String result = GlobalMethods.convertStreamToString(response.getEntity().getContent());

                        Toast.makeText(getApplicationContext(), "נרשמת בהצלחה!", Toast.LENGTH_SHORT).show();

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

                } else {
                    Toast.makeText(getApplicationContext(), RegisterActivity.this.getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                }
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

        if(!ValidateAccess.CheckLength(txb_ConfirmPassword.getText().toString(),6,20) || !txb_ConfirmPassword.getText().toString().equals(txb_Password.getText().toString())){
            flag = false;
        }

        if( !ValidateAccess.CheckValue(txb_Email.getText().toString(),"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")){
            flag = false;
        }

        return flag;
    }
}
