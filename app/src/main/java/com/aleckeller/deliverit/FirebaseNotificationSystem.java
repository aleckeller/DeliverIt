package com.aleckeller.deliverit;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by aleckeller on 2/23/17.
 */
public class FirebaseNotificationSystem extends FirebaseMessagingService {
    private DatabaseReference database;
    public static final String TAG = FirebaseNotificationSystem.class.getSimpleName();
    private String name;
    private String placeAddress;
    private String userAddress;
    private String orderItems;
    private String amount;
    private String placeName;
    private boolean isDriver;
    private String user;
    private String order;

    public FirebaseNotificationSystem(){
    }

    public FirebaseNotificationSystem(String name, String placeAddress, String userAddress, String orderItems, String amount, String placeName, boolean isDriver) {
        database = FirebaseDatabase.getInstance().getReference();
        this.name = name;
        this.placeAddress = placeAddress;
        this.userAddress = userAddress;
        this.orderItems = orderItems;
        this.amount = amount;
        this.placeName = placeName;
        this.isDriver = isDriver;
        this.order = "";
        setUser();
    }

    public void writeToDatabase(){
        database.child("User").child("placeAddress").setValue(placeAddress);
        database.child("User").child("name").setValue(name);
        database.child("User").child("amount").setValue(amount);
        database.child("User").child("userAddress").setValue(userAddress);
        database.child("User").child("orderItems").setValue(orderItems);
        database.child("User").child("placeName").setValue(placeName);
    }

    private void listenForNotificationRequests(){
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
                    sendNotification();
                }

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
    private void sendNotification(){
        String tag_string_req = "Notification Request";
        JSONObject notiObjFields = new JSONObject();
        JSONObject noti = new JSONObject();
        try {
            if (user.equals("driver")){
                notiObjFields.put("title", "Driver");
                notiObjFields.put("text", "There is a new order available!" + "\n" + "\n" + order);
            }
            else{
                notiObjFields.put("title", "Regular User");
                notiObjFields.put("text", "You are a regular user");
            }
            noti.put("notification",notiObjFields);
            noti.put("to", "/topics/user_"+user);
            noti.put("priority",10);
            Log.d("BODYDYYDYY", noti.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String requestBody = noti.toString();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_NOTI, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Notification response: " + response);
                try {
                    JSONObject jObj = new JSONObject(response);
                    if (!jObj.equals(null)) {
                    } else {
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Content-Type"," application/json");
                params.put("Authorization", "key=" + Constants.firebase_api_key);
                return params;
            }
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }


        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotificationToPhone(remoteMessage.getNotification().getBody(), remoteMessage.getFrom());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private void sendNotificationToPhone(String messageBody, String from) {
        //if from regular user
        Log.d("Firebase",from);
        if (from.equals("/topics/user_driver")){
            Intent intent = new Intent(this, NotificationFromDriverActivity.class);
            intent.putExtra("order",messageBody);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.road)
                    .setContentTitle("DeliverIt")
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        }
        else{
            Intent intent = new Intent(this, NotificationFromRegularActivity.class);
            intent.putExtra("order",messageBody);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.road)
                    .setContentTitle("DeliverIt")
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        }
    }
    private void setUser(){
        //if the person logged in is a driver, send to regular user
        if (isDriver){
            user = "regular";
        //if the person logged in is a regular user, send to drivers
        }else{
            user = "driver";
        }
        //prevents async tasks
        listenForNotificationRequests();
    }

}
