package com.aleckeller.deliverit;

/**
 * Created by aleckeller on 1/19/17.
 */

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends Activity {
    private static final String TAG = CreateAccountActivity.class.getSimpleName();
    private Button btnLogin;
    private TextView btnLinkToRegister;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private LoginButton fbLogin;
    private CallbackManager callbackManager;
    private String tmpDriver;
    private String fbEmail;
    private String id;
    private ProfileTracker mProfileTracker;
    private Profile profile;
    private String fbName;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //facebook events
        AppEventsLogger.activateApp(getApplication());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.login);

        inputEmail = (EditText) findViewById(R.id.registerName);
        inputPassword = (EditText) findViewById(R.id.passwordField);
        btnLogin = (Button) findViewById(R.id.loginButton);
        btnLinkToRegister = (TextView) findViewById(R.id.createAccount);

        // Session manager
        session = new SessionManager(getApplicationContext());

        if (session.isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, LocationActivity.class);
            startActivity(intent);
            finish();
        }
        else if (session.isDriverLoggedIn()){
            Intent intent = new Intent(LoginActivity.this, DriverActivity.class);
            startActivity(intent);
            finish();
        }
        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Since user won't be in database
        //******************************************* FACEBOOK LOGIN ****************************************************************
        fbLogin = (LoginButton) findViewById(R.id.facebookLogin);
        fbLogin.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_birthday", "user_friends"));

        fbLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //get FB EMAIL
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                // Application code
                                try {
                                    fbEmail = object.getString("email");
                                    id = object.getString("id");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();

                if(Profile.getCurrentProfile() == null) {
                    mProfileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                            // profile2 is the new profile
                            fbName = profile2.getFirstName() + " " + profile2.getLastName();
                            doesExist(fbName);
                            mProfileTracker.stopTracking();
                        }
                    };
                    // no need to call startTracking() on mProfileTracker
                    // because it is called by its constructor, internally.
                }
                else {
                    profile = Profile.getCurrentProfile();
                    fbName = profile.getFirstName() + " " + profile.getLastName();
                    doesExist(fbName);
                    Log.v("facebook - profile", profile.getFirstName());
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(),
                        error.toString(), Toast.LENGTH_LONG)
                        .show();

            }
        });

        //************************************************ END OF FACEBOOK LOGIN ****************************************************


        //*********************************************** REGULAR USER LOGIN **********************************************************
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                // Check for empty data in the form
                if (!email.isEmpty() && !password.isEmpty()) {
                    // login user
                    checkLogin(email, password);
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),
                            "Please enter the credentials!", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });

        // Link to Register Screen
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        CreateAccountActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

    private void setProfile(Profile profile){
        this.profile = profile;
    }

    /**
     * function to verify login details in mysql db
     */
    private void checkLogin(final String email, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        // user successfully logged in
                        // Now store the user in SQLite
                        String uid = jObj.getString("uid");
                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString("name");
                        String email = user.getString("email");
                        String driver = user.getString("driver");
                        String created_at = user
                                .getString("created_at");

                        // Inserting row in users table
                        db.addUser(name, email, driver, uid, created_at);
                        if (driver.equals("TRUE")){
                            Intent intent = new Intent(LoginActivity.this,
                                    DriverActivity.class);
                            session.setDriverLogin(true);
                            session.setRegularLogin(false);
                            startActivity(intent);
                            finish();
                        }
                        else{
                            Intent intent = new Intent(LoginActivity.this,
                                    LocationActivity.class);
                            session.setRegularLogin(true);
                            session.setDriverLogin(false);
                            intent.putExtra("name", name);
                            intent.putExtra("email", email);
                            intent.putExtra("driver", driver);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", password);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    //*********************************************** END OF REGULAR USER LOGIN **********************************************************


    private void doesExist(final String name) {
        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_EXISTS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Exists Response: " + response.toString());
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        JSONObject user = jObj.getJSONObject("user");
                        boolean exists = user.getBoolean("exists");
                        Log.d(TAG,String.valueOf(exists));
                        if (exists) {
                            isDriver(name);
                        } else {
                            askFBDriver();
                        }
                    } else {
                        Log.d(TAG, "PHP Error");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "isDriver Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", name);
                return params;
            }

        };
        AppController.getInstance().addToRequestQueue(strReq, "isDriver");

    }

    private void askFBDriver() {
        //Find out if they want to be a driver
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Driver?")
                .setMessage("Are you going to be a driver?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tmpDriver = "TRUE";
                registerFB(true);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tmpDriver = "FALSE";
                registerFB(false);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

    }

    private void isDriver(final String name) {
        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_isDriver, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "isDriver Response: " + response.toString());
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        JSONObject user = jObj.getJSONObject("user");
                        String stringDriver = user.getString("driver");
                        if (stringDriver.equals("TRUE")) {
                            db.addUser(name, fbEmail, "true", "uid", "created_at");
                            session.setRegularLogin(false);
                            session.setDriverLogin(true);
                            Intent intent = new Intent(LoginActivity.this, DriverActivity.class);
                            session.setDriverLogin(true);
                            startActivity(intent);
                            finish();
                        } else {
                            db.addUser(name, fbEmail, "false","uid", "created_at");
                            session.setDriverLogin(false);
                            session.setRegularLogin(true);
                            Intent intent = new Intent(LoginActivity.this, LocationActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Log.d(TAG, "PHP Error");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "isDriver Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", name);
                return params;
            }

        };
        AppController.getInstance().addToRequestQueue(strReq, "isDriver");
    }

    //***************************************************REGISTER FB USER***************************************************************
    private void registerFB(final boolean isADriver) {
        // get facebook information
        Profile profile = Profile.getCurrentProfile();
        final String name = profile.getFirstName() + " " + profile.getLastName();
        final String email = fbEmail;
        final String driver = tmpDriver;
        final String password = id;

        // register facebook user
        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQL
                        // Now store the user in sqlite
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString("name");
                        String email = user.getString("email");
                        String driver = user.getString("driver");
                        String created_at = user
                                .getString("created_at");

                        // Inserting row in users table
                        db.addUser(name, email, driver, uid, created_at);

                        Toast.makeText(getApplicationContext(), "User successfully registered", Toast.LENGTH_LONG).show();
                        // Launch main activity
                        if (isADriver){
                            session.setDriverLogin(true);
                            session.setRegularLogin(false);
                            Intent intent = new Intent(LoginActivity.this, DriverActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else{
                            session.setRegularLogin(true);
                            session.setDriverLogin(false);
                            Intent intent = new Intent(LoginActivity.this, LocationActivity.class);
                            startActivity(intent);
                            finish();
                        }

                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", name);
                params.put("email", email);
                params.put("driver", driver);
                params.put("password", password);

                return params;
            }

        };
        AppController.getInstance().addToRequestQueue(strReq, "fb_register");
    }
    //*********************************************** END OF FB REGISTER **********************************************************

}
