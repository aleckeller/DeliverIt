package com.aleckeller.deliverit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by aleckeller on 2/27/17.
 */
public class OrderNotificationActivity extends AppCompatActivity {
    private SessionManager session;
    private SQLiteHandler db;
    private String order;
    private TextView orderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ordernotification);

        session = new SessionManager(getApplicationContext());
        db = new SQLiteHandler(getApplicationContext());

        Intent intent = getIntent();
        order = intent.getStringExtra("order");

        orderView = (TextView) findViewById(R.id.orderView);
        orderView.setText(String.valueOf(order));
    }
}
