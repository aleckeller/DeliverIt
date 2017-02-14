package com.aleckeller.deliverit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.Profile;
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
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by aleckeller on 2/7/17.
 */

public class LocationActivity extends Activity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {


    private Button logoutBtn;
    private Button getAddressBtn;
    private boolean mRequestLocationUpdates = false;
    private Location mLocation;
    private double latitude;
    private double longtitude;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = LocationActivity.class.getSimpleName();
    private SQLiteHandler db;

    private LocationRequest mLocationRequest;
    protected AddressResultReceiver mResultReceiver;
    private boolean mAddressRequested;
    private String mAddressOutput;
    private SessionManager session;
    private LatLng latlng;

    @SuppressLint("ParcelCreator")
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            displayAddressOutput();
            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Log.d(TAG,"Address found");
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location);

        logoutBtn = (Button) findViewById(R.id.logoutBtn);
        getAddressBtn = (Button) findViewById(R.id.getAddress);

        // session manager
        session = new SessionManager(getApplicationContext());

        mResultReceiver = new AddressResultReceiver(new Handler());

        db = new SQLiteHandler(getApplicationContext());

        if (session.isFinished() && db.isDriver(getName())){
            Intent intent = new Intent(LocationActivity.this, DriverActivity.class);
            startActivity(intent);
            finish();
        }
        else if (session.isFinished()){
            Intent intent = new Intent(LocationActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }


        logoutBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (session.isLoggedIn()){
                    session.setLogin(false);
                }
                else{
                    session.fbSetLogin(false);
                    LoginManager.getInstance().logOut();
                }
                // Launching the login activity
                Intent intent = new Intent(LocationActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();

            }
        });

        getAddressBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mGoogleApiClient.isConnected() && mLocation != null) {
                    startIntentService();
                }
                mAddressRequested = true;
                setLocation();
            }
        });

        // CREATE GOOGLE API CLIENT IF IT HASN'T BEEN CREATED
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();
        }

        // GET GOOGLE MAP
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    private void displayAddressOutput() {
        AlertDialog.Builder addressBuilder = new AlertDialog.Builder(LocationActivity.this);
        addressBuilder.setTitle("Is this your address?")
                .setMessage(mAddressOutput);
        addressBuilder.setPositiveButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //MANUALLY ENTER ADDRESS
                Intent intent = new Intent(LocationActivity.this, AutoCompleteActivity.class);
                startActivity(intent);
                finish();
            }
        });
        addressBuilder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (db.isDriver(getName())){
                    // Start Driver Activity
                    session.setFinished(true);
                    Intent intent = new Intent(LocationActivity.this, DriverActivity.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    // since not driver, start main activity
                    session.setFinished(true);
                    Intent intent = new Intent(LocationActivity.this, MainActivity.class);
                    intent.putExtra("LatLng",latlng);
                    startActivity(intent);
                    finish();
                }
            }
        });
        AlertDialog alert = addressBuilder.create();
        alert.show();
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
            latlng = new LatLng(latitude,longtitude);
            mMap.addMarker(new MarkerOptions().position(latlng).title("You are here!"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(12));
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

    //******************************************* CONNECTION TO GOOGLE CLIENT ***********************************************
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
        if (mLocation != null){
            if (!Geocoder.isPresent()){
                Toast.makeText(this, "No geocoder available",
                        Toast.LENGTH_LONG).show();
                return;
            }
            if (mAddressRequested){
                startIntentService();
            }
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
    //******************************************* END OF CONNECTION TO GOOGLE CLIENT ***********************************************

    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLocation);
        startService(intent);
    }

    private String getName(){
        String name = "";
        if (session.isFBLoggedIn()) {
            Profile profile = Profile.getCurrentProfile();
            name = profile.getFirstName() + " " + profile.getLastName();
        }
        else if (session.isLoggedIn()){
            Intent intent = getIntent();
            name = intent.getStringExtra("name");
        }
        return name;
    }

}
