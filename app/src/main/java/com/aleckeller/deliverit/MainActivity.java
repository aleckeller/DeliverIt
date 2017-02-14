package com.aleckeller.deliverit;

import android.net.Uri;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {

    private static final int PLACE_PICKER_REQUEST = 10;
    public static final String TAG = LocationActivity.class.getSimpleName();
    private SessionManager session;
    private Button logoutBtn;
    private LatLng latlng;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        session = new SessionManager(getApplicationContext());

        logoutBtn = (Button) findViewById(R.id.mBtnLogout);
        logoutBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (session.isLoggedIn()) {
                    session.setLogin(false);
                } else {
                    session.fbSetLogin(false);
                    LoginManager.getInstance().logOut();
                }
                session.setFinished(false);
                // Launching the login activity
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();

            }
        });
        Intent lastIntent = getIntent();
        latlng = lastIntent.getParcelableExtra("LatLng");
        if (latlng != null){
            LatLngBounds bounds = toBounds(latlng,Constants.radius);
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            builder.setLatLngBounds(bounds);
            try {
                startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
            } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        }
        else{
            session.setFinished(false);
            Intent intent = new Intent(MainActivity.this, LocationActivity.class);
            startActivity(intent);
            finish();
        }

    }

    public LatLngBounds toBounds(LatLng center, double radius) {
        LatLng southwest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 225);
        LatLng northeast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 45);
        return new LatLngBounds(southwest, northeast);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place selectedPlace = PlacePicker.getPlace(this, data);
                LatLng platlong = selectedPlace.getLatLng();
                latitude = platlong.latitude;
                longitude = platlong.longitude;
                Log.d(TAG, "Lat " + String.valueOf(platlong.latitude));
                Log.d(TAG, "Lon " +  String.valueOf(platlong.longitude));
                doRequest();
            }
        }
    }

    private void doRequest() {
        String tag_string_req = "zomato request";
        String uri = AppConfig.URL_ZOMATO + "lat=" + latitude + "&lon=" + longitude;
        StringRequest strReq = new StringRequest(Request.Method.GET,
                uri, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Zomato response: " + response.toString());
                try {
                    JSONObject jObj = new JSONObject(response);
                    if (!jObj.equals(null)) {
                        Log.d(TAG, "wow");
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Zomato Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("user-key",Constants.zomato_api_key);
                params.put("Accept", "application/json");
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
}