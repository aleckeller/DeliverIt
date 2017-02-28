package com.aleckeller.deliverit;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;


/**
 * Created by aleckeller on 2/7/17.
 */

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {


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
    private Toolbar myToolbar;
    private boolean specialRequest;
    private String userName;

    @SuppressLint("ParcelCreator")
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            if (specialRequest){
                Intent specIntent = new Intent(LocationActivity.this, SpecialRequestActivity.class);
                specIntent.putExtra("userAddress", mAddressOutput);
                specIntent.putExtra("userName", getName());
                Log.d(TAG,"Name:" + getName());
                startActivity(specIntent);
                finish();
            }else{
                displayAddressOutput();
            }
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

        myToolbar = (Toolbar) findViewById(R.id.locToolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        myToolbar.getBackground().setAlpha(128);

        // session manager
        session = new SessionManager(getApplicationContext());

        mResultReceiver = new AddressResultReceiver(new Handler());

        db = new SQLiteHandler(getApplicationContext());

        specialRequest = false;

        FirebaseMessaging.getInstance().subscribeToTopic("user_regular");

        //check if driver here
        HashMap<String,String> user = db.getUserDetails();
        String isDriver = user.get("driver");
        if (session.isFinished() && isDriver.equals("true")){
            Intent intent = new Intent(LocationActivity.this, DriverActivity.class);
            startActivity(intent);
            finish();
        }
        else if (session.isFinished()){
            Intent intent = new Intent(LocationActivity.this, MainActivity.class);
            intent.putExtra("userAddress",mAddressOutput);
            intent.putExtra("userName",getName());
            startActivity(intent);
            finish();
        }

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
        createLocationRequest();

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
                    showAlertDialog();
                }
            }
        });
        AlertDialog alert = addressBuilder.create();
        alert.show();
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LocationActivity.this);
        builder.setTitle("Google Places")
                .setMessage("Please select a place you would like to order from");
        builder.setPositiveButton("Begin", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(LocationActivity.this, MainActivity.class);
                intent.putExtra("LatLng", latlng);
                intent.putExtra("userAddress",mAddressOutput);
                intent.putExtra("userName",getName());
                startActivity(intent);
                finish();
            }
        });
        AlertDialog alert = builder.create();
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
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latlng)      // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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
                        Log.d(TAG,"SUCCESS");
                        createLocationRequest();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            Log.d(TAG,"TURN ON");
                            status.startResolutionForResult(
                                    LocationActivity.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.d(TAG,"WHO KNOWS");
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
        else{
            Log.d(TAG,"LOCATION IS NULL");
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
        HashMap<String,String> user = db.getUserDetails();
        return user.get("name");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                if (!LoginManager.getInstance().equals(null)){
                    LoginManager.getInstance().logOut();
                }
                if (session.isDriverLoggedIn()){
                    session.setDriverLogin(false);
                }
                else{
                    session.setRegularLogin(false);
                }
                db.deleteUsers();
                session.setFinished(true);
                Intent loginIntent = new Intent(LocationActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
                return true;
            case R.id.newlocation:
                if (mGoogleApiClient.isConnected() && mLocation != null) {
                    startIntentService();
                }
                else{
                    Log.d(TAG,"mLocation is null");
                }
                mAddressRequested = true;
                setLocation();
                return true;
            case R.id.specialRequest:
                specialRequest = true;
                startIntentService();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

}
