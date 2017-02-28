package com.aleckeller.deliverit;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by aleckeller on 2/1/17.
 */
public class SessionManager {
    // LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    SharedPreferences.Editor editor;
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "DeliverItLogin";

    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";

    private static final String DRIVER_LOGGED_IN = "DriverIsLoggedIn";

    private static final String finished = "finished";

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setRegularLogin(boolean isLoggedIn) {

        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);

        // commit changes
        editor.commit();

        Log.d(TAG, " User login session modified!");
    }

    public void setFinished(boolean isFinished) {

        editor.putBoolean(finished, isFinished);

        // commit changes
        editor.commit();

        Log.d(TAG, "Is Finished");
    }

    public void setDriverLogin(boolean isLoggedIn) {

        editor.putBoolean(DRIVER_LOGGED_IN, isLoggedIn);

        // commit changes
        editor.commit();

        Log.d(TAG, " User login session modified!");
    }
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }
    public boolean isFinished() {
        return pref.getBoolean(finished, false);
    }
    public boolean isDriverLoggedIn() {return pref.getBoolean(DRIVER_LOGGED_IN,false);}
    }

