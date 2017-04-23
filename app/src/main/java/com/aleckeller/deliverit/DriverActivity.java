package com.aleckeller.deliverit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class DriverActivity extends Activity {

    private Button logoutBtn;
    private SessionManager session;
    private SQLiteHandler db;
    private String order;
    private DatabaseReference mDatabase;
    private TextView orderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver);

        session = new SessionManager(getApplicationContext());
        db = new SQLiteHandler(getApplicationContext());
        FirebaseMessaging.getInstance().subscribeToTopic("user_driver");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data: dataSnapshot.getChildren()){
                    String name = (String) data.child("name").getValue();
                    String userAddress = (String) data.child("userAddress").getValue();
                    String placeAddress = (String) data.child("placeAddress").getValue();
                    String placeName = (String) data.child("placeName").getValue();
                    String orderItems = (String) data.child("orderItems").getValue();
                    String amount = (String) data.child("amount").getValue();
                    order = "Place Name: " + placeName + "\n" +
                            "Place Address: " + placeAddress + "\n" +
                            "User Name: " + name + "\n" +
                            "User Address: " + userAddress + "\n" +
                            "Order Items: " + orderItems + "\n" +
                            "Amount: " + amount;
                }
                orderView = (TextView)findViewById(R.id.orderViewDriver);
                orderView.setText(order);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("DRIVER", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mDatabase.addValueEventListener(postListener);

        logoutBtn = (Button) findViewById(R.id.dLogOut);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             if (!LoginManager.getInstance().equals(null)) {
                                                 LoginManager.getInstance().logOut();
                                             }
                                             if (session.isDriverLoggedIn()) {
                                                 session.setDriverLogin(false);
                                             } else {
                                                 session.setRegularLogin(false);
                                             }
                                             FirebaseMessaging.getInstance().unsubscribeFromTopic("user_driver");
                                             db.deleteUsers();
                                             session.setFinished(true);
                                             // Launching the login activity
                                             Intent intent = new Intent(DriverActivity.this, LoginActivity.class);
                                             startActivity(intent);
                                             finish();

                                         }
                                     }

        );
    }

}