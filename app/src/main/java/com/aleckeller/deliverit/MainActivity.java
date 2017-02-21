package com.aleckeller.deliverit;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final int PLACE_PICKER_REQUEST = 10;
    public static final String TAG = LocationActivity.class.getSimpleName();
    private SessionManager session;
    private LatLng latlng;
    private double latitude;
    private double longitude;
    private WebView webView;
    private String menu_url;
    private Toolbar myToolbar;
    private ProgressDialog menuLoadDialog;
    private ProgressDialog waitDialog;
    private EditText orderText;
    private Button orderBtn;
    private EditText amountText;
    private static final int REQUEST_LEFT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        session = new SessionManager(getApplicationContext());

        webView = (WebView) findViewById(R.id.webview);
        menu_url = "";

        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        orderText = (EditText) findViewById(R.id.orderTextView);
        amountText = (EditText) findViewById(R.id.orderAmount);
        orderBtn = (Button) findViewById(R.id.orderButton);

        waitDialog = ProgressDialog.show(MainActivity.this, "", "Loading...", true);

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

        orderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String itemOrdered = String.valueOf(orderText.getText());
                String itemAmount = String.valueOf(amountText.getText());
                if (!itemOrdered.equals("") && !itemAmount.equals("")){
                    showDialog("Order Item: " + itemOrdered + "\n" + "Amount: $" + itemAmount);
                }
                else if (!itemOrdered.equals("") && itemAmount.equals("")){
                    Toast.makeText(getApplicationContext(),"Please enter item amount. " + "\n" +
                            "If amount not shown, enter $0", Toast.LENGTH_LONG).show();
                }
                else if (itemOrdered.equals("") && !itemAmount.equals("")){
                    Toast.makeText(getApplicationContext(),"Please enter order", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(),"You have not entered any fields!", Toast.LENGTH_LONG).show();
                }
            }
        });

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
                doZomatoRequest(selectedPlace.getName().toString());
            }
            else if (resultCode == REQUEST_LEFT)  {
                Intent intent = new Intent(MainActivity.this, LocationActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    private void doZomatoRequest(final String placeName) {
        String tag_string_req = "zomato request";
        String uri = AppConfig.URL_ZOMATO + "lat=" + latitude + "&lon=" + longitude;
        Log.d(TAG,"Lat" + latitude + " Lon " + longitude);
        StringRequest strReq = new StringRequest(Request.Method.GET,
                uri, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Zomato response: " + response);
                try {
                    JSONObject jObj = new JSONObject(response);
                    if (!jObj.equals(null)) {
                        boolean notFound = true;
                        JSONArray restArray = jObj.getJSONArray("restaurants");
                        for (int i = 0; i < restArray.length(); i++){
                            // get the whole object from array
                            JSONObject restObj = restArray.getJSONObject(i);
                            // get the restaurant object
                            JSONObject restaurant = restObj.getJSONObject("restaurant");
                            // get the string from the objects
                            String name = restaurant.getString("name");
                            if (name.equals(placeName)){
                                notFound = false;
                                menu_url = restaurant.getString("menu_url");
                                if (!menu_url.equals("")) {
                                    webView.getSettings().setJavaScriptEnabled(true);
                                    waitDialog.dismiss();
                                    menuLoadDialog = ProgressDialog.show(MainActivity.this, "", "Your menu is loading...", true);
                                    webView.setWebViewClient(new WebViewClient() {
                                        @Override
                                        public void onPageStarted(WebView view, String url, Bitmap favicon) {
                                            menuLoadDialog.show();
                                        }

                                        @Override
                                        public void onPageFinished(WebView view, String url) {
                                            menuLoadDialog.dismiss();
                                        }

                                        @Override
                                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                            if (url.equals(menu_url)) {
                                                view.loadUrl(url);
                                            }
                                            return true;
                                        }
                                    });
                                    webView.loadUrl(menu_url);
                                    break;
                                }
                            }
                        }
                        if (notFound){
                            Toast.makeText(getApplicationContext(),
                                    "Place selected not found in our database! " +
                                            "Please select new location or enter special request!", Toast.LENGTH_LONG).show();
                            Intent locIntent = new Intent(MainActivity.this, LocationActivity.class);
                            startActivity(locIntent);
                            finish();
                        }

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
                if (session.isLoggedIn()) {
                    session.setLogin(false);
                } else {
                    session.fbSetLogin(false);
                    LoginManager.getInstance().logOut();
                }
                session.setFinished(false);
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
                return true;
            case R.id.newlocation:
                Intent locIntent = new Intent(MainActivity.this, LocationActivity.class);
                startActivity(locIntent);
                finish();
                return true;

            case R.id.specialRequest:
                Intent specIntent = new Intent(MainActivity.this, SpecialRequestActivity.class);
                startActivity(specIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void showDialog(String order){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Place this order?")
                .setMessage(order);
        builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MainActivity.this, CheckoutActivity.class);
                startActivity(intent);
                finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


}