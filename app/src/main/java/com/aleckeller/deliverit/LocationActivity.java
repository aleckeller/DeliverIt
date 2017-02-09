package com.aleckeller.deliverit;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.Toast;

import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;


import com.facebook.Profile;
import com.google.android.gms.common.ConnectionResult;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;


/**
 * Created by aleckeller on 2/7/17.
 */

public class LocationActivity extends Activity implements OnMapReadyCallback {


    private SQLiteHandler db;
    private SessionManager session;
    private String name;
    private Button logoutBtn;
    private Location myLocation;
    private boolean mRequestLocationUpdates = false;
    private Location mLastLocation;
    private double latitude;
    private double longtitude;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = LocationActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private LocationRequest mLocationRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location);

        logoutBtn = (Button) findViewById(R.id.logoutBtn);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (AppConfig.fbLoggedIn) {
            Profile profile = Profile.getCurrentProfile();
            name = profile.getFirstName() + " " + profile.getLastName();
        } else {
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


        logoutBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                session.setLogin(false);
                AppConfig.fbLoggedIn = false;
                LoginManager.getInstance().logOut();
                //db.deleteUsers();
                // Launching the login activity
                Intent intent = new Intent(LocationActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();

            }
        });

        
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
}
