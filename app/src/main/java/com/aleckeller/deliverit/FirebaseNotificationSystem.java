package com.aleckeller.deliverit;

import android.content.Context;
import android.util.Log;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by aleckeller on 2/23/17.
 */
public class FirebaseNotificationSystem {
    private final DatabaseReference database;
    public static final String TAG = FirebaseNotificationSystem.class.getSimpleName();
    private final String name;
    private final String placeAddress;
    private final String userAddress;
    private final String orderItems;
    private final String amount;

    public FirebaseNotificationSystem(String name, String placeAddress, String userAddress, String orderItems, String amount) {
        database = FirebaseDatabase.getInstance().getReference();
        this.name = name;
        this.placeAddress = placeAddress;
        this.userAddress = userAddress;
        this.orderItems = orderItems;
        this.amount = amount;
        listenForNotificationRequests();
    }
    private void listenForNotificationRequests(){
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Object user = dataSnapshot.getValue();
                Log.d(TAG,user.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        database.addValueEventListener(postListener);
    }
    public void writeToDatabase(){
        database.child("User").child("name").setValue(name);
        database.child("User").child("placeAddress").setValue(placeAddress);
        database.child("User").child("userAddress").setValue(userAddress);
        database.child("User").child("orderItems").setValue(orderItems);
        database.child("User").child("amount").setValue(amount);
    }
}
