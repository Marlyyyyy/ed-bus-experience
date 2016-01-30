package com.marton.edibus.journey.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.location.Location;
import android.support.v4.app.FragmentManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
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
import com.marton.edibus.shared.utilities.WebCallBack;
import com.marton.edibus.journey.adapters.ServiceAdapter;
import com.marton.edibus.journey.enums.StopTypeEnum;
import com.marton.edibus.journey.fragments.StopDialogFragment;
import com.marton.edibus.shared.models.Service;
import com.marton.edibus.shared.models.Stop;
import com.marton.edibus.shared.models.Ride;
import com.marton.edibus.shared.network.BusClient;
import com.marton.edibus.journey.utilities.CoordinateProvider;
import com.marton.edibus.journey.utilities.GpsCalculator;
import com.marton.edibus.journey.utilities.JourneyManager;
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

    private HashMap<Marker, Stop> stopMarkersHashMap;

    private Marker clickedPointMarker;

    private StopDialogFragment stopDialog;

    private FragmentManager fragmentManager;

    private GoogleMap googleMap;

    @InjectView(R.id.sliding_layout)
    private SlidingUpPanelLayout slidingUpPanelLayout;

    @Inject
    private JourneyManager journeyManager;

    @Inject
    private BusClient busWebService;

    @InjectView(R.id.listView)
    private ListView listView;

    @InjectView(R.id.sliding_up_panel_header_text)
    private TextView slidingUpPanelHeaderTextView;

    private ArrayList<Service> services;

    private ServiceAdapter serviceAdapter;

    private Location userLocation;

    private CoordinateProvider coordinateProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_setup);

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
            }
        });

        // Create Stop details dialog
        this.stopDialog = new StopDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("STOP_TYPE", stopTypeEnum);
        args.putBoolean("SERVICE_SELECTED", this.previewService != null);
        this.stopDialog.setArguments(args);

        // Disable service selection if the user is selecting an end stop
        if(this.stopTypeEnum.equals(StopTypeEnum.END)){
            this.slidingUpPanelLayout.setEnabled(false);
        }

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
            }

            @Override
            public void onPanelHidden(View view) {

            }
        });

        this.coordinateProvider = new CoordinateProvider(this);
    }

    @Override
    public void onStart(){
        super.onStart();

        final ActionBar actionBar = this.getSupportActionBar();

        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            switch (this.stopTypeEnum){
                case START:
                    setTitle("Start Stop");
                    break;
                case END:
                    setTitle("End Stop");
                    this.slidingUpPanelHeaderTextView.setText("Service: " + journeyManager.getRide().getService().getName());
                    break;
            }
        }
    }

    private void selectService(Service service){

        this.journeyManager.getRide().setService(service);
        this.previewService = service;

        final ProgressDialog progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(String.format("Loading service %s ...", service.getName()));
        progressDialog.setCancelable(false);
        progressDialog.show();

        this.slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

        WebCallBack<List<Stop>> stopCallback = new WebCallBack<List<Stop>>(){

            @Override
            public void onSuccess(List<Stop> stops) {

                googleMap.clear();

                // Calculate the distance between the user and the bus stop
                attachDistancesToStops(stops, userLocation);

                // Create the stop marker and add it to the list of stop-markers
                List<Marker> stopMarkers = new ArrayList<>();
                for (int i = 0; i< stops.size(); i++){
                    Stop stop = stops.get(i);
                    Marker marker = createAndAddStopMarker(stop, R.drawable.edi_bus_marker);
                    stopMarkers.add(marker);
                    stopMarkersHashMap.put(marker, stop);
                }

                // Change the camera position of the map
                moveCamera(100, stopMarkers);

                progressDialog.dismiss();
            }
        };

        this.busWebService.getStopsForService(this.journeyManager.getRide().getServiceId(), stopCallback);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        this.googleMap = googleMap;
        this.googleMap.setMyLocationEnabled(true);

        this.coordinateProvider.connect(new CoordinateProvider.CoordinateReceivedCallback() {
            @Override
            public void onCoordinateReceived(final Location location) {

                final double userLatitude = location.getLatitude();
                final double userLongitude = location.getLongitude();

                userLocation = location;

                // Handle clicks on the map
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {

                        if (stopTypeEnum.equals(StopTypeEnum.END)) {
                            return;
                        }

                        // Remove previously clicked point marker
                        if (clickedPointMarker != null) {
                            clickedPointMarker.remove();
                        }

                        // Add currently clicked point marker
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(new LatLng(latLng.latitude, latLng.longitude))
                                .anchor((float) 0.5, (float) 0.5);

                        clickedPointMarker = googleMap.addMarker(markerOptions);

                        // Append new stops to the map
                        WebCallBack<List<Stop>> stopCallback = new WebCallBack<List<Stop>>() {

                            @Override
                            public void onSuccess(List<Stop> stops) {

                                // Calculate the distance between the user and the bus stop
                                attachDistancesToStops(stops, location);

                                // Create the stop marker and add it to the list of stop-markers
                                for (int i = 0; i < stops.size(); i++) {
                                    Stop stop = stops.get(i);
                                    Marker marker = createAndAddStopMarker(stop, R.drawable.edi_bus_marker);

                                    // Make sure each stop is present only once within the hash-map
                                    addToHashMapIfValueNotExists(stopMarkersHashMap, marker, stop);
                                }

                                // Change the camera position of the map
                                moveCamera(100, new ArrayList<>(stopMarkersHashMap.keySet()));
                            }
                        };


                        busWebService.getStopsWithinRadius(latLng.latitude, latLng.longitude, CLOSEST_STOPS_RADIUS, stopCallback);
                    }
                });

                // Handle clicks on markers
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {

                        if (marker != clickedPointMarker) {
                            if(stopMarkersHashMap.containsKey(marker)){

                                // Launch custom dialog fragment
                                journeyManager.setReviewStop(stopMarkersHashMap.get(marker));
                                stopDialog.show(fragmentManager, "Stop Dialog Fragment");
                            }
                        }

                        return true;
                    }
                });

                // Called after obtaining a new list of stops
                WebCallBack<List<Stop>> stopCallback = new WebCallBack<List<Stop>>(){

                    @Override
                    public void onSuccess(List<Stop> stops) {

                        // Calculate the distance between the user and the bus stop
                        attachDistancesToStops(stops, location);

                        // Create the stop marker and add it to the list of stop-markers
                        List<Marker> stopMarkers = new ArrayList<>();
                        for (int i = 0; i< stops.size(); i++){
                            Stop stop = stops.get(i);
                            Marker marker = createAndAddStopMarker(stop, R.drawable.edi_bus_marker);
                            stopMarkers.add(marker);

                            // Make sure each stop is present only once within the hash-map
                            addToHashMapIfValueNotExists(stopMarkersHashMap, marker, stop);
                        }

                        // Change the camera position of the map
                        moveCamera(100, stopMarkers);
                    }
                };

                switch (stopTypeEnum){
                    case START:
                        busWebService.getStopsWithinRadius(userLatitude, userLongitude, CLOSEST_STOPS_RADIUS, stopCallback);
                        break;
                    case END:
                        Ride ride = journeyManager.getRide();
                        busWebService.getStopsForService(ride.getServiceId(), ride.getStartStopId(), stopCallback);
                        break;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {

        this.coordinateProvider.disconnect();
        super.onDestroy();
    }

    // Adjusts the camera position to include all visibleMarkers with the specified padding around the edges of the map
    private void moveCamera(int padding, List<Marker> visibleMarkers){

        if (visibleMarkers.size() == 0){
            return;
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : visibleMarkers) {
            builder.include(marker.getPosition());
        }

        // Padding from edges of the map in pixels
        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        this.googleMap.moveCamera(cameraUpdate);
    }

    // Calculates the distance between the user and each bus stop in the specified list. This method modifies the original object in place.
    private void attachDistancesToStops(List<Stop> stops, Location location){

        if (location == null){
            return;
        }

        for (int i=0; i<stops.size(); i++){
            Stop stop = stops.get(i);
            stop.setDistance(GpsCalculator.getDistanceBetweenPoints(location.getLatitude(), location.getLongitude(), stop.getLatitude(), stop.getLongitude()));
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
