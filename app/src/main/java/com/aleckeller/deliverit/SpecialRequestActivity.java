package com.aleckeller.deliverit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by aleckeller on 2/21/17.
 */
public class SpecialRequestActivity extends AppCompatActivity {
    private LatLng latlng;
    private SessionManager session;
    private static final int PLACE_PICKER_REQUEST = 10;
    public static final String TAG = SpecialRequestActivity.class.getSimpleName();
    private static final int REQUEST_LEFT = 0;
    private TextView placeTextView;
    private ImageView roadImage;
    private EditText requestTextView;
    private WebView webView;
    private LinearLayout layout;
    private Toolbar myToolbar;
    private Place selectedPlace;
    private EditText requestAmountView;
    private String itemOrdered;
    private String itemAmount;
    private String mAddressOutput;
    private ProgressDialog waitDialog;
    private String placeAddress;
    private String userName;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.specialrequest);
        session = new SessionManager(getApplicationContext());
        placeTextView = (TextView) findViewById(R.id.textPlace);
        requestTextView = (EditText) findViewById(R.id.requestTextView);
        requestTextView.getBackground().setAlpha(130);
        requestAmountView = (EditText) findViewById(R.id.requestAmountView);
        layout = (LinearLayout) findViewById(R.id.layout);
        layout.getBackground().setAlpha(130);
        webView = (WebView) findViewById(R.id.placeWebView);
        myToolbar = (Toolbar) findViewById(R.id.specToolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        myToolbar.getBackground().setAlpha(128);
        db = new SQLiteHandler(getApplicationContext());
        Intent intent = getIntent();
        mAddressOutput = intent.getStringExtra("userAddress");
        userName = intent.getStringExtra("userName");

        Log.d(TAG,intent.getStringExtra("userAddress"));

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
                selectedPlace = PlacePicker.getPlace(this, data);
                placeTextView.setText(selectedPlace.getName());
                placeAddress = String.valueOf(selectedPlace.getAddress());
                String website = String.valueOf(selectedPlace.getWebsiteUri());
                if (!website.equals("null")) {
                    waitDialog = ProgressDialog.show(SpecialRequestActivity.this, "", "Loading Site...", true);
                    webView.setVisibility(View.VISIBLE);
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.setWebViewClient(new WebViewClient() {
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                            waitDialog.dismiss();
                            view.loadUrl(url);
                            return true;
                        }

                        @Override
                        public void onPageFinished(WebView view, String url) {
                            waitDialog.dismiss();
                        }
                    });
                    webView.loadUrl(website);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SpecialRequestActivity.this);
                    builder.setTitle("")
                            .setMessage("There is no website to display for this location!" + "\n" +
                                    "If you would still like to use this location, press OK and enter item(s)." + "\n" +
                                    "Select New Place to pick a new place!");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.setNegativeButton("New Place", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(SpecialRequestActivity.this, SpecialRequestActivity.class);
                            intent.putExtra("userAddress",mAddressOutput);
                            intent.putExtra("userName",userName);
                            startActivity(intent);
                            finish();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            } else if (resultCode == REQUEST_LEFT) {
                Intent intent = new Intent(SpecialRequestActivity.this, LocationActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    public void requestSubmit(View view) {
        itemOrdered = String.valueOf(requestTextView.getText());
        itemAmount = String.valueOf(requestAmountView.getText());
        if (!itemOrdered.equals("") && !itemAmount.equals("")) {
            showDialog("Place: " + selectedPlace.getName() + "\n" +
                    "Order: " + itemOrdered + "\n" + "Amount: $" + itemAmount);
        } else if (!itemOrdered.equals("") && itemAmount.equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter item amount. " + "\n" +
                    "If amount not shown, enter $0", Toast.LENGTH_LONG).show();
        } else if (itemOrdered.equals("") && !itemAmount.equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter order", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "You have not entered any fields!", Toast.LENGTH_LONG).show();
        }

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
                    db.deleteUsers();
                } else {
                    session.fbSetLogin(false);
                    LoginManager.getInstance().logOut();
                }
                db.deleteUsers();
                session.setFinished(true);
                Intent loginIntent = new Intent(SpecialRequestActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
                return true;
            case R.id.newlocation:
                Intent locIntent = new Intent(SpecialRequestActivity.this, LocationActivity.class);
                startActivity(locIntent);
                finish();
                return true;

            case R.id.specialRequest:
                Intent specIntent = new Intent(SpecialRequestActivity.this, SpecialRequestActivity.class);
                specIntent.putExtra("userAddress", mAddressOutput);
                specIntent.putExtra("userName", userName);
                startActivity(specIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void showDialog(String order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SpecialRequestActivity.this);
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
                Intent intent = new Intent(SpecialRequestActivity.this, CheckoutActivity.class);
                String placeName = String.valueOf(selectedPlace.getName());
                intent.putExtra("name", placeName);
                intent.putExtra("itemOrdered", itemOrdered);
                intent.putExtra("itemAmount", itemAmount);
                intent.putExtra("userAddress", mAddressOutput);
                intent.putExtra("placeAddress", placeAddress);
                intent.putExtra("userName",userName);
                startActivity(intent);
                finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
