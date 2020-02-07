package ru.aydarov.geoapp;

import android.app.IntentService;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;


public class GeoService extends IntentService {
    private MutableLiveData<Location> mLocationMutableLiveData = new MutableLiveData<>();


    private final Binder mBinder = new LocalBinder();
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback mLocationCallback = new MainLocationCallback();
    public static final String TAG = "GeoService";
    private Geocoder mGeocoder;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public GeoService getService() {
            return GeoService.this;
        }
    }

    public GeoService() {
        super("GeoService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), mLocationCallback, null);

    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000L);
        locationRequest.setFastestInterval(5000L);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return locationRequest;
    }


    public MutableLiveData<Location> getLocationMutableLiveData() {
        return mLocationMutableLiveData;
    }

    private class MainLocationCallback extends LocationCallback {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Log.d(TAG, "onLocationResult() called with: locationResult = [" + locationResult + "]");

            if (locationResult == null) {
                return;
            }
            mLocationMutableLiveData.postValue(locationResult.getLastLocation());

        }


    }
}
