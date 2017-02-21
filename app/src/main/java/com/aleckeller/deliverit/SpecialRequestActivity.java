package com.aleckeller.deliverit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by aleckeller on 2/21/17.
 */
public class SpecialRequestActivity extends Activity {
    private LatLng latlng;
    private SessionManager session;
    private static final int PLACE_PICKER_REQUEST = 10;
    public static final String TAG = SpecialRequestActivity.class.getSimpleName();
    private static final int REQUEST_LEFT = 0;
    private TextView placeTextView;
    private ImageView roadImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.specialrequest);
        session = new SessionManager(getApplicationContext());
        placeTextView = (TextView) findViewById(R.id.textPlace);
        roadImage = (ImageView) findViewById(R.id.roadImage);
        roadImage.getBackground().setAlpha(100);
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place selectedPlace = PlacePicker.getPlace(this, data);
                placeTextView.setText("Selected Place: " + selectedPlace.getName());
            }
            else if (resultCode == REQUEST_LEFT)  {
                Intent intent = new Intent(SpecialRequestActivity.this, LocationActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

}
