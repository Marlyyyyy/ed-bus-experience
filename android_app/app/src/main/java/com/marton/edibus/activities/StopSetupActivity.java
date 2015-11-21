package com.marton.edibus.activities;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

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
import com.marton.edibus.adapters.ServiceAdapter;
import com.marton.edibus.enums.StopTypeEnum;
import com.marton.edibus.fragments.StopDialogFragment;
import com.marton.edibus.models.Service;
import com.marton.edibus.models.Stop;
import com.marton.edibus.models.Trip;
import com.marton.edibus.network.BusWebClient;
import com.marton.edibus.utilities.DistanceCalculator;
import com.marton.edibus.utilities.JourneyManager;
import com.marton.edibus.utilities.SnackbarManager;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

    private Marker userMarker;

    private HashMap<Marker, Stop> markerStopHashMap = new HashMap<>();

    private StopDialogFragment stopDialog;

    private FragmentManager fragmentManager;

    private GoogleMap googleMap;

    @InjectView(R.id.sliding_layout)
    SlidingUpPanelLayout slidingUpPanelLayout;

    @Inject
    JourneyManager journeyManager;

    @Inject
    BusWebClient busWebService;

    @InjectView(R.id.listView)
    ListView listView;

    ArrayList<Service> services;

    private ServiceAdapter serviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_setup);

        this.resources = getResources();
        this.fragmentManager = getSupportFragmentManager();

        // Read the data passed in for the activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.stopTypeEnum = (StopTypeEnum) extras.getSerializable("STOP");
        }

        // Create the map
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Create the list-view of services
        this.busWebService.getAllServices(new WebCallBack<List<Service>>() {
            @Override
            public void onSuccess(List<Service> data) {
                services = new ArrayList<>(data);
                serviceAdapter = new ServiceAdapter(context, services, resources);
                listView.setAdapter(serviceAdapter);
            }
        });

        // Make the ListView items selectable
        this.listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Service service = services.get(position);
                selectService(service);
                // listView.getChildAt(position).setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_bright));
                SnackbarManager.showSucess(view, String.valueOf(service.getId()));
            }
        });

        // Create Stop details dialog
        this.stopDialog = new StopDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("STOP_TYPE", stopTypeEnum);
        this.stopDialog.setArguments(args);
    }

    @Override
    public void onStart(){
        super.onStart();

        final ActionBar actionBar = this.getSupportActionBar();

        if (actionBar != null){
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.actionbar_journey, null);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            TextView activityTitleTextView = (TextView) view.findViewById(R.id.activity_title);

            switch (this.stopTypeEnum){
                case START:
                    activityTitleTextView.setText("Start Stop");
                    break;
                case END:
                    activityTitleTextView.setText("End Stop");
                    break;
            }
        }
    }

    private void selectService(Service service){
        Trip trip = this.journeyManager.getTrip();
        trip.setService(service);
        this.journeyManager.setTrip(trip);

        this.slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        this.busWebService.getStopsForService(this.journeyManager.getTrip().getServiceId(), this.createStopCallback());
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

        this.googleMap = googleMap;

        // Handle clicks on the map
        this.googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                WebCallBack stopCallback = createStopCallback(latLng.latitude, latLng.longitude);
                busWebService.getClosestStops(latLng.latitude, latLng.longitude, 5, stopCallback);
            }
        });

        // Handle clicks on markers
        this.googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                // Change the look of the icons
                if(latestClickedMarker != null){
                    latestClickedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.edi_bus_marker));
                }
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.edi_bus_logo_clicked));
                latestClickedMarker = marker;

                // Launch custom dialog fragment
                journeyManager.setReviewStop(markerStopHashMap.get(marker));
                stopDialog.show(fragmentManager, "Stop Dialog Fragment");

                return true;
            }
        });

        // TODO: get actual coordinates
        final double latitude = 55.928042085586306;
        final double longitude = -3.1669341400265694;

        WebCallBack stopCallback = this.createStopCallback(latitude, longitude);

        switch (this.stopTypeEnum){
            case START:
                this.busWebService.getClosestStops(latitude, longitude, 4, stopCallback);
                break;
            case END:
                this.busWebService.getStopsForService(this.journeyManager.getTrip().getServiceId(), stopCallback);
                break;
        }
    }

    private WebCallBack<List<Stop>> createStopCallback(){
        // TODO: get actual coordinates
        final double latitude = 55.928042085586306;
        final double longitude = -3.1669341400265694;

        return this.createStopCallback(latitude, longitude);
    }
    private WebCallBack<List<Stop>> createStopCallback(final double latitude, final double longitude){

        WebCallBack stopsCallback = new WebCallBack<List<Stop>>(){

            @Override
            public void onSuccess(List<Stop> stops) {

                Iterator iterator;

                // Set the distance for each stop
                for (int i=0; i<stops.size(); i++){
                    Stop stop = stops.get(i);
                    stop.setDistance(DistanceCalculator.getDistanceBetweenPoints(latitude, longitude, stop.getLatitude(), stop.getLongitude()));
                }

                List<Marker> markers = new ArrayList<>();
                  for (int i = 0; i< stops.size(); i++){
                    Stop stop = stops.get(i);
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(new LatLng(stop.getLatitude(), stop.getLongitude()))
                            .title(stop.getName())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.edi_bus_marker))
                            .anchor((float) 0.5, (float) 0.5)
                            .rotation(stop.getOrientation());
                    Marker marker = googleMap.addMarker(markerOptions);
                    markers.add(marker);

                    // Make sure each stop is present only once within the hash-map
                    iterator = markerStopHashMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry pair = (Map.Entry)iterator.next();
                        Stop existingStop = (Stop) pair.getValue();
                        if (existingStop.getId() == stop.getId()){
                            iterator.remove();
                            Marker existingMarker = (Marker) pair.getKey();
                            existingMarker.remove();
                        }
                    }
                    latestClickedMarker = null;
                    markerStopHashMap.put(marker, stop);
                }

                // Change the camera position of the map
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker marker : markers) {
                    builder.include(marker.getPosition());
                }

                // Padding from edges of the map in pixels
                int padding = 100;
                LatLngBounds bounds = builder.build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                googleMap.moveCamera(cameraUpdate);
            }
        };

        return stopsCallback;
    }
}
