package com.dry.idk.idk;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.dry.POCO.Facility;
import com.dry.POCO.User;

/**
 * Created by yakov on 30/07/2015.
 */
public class ProfileActivity extends Activity  {

    TextView UserName;
    TextView FacilityTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);

        UserName = (TextView) this.findViewById(R.id.profile_username_text);
        FacilityTV = (TextView) this.findViewById(R.id.prodile_facility_text);


        UserName.setText(GlobalVar.LoggedInUser.getUsername());
        FacilityTV.setText(GlobalVar.LoggedInUser.getFacility().getName());
    }

}
