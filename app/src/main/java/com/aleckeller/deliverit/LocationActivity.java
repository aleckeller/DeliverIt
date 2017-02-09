package com.aleckeller.deliverit;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;



/**
 * Created by aleckeller on 2/7/17.
 */

public class LocationActivity extends Activity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private SessionManager session;
    private Button logoutBtn;
    private boolean mRequestLocationUpdates = false;
    private Location mLocation;
    private double latitude;
    private double longtitude;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = LocationActivity.class.getSimpleName();
    private LatLng latLng;

    private LocationRequest mLocationRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location);

        logoutBtn = (Button) findViewById(R.id.logoutBtn);

        // session manager
        session = new SessionManager(getApplicationContext());

//        if (AppConfig.fbLoggedIn) {
//            Profile profile = Profile.getCurrentProfile();
//            name = profile.getFirstName() + " " + profile.getLastName();
//        } else {
//            // Fetching user details from sqlite
//            HashMap<String, String> user = db.getUserDetails();
//            name = user.get("name");
//        }
//
//        // Welcome User
//        AlertDialog.Builder builder = new AlertDialog.Builder(LocationActivity.this);
//        builder.setTitle("DeliverIt")
//                .setMessage("Welcome " + name);
//        builder.setPositiveButton("Begin", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//        AlertDialog alert = builder.create();
//        alert.show();


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

        // CREATE GOOGLE API CLIENT IF IT HASN'T BEEN CREATED
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        createLocationRequest();

        // GET GOOGLE MAP
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

    //******************************************* ACTIVITY INFO ***********************************************
    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestLocationUpdates) {
            startLocationUpdates();
        }
    }

    //******************************************* END ACTIVITY INFO ***********************************************

    //******************************************* LOCATION INFO ***********************************************
    //sets map to current location
    private void setLocation() {
        if (mLocation != null){
            latitude = mLocation.getLatitude();
            longtitude = mLocation.getLongitude();
            Log.d(TAG, "Latitude: " +String.valueOf(latitude));
            Log.d(TAG, "Longtitude: " + String.valueOf(longtitude));
            latLng = new LatLng(latitude,longtitude);
            mMap.addMarker(new MarkerOptions().position(latLng).title("You are here!"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
        }

    }
    //creates location requests
    protected void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(30 * 1000);
        mLocationRequest.setFastestInterval(5 * 1000);
    }
    //stop location updates
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }
    //start location updates
    private void startLocationUpdates() {
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        catch(SecurityException e){

        }
    }
    //if the location chages, set mLocation as new location and then set map to new location
    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        setLocation();
    }

    //******************************************* END OF LOCATION INFO ***********************************************

    @Override
    public void onConnected(Bundle bundle) {
        LocationSettingsRequest.Builder build = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        build.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, build.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {

                // TO FIGURE OUT IF LOCATION IS TURNED ON
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        createLocationRequest();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(
                                    LocationActivity.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });

        //get last location
        try{
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        catch(SecurityException ignored){
        }
        setLocation();

        if (mRequestLocationUpdates){
            startLocationUpdates();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
