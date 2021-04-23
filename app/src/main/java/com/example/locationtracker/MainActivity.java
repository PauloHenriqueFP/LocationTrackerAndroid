package com.example.locationtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private static final int ID_FINE_LOCATION_REQUEST = 7;

    /* Time in seconds to ask for the current location */
    private static final int DEFAULT_UPDATE_INTERVAL = 30;
    private static final int FAST_UPDATE_INTERVAL = 5;

    /* UI widgets */
    private TextView msgText;
    private Switch trackingStatus;

    /* Get the location provider either gps or tower or wifi */
    private FusedLocationProviderClient fusedLocationProviderClient;

    /* Configuration class */
    private LocationRequest locationRequest;

    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        msgText = findViewById(R.id.msg_text);
        trackingStatus = findViewById(R.id.tracking_status);

        /* Setting up the gps configurations */
        locationRequest = new LocationRequest();
        locationRequest.setInterval(DEFAULT_UPDATE_INTERVAL * 1000);
        locationRequest.setFastestInterval(FAST_UPDATE_INTERVAL * 1000); // Consumes more power
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Toast
                        .makeText(
                                getApplicationContext(),
                                "CCLatitude: " + locationResult.getLastLocation().getLatitude() + "" +
                                        "\nCCLongitude: " + locationResult.getLastLocation().getLongitude(),
                                Toast.LENGTH_LONG
                        )
                        .show();

            }
        };

        /* Adding event to the Switch */
        trackingStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    startTrackLocation();

                } else {

                    stopTrackLocation();

                }
            }
        });
    } // end onCreate() method

    private void startTrackLocation() {
        msgText.setText("We are tracking your location !");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            trackLocation();
        }
    }

    private void stopTrackLocation() {
        msgText.setText("Click again to start tracking !");

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case ID_FINE_LOCATION_REQUEST :
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    trackLocation();
                }
                else {
                    Toast
                            .makeText(
                                    this,
                                    "You need to accept the permission in order to track the location",
                                    Toast.LENGTH_LONG
                            )
                            .show();

                    finish();
                }
                break;
        }
    }

    private void trackLocation() {

        /* Check android permissions */
        String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
        boolean hasLocationPermission = ActivityCompat
                .checkSelfPermission(this, locationPermission) == PackageManager.PERMISSION_GRANTED;

        if(hasLocationPermission) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    Toast
                            .makeText(
                                    getApplicationContext(),
                                    "TTLatitude: " + location.getLatitude() + "\nTTLongitude: " + location.getLongitude(),
                                    Toast.LENGTH_LONG
                            )
                            .show();
                }
            });
        }
        else {
            /* Request permissions */
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                        new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, // permissions
                        ID_FINE_LOCATION_REQUEST // request code to identify this request later
                );
            }
        }
    }
}