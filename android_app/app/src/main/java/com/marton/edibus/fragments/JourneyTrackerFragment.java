package com.marton.edibus.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.inject.Inject;
import com.marton.edibus.R;
import com.marton.edibus.events.TrackerStateUpdatedEvent;
import com.marton.edibus.models.Stop;
import com.marton.edibus.models.Trip;
import com.marton.edibus.services.LocationProcessorService;
import com.marton.edibus.services.LocationProviderService;
import com.marton.edibus.utilities.JourneyManager;
import com.marton.edibus.utilities.SnackbarManager;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

public class JourneyTrackerFragment extends RoboFragment implements OnMapReadyCallback {

    private static final String TAG = JourneySetupFragment.class.getName();

    private EventBus eventBus = EventBus.getDefault();

    private Intent locationProviderService;

    private Intent locationProcessorService;

    private double latestUserLatitude;

    private double latestUserLongitude;

    ArrayList<Marker> stopMarkers;

    Marker userMarker;

    GoogleMap googleMap;

    @Inject private JourneyManager journeyManager;

    @InjectView(R.id.current_activity)
    TextView currentActivity;

    @InjectView(R.id.travelled_distance)
    TextView travelledDistanceTextView;

    @InjectView(R.id.remaining_distance)
    TextView remainingDistanceTextView;

    @InjectView(R.id.waiting_duration)
    TextView waitingDurationTextView;

    @InjectView(R.id.travelling_duration)
    TextView travellingDuration;

    @InjectView(R.id.start_journey)
    Button startJourneyButton;

    @InjectView(R.id.pause_journey)
    Button pauseJourneyButton;

    @InjectView(R.id.continue_journey)
    Button continueJourneyButton;

