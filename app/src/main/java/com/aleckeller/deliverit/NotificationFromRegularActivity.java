package com.aleckeller.deliverit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by aleckeller on 2/27/17.
 */
public class NotificationFromRegularActivity extends AppCompatActivity {
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.regularnotification);

        session = new SessionManager(getApplicationContext());
        db = new SQLiteHandler(getApplicationContext());
    }
}
