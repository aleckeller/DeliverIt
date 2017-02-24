package com.aleckeller.deliverit;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by aleckeller on 2/9/17.
 */
public final class Constants {
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME =
            "com.google.android.gms.location.sample.locationaddress";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME +
            ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
            ".LOCATION_DATA_EXTRA";
    public static final int radius = 5000;

    public static final String zomato_api_key = "067d68c1900b384b782ab3049d7c8179";

    public static final String firebase_url = "https://deliverit-158103.firebaseio.com/";

    public static String reg_user = "";
}
