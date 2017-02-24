package com.aleckeller.deliverit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.firebase.client.Firebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by aleckeller on 2/16/17.
 */
public class CheckoutActivity extends AppCompatActivity {
    private Toolbar myToolbar;
    private SessionManager session;
    private TextView name;
    private TextView orderItemView;
    private TextView amountView;
    private TextView userAddressView;
    public static final String TAG = CheckoutActivity.class.getSimpleName();
    private String placeName;
    private String orderItems;
    private String amount;
    private String userAddress;
    private String placeAddress;
    private SQLiteHandler db;
    private String userName;
    private HashMap<String, String> userDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkout);

        session = new SessionManager(getApplicationContext());
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        db = new SQLiteHandler(getApplicationContext());

        Firebase.setAndroidContext(this);

        name = (TextView) findViewById(R.id.name);
        orderItemView = (TextView) findViewById(R.id.orderItem);
        amountView = (TextView) findViewById(R.id.amountView);
        userAddressView = (TextView) findViewById(R.id.yourAddressView);

        Intent intent = getIntent();
        placeName = intent.getStringExtra("placeName");
        placeAddress = intent.getStringExtra("placeAddress");
        orderItems = intent.getStringExtra("itemOrdered");
        amount = intent.getStringExtra("itemAmount");
        userAddress = intent.getStringExtra("userAddress");
        userDetails = db.getUserDetails();
        userName = userDetails.get("name");


        name.setText(placeName);
        orderItemView.setText(orderItems);
        amountView.setText("$" + amount);
        userAddressView.setText(userAddress);

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
                db.deleteUsers();
                session.setFinished(true);
                Intent loginIntent = new Intent(CheckoutActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
                return true;
            case R.id.newlocation:
                Intent locIntent = new Intent(CheckoutActivity.this, LocationActivity.class);
                startActivity(locIntent);
                finish();
                return true;

            case R.id.specialRequest:
                Intent specIntent = new Intent(CheckoutActivity.this, SpecialRequestActivity.class);
                specIntent.putExtra("userAddress",userAddress);
                specIntent.putExtra("userName",userName);
                startActivity(specIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public void placeOrder(View view) {
        String stringIsDriver = userDetails.get("driver");
        boolean isDriver = false;
        if (stringIsDriver.equals("TRUE")){
            isDriver = true;
        }
        sendNotification(userName,placeAddress,userAddress,orderItems,amount,placeName,isDriver);
    }

    private void sendNotification(String userName, String placeAddress, String userAddress, String orderItems, String amount, String placeName, boolean isDriver) {
        FirebaseNotificationSystem system = new FirebaseNotificationSystem(userName,placeAddress,userAddress,orderItems,amount,placeName,isDriver);
        system.writeToDatabase();
    }

}
