package com.example.jasper;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.jar.Attributes;

/**
 * This is a utility class helping in getting user location
 */
public class LocationManagerClass  implements LocationListener {
    private final Context mContext;
    private Activity mActivity = null;


    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private LocationManager mLocationManager;
    Location mLocation;
    private double lat;
    private double lng;
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATE = 10;

    private static final long MIN_TIME_BW_UPDATE = 1000 * 60 * 1;

    protected LocationManager locationManager;

    public LocationManagerClass(Activity activity) {

        this.mContext = activity.getApplicationContext();
        this.mActivity = activity;


    }

    public Location getLocation() {

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_ACCESS_COARSE_LOCATION);
        }
        else {
            mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            try {
                locationManager = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);
                isGPSEnabled = locationManager.isProviderEnabled(locationManager.GPS_PROVIDER);
                isNetworkEnabled = locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);

                if (!isGPSEnabled && !isNetworkEnabled) {
                    //no network or gps
                } else {
                    setCanGetLocation(true);

                    if (isNetworkEnabled) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATE,
                                MIN_DISTANCE_CHANGE_FOR_UPDATE,
                                this
                        );
                        if (locationManager != null) {
                            mLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (mLocation != null) {
                                setLat(mLocation.getLatitude());
                                setLng(mLocation.getLongitude());
                            }
                        }
                    }
                    if (isGPSEnabled) {
                        if (mLocation == null) {
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATE,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATE, this);
                            if (locationManager != null) {
                                mLocation = locationManager
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (mLocation != null) {
                                    setLat(mLocation.getLatitude());
                                    setLng(mLocation.getLongitude());
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {

            }
            return mLocation;
        }

        return null;
    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }



    private void setLat(double lat) {
        this.lat = lat;
    }


    private void setLng(double lng) {
        this.lng = lng;
    }


    private void setCanGetLocation(boolean canGetLocation) {
        this.canGetLocation = canGetLocation;
    }

}
