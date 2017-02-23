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

import com.facebook.login.LoginManager;

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

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkout);

        session = new SessionManager(getApplicationContext());
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        name = (TextView) findViewById(R.id.name);
        orderItemView = (TextView) findViewById(R.id.orderItem);
        amountView = (TextView) findViewById(R.id.amountView);
        userAddressView = (TextView) findViewById(R.id.yourAddressView);

        Intent intent = getIntent();
        name.setText(intent.getStringExtra("name"));
        orderItemView.setText(intent.getStringExtra("itemOrdered"));
        amountView.setText("$" + intent.getStringExtra("itemAmount"));
        String address = intent.getStringExtra("userAddress");
        Log.d(TAG,address);
        userAddressView.setText(intent.getStringExtra("userAddress"));

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
                startActivity(specIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public void placeOrder(View view) {
    }

    public void goPayment(View view) {
    }
}
