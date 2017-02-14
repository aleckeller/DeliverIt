package com.aleckeller.deliverit;

import android.os.Bundle;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.Profile;
import com.facebook.login.LoginManager;

public class MainActivity extends Activity {

    private SessionManager session;
    private Button logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        session = new SessionManager(getApplicationContext());

        logoutBtn = (Button) findViewById(R.id.mBtnLogout);
        logoutBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (session.isLoggedIn()) {
                    session.setLogin(false);
                } else {
                    session.fbSetLogin(false);
                    LoginManager.getInstance().logOut();
                }
                session.setFinished(false);
                // Launching the login activity
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();

            }
        });


    }
}