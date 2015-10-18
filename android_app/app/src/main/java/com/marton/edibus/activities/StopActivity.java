package com.marton.edibus.activities;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.inject.Inject;
import com.marton.edibus.R;
import com.marton.edibus.WebCallBack;
import com.marton.edibus.adapters.StopAdapter;
import com.marton.edibus.events.MessageEvent;
import com.marton.edibus.models.Stop;
import com.marton.edibus.models.Trip;
import com.marton.edibus.network.BusWebService;
import com.marton.edibus.services.JourneyManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;
import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;

public class StopActivity extends RoboActionBarActivity implements OnMapReadyCallback {

    private EventBus eventBus = EventBus.getDefault();

    private Resources resources;

    private final Activity context = this;

    @Inject
    JourneyManager journeyManager;

    @Inject
    BusWebService busWebService;

    @InjectView(R.id.listView)
    ListView listView;

    private StopAdapter stopAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop);

        resources = getResources();

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_journey, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        double latitude = 55.928042085586306;
        double longitude = -3.1669341400265694;

        WebCallBack<List<Stop>> callback = new WebCallBack<List<Stop>>(){

            @Override
            public void onSuccess(List<Stop> stops) {
                ArrayList<Stop> stopsArrayList = new ArrayList<>(stops);
                stopAdapter = new StopAdapter(context, stopsArrayList, resources);
                listView.setAdapter(stopAdapter);
                for (int i = 0; i<stopsArrayList.size(); i++){
                    Stop stop = stopsArrayList.get(i);
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(stop.getLatitude(), stop.getLongitude()))
                            .title(String.valueOf(stop.getId()))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.maps_marker)));
                }
            }
        };

        busWebService.getClosestStops(latitude, longitude, 5, callback);

        eventBus.post(new MessageEvent("Hey"));
    }
}
