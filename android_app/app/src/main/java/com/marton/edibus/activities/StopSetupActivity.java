package com.marton.edibus.activities;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
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
import com.marton.edibus.utilities.GpsCalculator;
import com.marton.edibus.utilities.JourneyManager;
import com.marton.edibus.utilities.SnackbarManager;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;

public class StopSetupActivity extends RoboActionBarActivity implements OnMapReadyCallback {

    private static final double CLOSEST_STOPS_RADIUS = 0.2;

    // The enum indicating the type of the bus stop to be selected
    private StopTypeEnum stopTypeEnum;

    private Resources resources;

    private final Activity context = this;

    // The service for which the bus stops should be displayed
    private Service previewService;

    // The bus-stop marker which the user clicked on latest
    private Marker latestClickedBusMarker;

    private HashMap<Marker, Stop> stopMarkersHashMap;

    private Marker clickedPointMarker;

    private Circle clickedPointCircle;

    private StopDialogFragment stopDialog;

    private FragmentManager fragmentManager;

    private GoogleMap googleMap;

    @InjectView(R.id.sliding_layout)
    private SlidingUpPanelLayout slidingUpPanelLayout;

    @Inject
    private JourneyManager journeyManager;

    @Inject
    private BusWebClient busWebService;

    @InjectView(R.id.listView)
    private ListView listView;

    @InjectView(R.id.sliding_up_panel_header_text)
    private TextView slidingUpPanelHeaderTextView;

    private ArrayList<Service> services;

    private ServiceAdapter serviceAdapter;

    // TODO: get actual coordinates
    private final double userLatitude = 55.928042085586306;
    private final double userLongitude = -3.1669341400265694;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_setup);

        // Create The Toolbar and setting it as the Toolbar for the activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        this.resources = getResources();
        this.fragmentManager = getSupportFragmentManager();
        this.previewService = null;
        this.stopMarkersHashMap =  new HashMap<>();

        // Read the data passed in for the activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.stopTypeEnum = (StopTypeEnum) extras.getSerializable("STOP");
        }

        // Create the map on which the bus stops will be displayed
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
        args.putBoolean("SERVICE_SELECTED", this.previewService != null);
        this.stopDialog.setArguments(args);

        // Configure what should happen when there's interaction with the sliding-up-panel
        this.slidingUpPanelLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {
                slidingUpPanelHeaderTextView.setTextColor(ContextCompat.getColor(StopSetupActivity.this, R.color.white));
            }

            @Override
            public void onPanelCollapsed(View view) {
                slidingUpPanelHeaderTextView.setTextColor(ContextCompat.getColor(StopSetupActivity.this, R.color.black));
            }

            @Override
            public void onPanelExpanded(View view) {
                slidingUpPanelHeaderTextView.setTextColor(ContextCompat.getColor(StopSetupActivity.this, R.color.white));
            }

            @Override
            public void onPanelAnchored(View view) {
                //slidingUpPanelHeaderTextView.setTextColor(ContextCompat.getColor(StopSetupActivity.this, R.color.black));
            }

            @Override
            public void onPanelHidden(View view) {

            }
        });
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
        this.journeyManager.getTrip().setService(service);
        this.previewService = service;

        this.slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

        WebCallBack<List<Stop>> stopCallback = new WebCallBack<List<Stop>>(){

            @Override
            public void onSuccess(List<Stop> stops) {

                googleMap.clear();

                // Calculate the distance between the user and the bus stop
                attachDistancesToStops(stops);

                // Create the stop marker and add it to the list of stop-markers
                List<Marker> stopMarkers = new ArrayList<>();
                for (int i = 0; i< stops.size(); i++){
                    Stop stop = stops.get(i);
                    Marker marker = createAndAddStopMarker(stop, R.drawable.edi_bus_marker);
                    stopMarkers.add(marker);
                    stopMarkersHashMap.put(marker, stop);
                    latestClickedBusMarker = null;
                }

                // Change the camera position of the map
                moveCamera(100, stopMarkers);
            }
        };

        this.busWebService.getStopsForService(this.journeyManager.getTrip().getServiceId(), stopCallback);
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
        this.googleMap.setMyLocationEnabled(true);

        // Handle clicks on the map
        this.googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                // Remove previously clicked point marker
                if (clickedPointMarker != null){
                    clickedPointMarker.remove();
                }

                if (clickedPointCircle != null){
                    clickedPointCircle.remove();
                }

                // Add currently clicked point marker
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(latLng.latitude, latLng.longitude))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.edi_bus_marker))
                        .anchor((float) 0.5, (float) 0.5);

                CircleOptions circleOptions = new CircleOptions()
                        .center(new LatLng(latLng.latitude, latLng.longitude))
                        .radius(CLOSEST_STOPS_RADIUS * 1000)
                        .fillColor(R.color.map_circle)
                        .strokeWidth(0)
                        .zIndex(-1f);

                clickedPointCircle = googleMap.addCircle(circleOptions);
                clickedPointMarker = googleMap.addMarker(markerOptions);

                // Append new stops to the map
                WebCallBack<List<Stop>> stopCallback = new WebCallBack<List<Stop>>(){

                    @Override
                    public void onSuccess(List<Stop> stops) {

                        // Calculate the distance between the user and the bus stop
                        attachDistancesToStops(stops);

                        // Create the stop marker and add it to the list of stop-markers
                        List<Marker> stopMarkers = new ArrayList<>();
                        for (int i = 0; i< stops.size(); i++){
                            Stop stop = stops.get(i);
                            Marker marker = createAndAddStopMarker(stop, R.drawable.edi_bus_marker);
                            stopMarkers.add(marker);

                            // Make sure each stop is present only once within the hash-map
                            addToHashMapIfValueNotExists(stopMarkersHashMap, marker, stop);
                            latestClickedBusMarker = null;
                        }

                        // Change the camera position of the map
                        moveCamera(100, stopMarkers);
                    }
                };

                busWebService.getStopsWithinRadius(latLng.latitude, latLng.longitude, CLOSEST_STOPS_RADIUS, stopCallback);
            }
        });

        // Handle clicks on markers
        this.googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if (marker == clickedPointMarker){

                }else if(stopMarkersHashMap.containsKey(marker)){

                    // Change the look of the icons
                    if(latestClickedBusMarker != null){
                        latestClickedBusMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.edi_bus_marker));
                    }
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.edi_bus_logo_clicked));
                    latestClickedBusMarker = marker;

                    // Launch custom dialog fragment
                    journeyManager.setReviewStop(stopMarkersHashMap.get(marker));
                    stopDialog.show(fragmentManager, "Stop Dialog Fragment");
                }

                return true;
            }
        });