    @InjectView(R.id.finish_journey)
    Button finishJourneyButton;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);

        // Register as a subscriber
        this.eventBus.register(this);
        this.stopMarkers = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_journey_tracker,container,false);

        // Create the map
        MapFragment mapFragment = (MapFragment) this.getActivity().getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.locationProviderService = new Intent(getActivity(), LocationProviderService.class);
        this.locationProcessorService = new Intent(getActivity(), LocationProcessorService.class);


        // Set visibility of specific buttons
        this.refreshButtons();

        // Configure listeners for buttons
        this.startJourneyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (journeyManager.tripSetupComplete()) {
                    journeyManager.startTrip();
                    refreshButtons();

                    getActivity().startService(locationProviderService);
                    getActivity().startService(locationProcessorService);
                } else {
                    SnackbarManager.showSnackbar(getView(), "error", "Trip needs to be set up first!", getResources());
                }
            }
        });

        this.pauseJourneyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                journeyManager.pauseTrip();
                refreshButtons();
            }
        });

        this.continueJourneyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                journeyManager.continueTrip();
                refreshButtons();
            }
        });
        
        this.finishJourneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                journeyManager.finishTrip();
                refreshButtons();
                getActivity().stopService(locationProviderService);
                getActivity().stopService(locationProcessorService);
            }
        });
    }

    @Override
    public void onDestroy(){
        this.getActivity().stopService(this.locationProviderService);
        this.getActivity().stopService(this.locationProcessorService);
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.refreshMap();
    }

    // Sets all tracking related text-fields
    private void refreshDataInterface(TrackerStateUpdatedEvent trackerStateUpdatedEvent){

        this.currentActivity.setText(String.valueOf(trackerStateUpdatedEvent.getCurrentActivityEnum()));
        this.remainingDistanceTextView.setText(String.valueOf(trackerStateUpdatedEvent.getDistanceFromGoal()));
        this.travelledDistanceTextView.setText(String.valueOf(trackerStateUpdatedEvent.getDistanceFromStart()));
        this.waitingDurationTextView.setText(String.valueOf(trackerStateUpdatedEvent.getWaitingTime()));
        this.travellingDuration.setText(String.valueOf(trackerStateUpdatedEvent.getTravellingTime()));

        this.latestUserLatitude = trackerStateUpdatedEvent.getLatitude();
        this.latestUserLongitude = trackerStateUpdatedEvent.getLongitude();
    }

    // Hides and displays buttons according to the current state of the journey
    private void refreshButtons(){

        switch (this.journeyManager.getJourneyState()){
            case SETUP_INCOMPLETE:
                this.startJourneyButton.setVisibility(View.VISIBLE);
                this.pauseJourneyButton.setVisibility(View.GONE);
                this.continueJourneyButton.setVisibility(View.GONE);
                this.finishJourneyButton.setVisibility(View.GONE);
                break;
            case READY_TO_START:
                this.startJourneyButton.setVisibility(View.VISIBLE);
                this.pauseJourneyButton.setVisibility(View.GONE);
                this.continueJourneyButton.setVisibility(View.GONE);
                this.finishJourneyButton.setVisibility(View.GONE);
                break;
            case RUNNING:
                this.startJourneyButton.setVisibility(View.GONE);
                this.pauseJourneyButton.setVisibility(View.VISIBLE);
                this.continueJourneyButton.setVisibility(View.GONE);
                this.finishJourneyButton.setVisibility(View.GONE);
                break;
            case PAUSED:
                this.startJourneyButton.setVisibility(View.GONE);
                this.pauseJourneyButton.setVisibility(View.GONE);
                this.continueJourneyButton.setVisibility(View.VISIBLE);
                this.finishJourneyButton.setVisibility(View.VISIBLE);
                break;
            case FINISHED:
                this.startJourneyButton.setVisibility(View.GONE);
                this.pauseJourneyButton.setVisibility(View.GONE);
                this.continueJourneyButton.setVisibility(View.GONE);
                this.finishJourneyButton.setVisibility(View.GONE);
                break;
            case UPLOADED:
                break;
            default:
                SnackbarManager.showSnackbar(getView(), "error", "Journey is in an undefined state!", getResources());
                break;
        }
    }

    // Redraws all markers on the map
    private void refreshMap(){

        Trip trip = this.journeyManager.getTrip();
        Stop startStop = trip.getStartStop();
        Stop endStop = trip.getEndStop();

        if (startStop != null && endStop != null && this.googleMap != null){

            // Clear all markers
            this.googleMap.clear();

            // Add start-stop to the map
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(startStop.getLatitude(), startStop.getLongitude()))
                    .title(startStop.getName())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.edi_bus_marker))
                    .anchor((float) 0.5, (float) 0.5)
                    .rotation(startStop.getOrientation());

            Marker marker = this.googleMap.addMarker(markerOptions);
            this.stopMarkers.add(marker);

            // Add end-stop to the map
            markerOptions = new MarkerOptions()
                    .position(new LatLng(endStop.getLatitude(), endStop.getLongitude()))
                    .title(endStop.getName())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.edi_bus_marker))
                    .anchor((float) 0.5, (float) 0.5)
                    .rotation(endStop.getOrientation());

            marker = this.googleMap.addMarker(markerOptions);
            this.stopMarkers.add(marker);

            // Add user to the map
            if (this.latestUserLatitude == 0.0){
                this.latestUserLatitude = startStop.getLatitude();
            }

            if (this.latestUserLongitude == 0.0){
                this.latestUserLongitude = startStop.getLongitude();
            }

            markerOptions = new MarkerOptions()
                    .position(new LatLng(this.latestUserLatitude, this.latestUserLongitude))
                    .title("You")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.edi_bus_marker))
                    .anchor((float) 0.5, (float) 0.5);

            marker = this.googleMap.addMarker(markerOptions);
            this.userMarker = marker;

            // TODO: put in separate method. Detect when user moves the map.
            /*// Change the camera position of the map
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(this.userMarker.getPosition());

            // Padding from edges of the map in pixels
            int padding = 100;
            LatLngBounds bounds = builder.build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            googleMap.moveCamera(cameraUpdate);*/
        }
    }

    public void onEventMainThread(TrackerStateUpdatedEvent trackerStateUpdatedEvent){

        // If we are near the end, finish the tracking
        if (trackerStateUpdatedEvent.getDistanceFromGoal() < 50){

            this.journeyManager.finishTrip();

            // Automatically upload trip and/or fire events
            if (this.journeyManager.getAutomaticUpload())
            {
                SnackbarManager.showSnackbar(getView(), "success", "Uploading the trip...", getResources());
            }else
            {
                SnackbarManager.showSnackbar(getView(), "success", "Please upload the trip...", getResources());
            }
        }

        // Refresh the UI
        this.refreshDataInterface(trackerStateUpdatedEvent);
        this.refreshMap();
    }
}
