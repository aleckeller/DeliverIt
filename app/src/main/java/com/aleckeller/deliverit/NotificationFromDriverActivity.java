package com.aleckeller.deliverit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by aleckeller on 2/27/17.
 */
public class NotificationFromDriverActivity extends AppCompatActivity {
    private SessionManager session;
    private SQLiteHandler db;
    private String order;
    private TextView orderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drivernotification);

        session = new SessionManager(getApplicationContext());
        db = new SQLiteHandler(getApplicationContext());

        Intent intent = getIntent();
        order = intent.getStringExtra("order");

        orderView = (TextView) findViewById(R.id.orderView);
        orderView.setText(String.valueOf(order));
    }

    public void sendNotification(View view){
        String tag_string_req = "Notification Request";
        JSONObject notiObjFields = new JSONObject();
        JSONObject noti = new JSONObject();
        try {
            notiObjFields.put("title", "DeliverIt");
            notiObjFields.put("text", "Someone has accepted your order!");
            noti.put("notification",notiObjFields);
            noti.put("to", "/topics/user_regular");
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
                Log.d("OrderNotification", "Notification response: " + response);
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
        Intent intent = new Intent(NotificationFromDriverActivity.this, DriverActivity.class);
        startActivity(intent);
        finish();
    }

    public void goToDriver(View view) {
        Intent intent = new Intent(NotificationFromDriverActivity.this, DriverActivity.class);
        startActivity(intent);
        finish();
    }
}