//        Location userLocation = this.googleMap.getMyLocation();
//        final double userLatitude = userLocation.getLatitude();
//        final double userLongitude = userLocation.getLongitude();

        final double userLatitude = 55.928042085586306;
        final double userLongitude = -3.1669341400265694;

        WebCallBack<List<Stop>> stopCallback = new WebCallBack<List<Stop>>(){

            @Override
            public void onSuccess(List<Stop> stops) {

                // Calculate the distance between the user and the bus stop
                attachDistancesToStops(stops);

                // Create the stop marker and add it to the list of stop-markers
                List<Marker> stopMarkers = new ArrayList<>();
                for (int i = 0; i< stops.size(); i++){
                    Stop stop = stops.get(i);
                    Marker marker = createAndAddStopMarker(stop, R.drawable.edi_bus_marker);
                    stopMarkers.add(marker);

                    // Make sure each stop is present only once within the hash-map
                    addToHashMapIfValueNotExists(stopMarkersHashMap, marker, stop);
                    latestClickedBusMarker = null;
                }

                // Change the camera position of the map
                moveCamera(100, stopMarkers);
            }
        };

        switch (this.stopTypeEnum){
            case START:
                this.busWebService.getStopsWithinRadius(userLatitude, userLongitude, CLOSEST_STOPS_RADIUS, stopCallback);
                break;
            case END:
                Trip trip = this.journeyManager.getTrip();
                this.busWebService.getStopsForService(trip.getServiceId(), trip.getStartStopId(), stopCallback);
                break;
        }
    }

    // Adjusts the camera position to include all visibleMarkers with the specified padding around the edges of the map
    private void moveCamera(int padding, List<Marker> visibleMarkers){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : visibleMarkers) {
            builder.include(marker.getPosition());
        }

        // Padding from edges of the map in pixels
        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        googleMap.moveCamera(cameraUpdate);
    }

    // Calculates the distance between the user and each bus stop in the specified list. This method modifies the original object in place.
    private void attachDistancesToStops(List<Stop> stops){

        for (int i=0; i<stops.size(); i++){
            Stop stop = stops.get(i);
            stop.setDistance(GpsCalculator.getDistanceBetweenPoints(this.userLatitude, this.userLongitude, stop.getLatitude(), stop.getLongitude()));
        }
    }

    private void addToHashMapIfValueNotExists(HashMap<Marker, Stop> hashmap, Marker marker, Stop stop){

        // Remove if a stop already exists with this ID
        Iterator iterator;
        iterator = hashmap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry)iterator.next();
            Stop existingStop = (Stop) pair.getValue();
            if (existingStop.getId() == stop.getId()){
                iterator.remove();
                Marker existingMarker = (Marker) pair.getKey();
                existingMarker.remove();
            }
        }

        // Add new stop
        hashmap.put(marker, stop);
    }

    private Marker createAndAddStopMarker(Stop stop, int image){

        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(stop.getLatitude(), stop.getLongitude()))
                .title(stop.getName())
                .icon(BitmapDescriptorFactory.fromResource(image))
                .anchor((float) 0.5, (float) 0.5)
                .rotation(stop.getOrientation());

        return this.googleMap.addMarker(markerOptions);
    }
}
