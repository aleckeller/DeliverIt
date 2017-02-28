package com.aleckeller.deliverit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.facebook.login.LoginManager;
import com.google.firebase.messaging.FirebaseMessaging;

public class DriverActivity extends Activity {

    private Button logoutBtn;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver);

        session = new SessionManager(getApplicationContext());
        db = new SQLiteHandler(getApplicationContext());
        FirebaseMessaging.getInstance().subscribeToTopic("user_driver");

        logoutBtn = (Button) findViewById(R.id.dLogOut);
        logoutBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!LoginManager.getInstance().equals(null)){
                    LoginManager.getInstance().logOut();
                }
                if (session.isDriverLoggedIn()){
                    session.setDriverLogin(false);
                }
                else{
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
        });
    }

}