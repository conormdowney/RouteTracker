package com.example.conor.routetracker;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Created by User on 30/10/2014.
 */
public class GPSLocationListener implements LocationListener{

    double latitude;
    double longitude;

    public GPSLocationListener()
    {
        latitude = 0;
        longitude = 0;
    }

    public void onLocationChanged(Location location)
    {
        if(location != null)
        {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public void onProviderDisabled(String provider)
    {

    }

    public void onProviderEnabled(String provider)
    {

    }

    public void onStatusChanged(String provider, int status, Bundle extras)
    {

    }

}
