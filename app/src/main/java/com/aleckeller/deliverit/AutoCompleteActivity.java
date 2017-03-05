package com.aleckeller.deliverit;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by aleckeller on 2/9/17.
 */
public class AutoCompleteActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = AutoCompleteActivity.class.getSimpleName();
    private GoogleMap mMap;
    private LatLng latLng;
    private String mAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.autocomplete);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setHint("Address of Current Location");
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("US")
                .build();
        autocompleteFragment.setFilter(typeFilter);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                latLng = place.getLatLng();
                mAddress = String.valueOf(place.getAddress());
                setLocation();
                Intent intent = getIntent();
                boolean special = intent.getBooleanExtra("special",false);
                if (special){
                    String name = intent.getStringExtra("userName");
                    Intent specIntent = new Intent(AutoCompleteActivity.this, SpecialRequestActivity.class);
                    specIntent.putExtra("userAddress", mAddress);
                    specIntent.putExtra("userName", name);
                    startActivity(specIntent);
                    finish();
                }else{
                    showAlertDialog();
                }
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        // GET GOOGLE MAP
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.autoMap);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void setLocation() {
            mMap.addMarker(new MarkerOptions().position(latLng).title("You are here!"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AutoCompleteActivity.this);
        builder.setTitle("Google Places")
                .setMessage("Please select a place you would like to order from");
        builder.setPositiveButton("Begin", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(AutoCompleteActivity.this, MainActivity.class);
                intent.putExtra("LatLng", latLng);
                intent.putExtra("userAddress",mAddress);
                startActivity(intent);
                finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
