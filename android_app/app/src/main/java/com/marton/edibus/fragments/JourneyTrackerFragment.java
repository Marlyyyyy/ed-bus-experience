package com.marton.edibus.fragments;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.inject.Inject;
import com.marton.edibus.R;
import com.marton.edibus.WebCallBack;
import com.marton.edibus.enums.JourneyStateEnum;
import com.marton.edibus.enums.TripActionEnum;
import com.marton.edibus.events.TimerUpdatedEvent;
import com.marton.edibus.events.TrackerStateUpdatedEvent;
import com.marton.edibus.events.TripActionFiredEvent;
import com.marton.edibus.models.Stop;
import com.marton.edibus.models.Trip;
import com.marton.edibus.services.LocationProcessorService;
import com.marton.edibus.services.LocationProviderService;
import com.marton.edibus.utilities.JourneyManager;
import com.marton.edibus.utilities.SnackbarManager;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

public class JourneyTrackerFragment extends RoboFragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private static final String TAG = JourneySetupFragment.class.getName();

    private EventBus eventBus = EventBus.getDefault();

    private Intent locationProviderService;

    private Intent locationProcessorService;

    private double latestUserLatitude;

    private double latestUserLongitude;

    private ArrayList<Marker> stopMarkers;

    private Marker userMarker;

    private GoogleMap googleMap;

    private TripActionFiredEvent tripActionFiredEvent;

    private AlertDialog deleteJourneyDialog;

    private AlertDialog continueJourneyDialog;

    private DecimalFormat decimalFormat;

    private DateFormat dateFormat;

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

    @InjectView(R.id.delete_journey)
    Button deleteJourneyButton;

    @InjectView(R.id.upload_journey)
    Button uploadJourneyButton;

    @InjectView(R.id.elapsed_time)
    TextView elapsedTimeTextView;

    @InjectView(R.id.average_speed)
    TextView averageSpeedTextView;

    @InjectView(R.id.maximum_speed)
    TextView maximumSpeedTextView;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);

        this.stopMarkers = new ArrayList<>();

        // Initialise event objects
        this.tripActionFiredEvent = new TripActionFiredEvent();

        // Set up text view formats
        this.decimalFormat = new DecimalFormat(".##");
        this.dateFormat = new SimpleDateFormat("mm:ss");

        // Set up Delete Journey alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Are you sure?");
        builder.setPositiveButton("Yes, delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                journeyManager.setDefaults();
                getActivity().finish();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        this.deleteJourneyDialog = builder.create();

        // Set up Continue Journey alert dialog
        builder.setTitle("Add another trip?");
        builder.setPositiveButton("Yes, let's continue", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                eventBus.post(new TripActionFiredEvent(TripActionEnum.NEW_TRIP));
            }
        });
        builder.setNegativeButton("No, I'm finished", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                journeyManager.setDefaults();
                getActivity().finish();
                SnackbarManager.showSucess(getView(), "Thank you for your feedback!");
            }
        });
        this.continueJourneyDialog = builder.create();
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

                    // Move on to the feedback page
                    tripActionFiredEvent.setTripActionEnum(TripActionEnum.TRIP_STARTED);
                    eventBus.post(tripActionFiredEvent);

                    // Start the services
                    getActivity().startService(locationProviderService);
                    getActivity().startService(locationProcessorService);

                } else {
                    SnackbarManager.showError(getView(), "Trip needs to be set up first!");
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

                // Stop the services
                getActivity().stopService(locationProviderService);
                getActivity().stopService(locationProcessorService);
            }
        });

        this.uploadJourneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ProgressDialog progressDialog = new ProgressDialog(getActivity(), R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Uploading...");
                progressDialog.show();

                WebCallBack<Integer> callback = new WebCallBack<Integer>() {
                    @Override
                    public void onSuccess(Integer data) {
                        Stop previousEndStop = journeyManager.getTrip().getEndStop();
                        Trip trip = new Trip();
                        trip.setJourneyId(data);
                        trip.setStartStop(previousEndStop);

                        journeyManager.setDefaults();
                        journeyManager.setTrip(trip);

                        continueJourneyDialog.show();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure(int statusCode, String message){
                        SnackbarManager.showError(getView(), String.format("Journey upload has failed, status code: %d!", statusCode));

                        progressDialog.dismiss();
                    }
                };

                journeyManager.saveTrip(callback);
            }
        });

        this.deleteJourneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteJourneyDialog.show();
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();

        // Register as a subscriber
        this.eventBus.register(this);
    }

    @Override
    public void onPause(){
        super.onPause();

        // Register as a subscriber
        this.eventBus.unregister(this);
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
        this.googleMap.setMyLocationEnabled(true);
        //this.googleMap.setOnMapClickListener(this);
        this.refreshMap();
    }

    // Sets all tracking related text-fields
    private void refreshDataInterface(TrackerStateUpdatedEvent trackerStateUpdatedEvent){

        // TODO: make them default on demand
        this.currentActivity.setText(String.valueOf(trackerStateUpdatedEvent.getCurrentActivityEnum()));
        this.remainingDistanceTextView.setText(this.decimalFormat.format(trackerStateUpdatedEvent.getDistanceFromGoal()) + "m");
        this.travelledDistanceTextView.setText(this.decimalFormat.format(trackerStateUpdatedEvent.getDistanceFromStart()) + "m");
        this.averageSpeedTextView.setText(this.decimalFormat.format(trackerStateUpdatedEvent.getAverageSpeed() * 3.6) + "km/h");
        this.maximumSpeedTextView.setText(this.decimalFormat.format(trackerStateUpdatedEvent.getMaximumSpeed() * 3.6) + "km/h");

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
                this.uploadJourneyButton.setVisibility(View.GONE);
                this.deleteJourneyButton.setVisibility(View.GONE);
                break;
            case READY_TO_START:
                this.startJourneyButton.setVisibility(View.VISIBLE);
                this.pauseJourneyButton.setVisibility(View.GONE);
                this.continueJourneyButton.setVisibility(View.GONE);
                this.finishJourneyButton.setVisibility(View.GONE);
                this.uploadJourneyButton.setVisibility(View.GONE);
                this.deleteJourneyButton.setVisibility(View.GONE);
                break;
            case RUNNING:
                this.startJourneyButton.setVisibility(View.GONE);
                this.pauseJourneyButton.setVisibility(View.VISIBLE);
                this.continueJourneyButton.setVisibility(View.GONE);
                this.finishJourneyButton.setVisibility(View.GONE);
                this.uploadJourneyButton.setVisibility(View.GONE);
                this.deleteJourneyButton.setVisibility(View.GONE);
                break;
            case PAUSED:
                this.startJourneyButton.setVisibility(View.GONE);
                this.pauseJourneyButton.setVisibility(View.GONE);
                this.continueJourneyButton.setVisibility(View.VISIBLE);
                this.finishJourneyButton.setVisibility(View.VISIBLE);
                this.uploadJourneyButton.setVisibility(View.GONE);
                this.deleteJourneyButton.setVisibility(View.GONE);
                break;
            case FINISHED:
                this.startJourneyButton.setVisibility(View.GONE);
                this.pauseJourneyButton.setVisibility(View.GONE);
                this.continueJourneyButton.setVisibility(View.GONE);
                this.finishJourneyButton.setVisibility(View.GONE);
                this.uploadJourneyButton.setVisibility(View.VISIBLE);
                this.deleteJourneyButton.setVisibility(View.VISIBLE);
                break;
            case UPLOADED:
                break;
            default:
                SnackbarManager.showError(getView(), "Journey is in an undefined state!");
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

            this.googleMap.addMarker(markerOptions);

            // Add end-stop to the map
            markerOptions = new MarkerOptions()
                    .position(new LatLng(endStop.getLatitude(), endStop.getLongitude()))
                    .title(endStop.getName())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.edi_bus_marker))
                    .anchor((float) 0.5, (float) 0.5)
                    .rotation(endStop.getOrientation());

            this.googleMap.addMarker(markerOptions);

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
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker))
                    .anchor((float) 0.5, (float) 0.5);

            this.userMarker = this.googleMap.addMarker(markerOptions);

            // TODO: put in separate method. Detect when user moves the map.

            // Move view over the user's current position
            this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(this.userMarker.getPosition(),15));
        }
    }

    public void onEventMainThread(TrackerStateUpdatedEvent trackerStateUpdatedEvent){

        // If we are near the end, finish the tracking
        if (trackerStateUpdatedEvent.getDistanceFromGoal() < 50){

            this.journeyManager.finishTrip();

            // Automatically upload trip and/or fire events
            if (this.journeyManager.getAutomaticUpload())
            {
                SnackbarManager.showError(getView(), "Uploading the trip...");
            }else
            {
                SnackbarManager.showError(getView(), "Please upload the trip...");
            }
        }

        // Refresh the UI
        this.refreshDataInterface(trackerStateUpdatedEvent);
        this.refreshMap();
    }

    public void onEventMainThread(TripActionFiredEvent tripActionFiredEvent){
        switch (tripActionFiredEvent.getTripActionEnum()){
            case NEW_TRIP:
                this.refreshButtons();
                this.refreshDataInterface(new TrackerStateUpdatedEvent());
                break;
        }
    }

    public void onEventMainThread(TimerUpdatedEvent timerUpdatedEvent){
        if (this.journeyManager.getJourneyState().equals(JourneyStateEnum.RUNNING)){
            this.waitingDurationTextView.setText(this.dateFormat.format(timerUpdatedEvent.getWaitingMilliseconds()));
            this.travellingDuration.setText(this.dateFormat.format(timerUpdatedEvent.getTravellingMilliseconds()));
            this.elapsedTimeTextView.setText(this.dateFormat.format(timerUpdatedEvent.getTravellingMilliseconds() + timerUpdatedEvent.getWaitingMilliseconds()));
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        SnackbarManager.showError(getView(), "Clicked the map!");
    }
}
