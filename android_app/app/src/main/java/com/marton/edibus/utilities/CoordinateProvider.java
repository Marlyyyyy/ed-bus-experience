package com.marton.edibus.utilities;


import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class CoordinateProvider implements GoogleApiClient.ConnectionCallbacks{

    private GoogleApiClient googleApiClient;

    private CoordinateReceivedCallback coordinateReceivedCallback;

    private Context context;

    public CoordinateProvider(Context context){

        this.context = context;

        this.googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {

        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(this.googleApiClient);

        // Make sure GPS returns any location
        if (lastLocation == null){
            Toast gpsWarningToast = Toast.makeText(this.context, "GPS is disabled", Toast.LENGTH_LONG);
            gpsWarningToast.show();
            return;
        }

        if (this.coordinateReceivedCallback != null){
            this.coordinateReceivedCallback.onCoordinateReceived(lastLocation);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void connect(CoordinateReceivedCallback coordinateReceivedCallback){
        this.coordinateReceivedCallback = coordinateReceivedCallback;
        this.googleApiClient.connect();
    }

    public void disconnect(){
        if (!this.googleApiClient.isConnected()){
            this.googleApiClient.disconnect();
        }
    }

    public interface CoordinateReceivedCallback{

        void onCoordinateReceived(Location location);
    }
}
