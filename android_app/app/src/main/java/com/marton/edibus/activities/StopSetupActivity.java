package com.marton.edibus.activities;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.inject.Inject;
import com.marton.edibus.R;
import com.marton.edibus.WebCallBack;
import com.marton.edibus.adapters.StopAdapter;
import com.marton.edibus.enums.StopTypeEnum;
import com.marton.edibus.models.Stop;
import com.marton.edibus.models.Trip;
import com.marton.edibus.network.BusWebClient;
import com.marton.edibus.utilities.JourneyManager;
import com.marton.edibus.utilities.SnackbarManager;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;

public class StopSetupActivity extends RoboActionBarActivity implements OnMapReadyCallback {

    private static final String TAG = StopSetupActivity.class.getName();

    private StopTypeEnum stopTypeEnum;

    private EventBus eventBus = EventBus.getDefault();

    private Resources resources;

    private final Activity context = this;

    private Marker latestClickedMarker;

    @Inject
    JourneyManager journeyManager;

    @Inject
    BusWebClient busWebService;

    @InjectView(R.id.listView)
    ListView listView;

    ArrayList<Stop> stopsArrayList;

    private StopAdapter stopAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop);

        resources = getResources();

        // Read the data passed in for the activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            stopTypeEnum = (StopTypeEnum) extras.getSerializable("STOP");
        }

        // Create the map
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Make the ListView items selectable
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Stop stop = stopsArrayList.get(position);
                selectStop(stop);
                // listView.getChildAt(position).setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_bright));
                SnackbarManager.showSnackbar(view, "success", String.valueOf(stop.getId()), resources);
            }
        });
    }

    private void selectStop(Stop stop){
        Trip trip = journeyManager.getTrip();
        switch (stopTypeEnum){
            case START:
                trip.setStartStop(stop);
                break;
            case END:
                trip.setEndStop(stop);
                break;
        }
        journeyManager.setTrip(trip);
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

        // Handle clicks on markers
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(latestClickedMarker != null){
                    latestClickedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.edi_bus_marker));
                }
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.edi_bus_logo_clicked));
                latestClickedMarker = marker;
                return false;
            }
        });

        double latitude = 55.928042085586306;
        double longitude = -3.1669341400265694;

        // TODO: put the list of services in the display
        /*googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Getting view from the layout file info_window_layout
                View view = getLayoutInflater().inflate(R.layout.info_window_stop, null);

                return view;
            }
        });*/

        WebCallBack<List<Stop>> callback = new WebCallBack<List<Stop>>(){

            @Override
            public void onSuccess(List<Stop> stops) {
                // Set up the list of stops
                stopsArrayList = new ArrayList<>(stops);
                stopAdapter = new StopAdapter(context, stopsArrayList, resources);
                listView.setAdapter(stopAdapter);

                List<Marker> markers = new ArrayList<>();
                for (int i = 0; i<stopsArrayList.size(); i++){
                    Stop stop = stopsArrayList.get(i);
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(new LatLng(stop.getLatitude(), stop.getLongitude()))
                            .title(stop.getName())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.edi_bus_marker))
                            .anchor((float) 0.5, (float) 0.5)
                            .rotation(stop.getOrientation());
                    Marker marker = googleMap.addMarker(markerOptions);
                    markers.add(marker);
                }

                // Change the camera position of the map
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker marker : markers) {
                    builder.include(marker.getPosition());
                }
                LatLngBounds bounds = builder.build();

                // Padding from edges of the map in pixels
                int padding = 100;
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                googleMap.animateCamera(cameraUpdate);
            }
        };

        switch (stopTypeEnum){
            case START:
                busWebService.getClosestStops(latitude, longitude, 4, callback);
                break;
            case END:
                busWebService.getStopsForService(this.journeyManager.getTrip().getServiceId(), callback);
                break;
        }
    }
}
