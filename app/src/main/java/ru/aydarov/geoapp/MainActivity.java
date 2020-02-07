package ru.aydarov.geoapp;

import android.Manifest;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 101;
    private static final String TAG = "MainActivity";

    private static final int REQUEST_CHECK_SETTINGS = 102;

    private MainViewModel mViewModel;
    private GeoService mGeoService;
    private GeoService mService;
    private boolean mBound = false;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            GeoService.LocalBinder binder = (GeoService.LocalBinder) service;
            mService = binder.getService();
            binded();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    private void binded() {
        mService.getLocationMutableLiveData().observe(this, new Observer<Location>() {
            @Override
            public void onChanged(Location location) {
                mViewModel.loadWeather(location);
            }
        });
    }

    private TextView mTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mTv = findViewById(R.id.tvTemp);
        checkGooglePlayServices();
    }

    private void initService() {
        onStartService();
        bindService();
    }

    private void checkGooglePlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        int statusCode = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (statusCode != ConnectionResult.SUCCESS) {
            Dialog errorDialog = googleApiAvailability.getErrorDialog(this, statusCode,
                    0, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            finish();
                        }
                    });

            errorDialog.show();
        } else {
            checkPermission();
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        } else {
            checkDeviceSettings();
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_CODE);
    }

    private void checkDeviceSettings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(getLocationRequest());

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d(TAG, "onSuccess() called with: locationSettingsResponse = [" + locationSettingsResponse + "]");

                initService();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        Log.d(TAG, "onFailure() called with: e = [" + e + "]");
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000L);
        locationRequest.setFastestInterval(5000L);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return locationRequest;
    }

    @Override
    protected void onResume() {
        mViewModel.getMutableLiveData().observe(this, new Observer<Float>() {
            @Override
            public void onChanged(Float aFloat) {
                mTv.setText(String.valueOf(aFloat.floatValue()));
            }
        });
        super.onResume();
    }

    void bindService() {
        Intent intent = new Intent(this, GeoService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

    }


    public void onStartService() {
        Intent intent = new Intent(this, GeoService.class);
        startService(intent);
    }

    public void onStopService() {
        Intent intent = new Intent(this, GeoService.class);
        stopService(intent);
    }

    void unbindService() {
        unbindService(mServiceConnection);
    }

    @Override
    protected void onDestroy() {
        unbindService();
        onStopService();
        super.onDestroy();
    }
}
