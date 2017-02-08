package com.aleckeller.deliverit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.Profile;

import java.util.HashMap;


/**
 * Created by aleckeller on 2/7/17.
 */
public class LocationActivity extends Activity {

    private SQLiteHandler db;
    private SessionManager session;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());
        if (!session.isLoggedIn()) {
            logoutUser();
        }

        if(AppConfig.fbLoggedIn){
            Profile profile = Profile.getCurrentProfile();
            name = profile.getFirstName() + " " + profile.getLastName();
        }
        else{
            // Fetching user details from sqlite
            HashMap<String, String> user = db.getUserDetails();
            name = user.get("name");
        }

        // Welcome User
        AlertDialog.Builder builder = new AlertDialog.Builder(LocationActivity.this);
        builder.setTitle("DeliverIt")
                .setMessage("Welcome " + name);
        builder.setPositiveButton("Begin", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

    }


    private void logoutUser() {
        session.setLogin(false);
        AppConfig.fbLoggedIn = false;

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(LocationActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
