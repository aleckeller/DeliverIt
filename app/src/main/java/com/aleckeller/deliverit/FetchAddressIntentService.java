package com.aleckeller.deliverit;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by aleckeller on 2/9/17.
 */
public class FetchAddressIntentService extends IntentService {

    public static final String TAG = FetchAddressIntentService.class.getSimpleName();
    protected ResultReceiver mReceiver;

    // Constructor
    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        String errorM = "";

        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);

        List<Address> addresses = null;

        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);

        try{
            addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
        }
        catch(IOException e){
            errorM = "Service not available";
            Log.e(TAG,errorM);
        }
        catch(IllegalArgumentException e){
            errorM = "Invalid latitude and longitude";
            Log.e(TAG,errorM);
        }

        if (addresses == null || addresses.size() == 0){
            if (errorM.isEmpty()){
                errorM = "No address found";
                Log.e(TAG, errorM);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorM);
        }
        else{
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();
            for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i(TAG, "Address found");
            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"),
                            addressFragments));
        }

    }

    private void deliverResultToReceiver(int resultCode, String m) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, m);
        mReceiver.send(resultCode, bundle);
    }
}
