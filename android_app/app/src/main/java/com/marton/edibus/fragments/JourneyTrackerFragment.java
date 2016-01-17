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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.inject.Inject;
import com.marton.edibus.R;
import com.marton.edibus.WebCallBack;
import com.marton.edibus.enums.CurrentActivityEnum;
import com.marton.edibus.enums.JourneyStateEnum;
import com.marton.edibus.enums.RideActionEnum;
import com.marton.edibus.events.CurrentActivityUpdatedEvent;
import com.marton.edibus.events.JourneyUploadRequestedEvent;
import com.marton.edibus.events.TimerUpdatedEvent;
import com.marton.edibus.events.TrackerStateUpdatedEvent;
import com.marton.edibus.events.RideActionFiredEvent;
import com.marton.edibus.models.Stop;
import com.marton.edibus.models.Ride;
import com.marton.edibus.services.LocationProcessorService;
import com.marton.edibus.services.LocationProviderService;
import com.marton.edibus.utilities.JourneyManager;
import com.marton.edibus.utilities.SnackbarManager;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;


public class JourneyTrackerFragment extends RoboFragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private EventBus eventBus = EventBus.getDefault();

    private Intent locationProviderService;

    private Intent locationProcessorService;

    private double latestUserLatitude;

    private double latestUserLongitude;

    private GoogleMap googleMap;

    private boolean cameraCenteredOnUser;

    private RideActionFiredEvent rideActionFiredEvent;

    private AlertDialog deleteJourneyDialog;

    private AlertDialog continueJourneyDialog;

    private DecimalFormat decimalFormat;

    private DateFormat dateFormat;

    private CurrentActivityUpdatedEvent currentActivityUpdatedEvent;

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

    @InjectView(R.id.start_waiting)
    Button startWaitingButton;

    @InjectView(R.id.board_the_bus)
    Button boardTheBusButton;

    @InjectView(R.id.finish_ride)
    Button finishRideButton;

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

        // Initialise event objects
        this.rideActionFiredEvent = new RideActionFiredEvent();
        this.currentActivityUpdatedEvent = new CurrentActivityUpdatedEvent();

        // Set up text view formats
        this.decimalFormat = new DecimalFormat("#.##");
        this.dateFormat = new SimpleDateFormat("mm:ss", Locale.UK);

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
        builder.setTitle("Add another ride?");
        builder.setPositiveButton("Yes, let's continue", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                eventBus.post(new RideActionFiredEvent(RideActionEnum.NEW_RIDE));
            }
        });

        builder.setNegativeButton("No, I'm finished", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                journeyManager.setDefaults();
                getActivity().finish();
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
        this.startWaitingButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (journeyManager.rideSetupComplete()) {

                    journeyManager.startWaiting();
                    refreshButtons();

                    // Move on to the feedback page
                    rideActionFiredEvent.setRideActionEnum(RideActionEnum.RIDE_STARTED);
                    eventBus.post(rideActionFiredEvent);

                    // Fire update event for current activity
                    currentActivityUpdatedEvent.setCurrentActivityEnum(CurrentActivityEnum.WAITING);
                    eventBus.post(currentActivityUpdatedEvent);

                    // Start the services
                    getActivity().startService(locationProviderService);
                    getActivity().startService(locationProcessorService);

                } else {
                    SnackbarManager.showError(getView(), "Ride needs to be set up first!");
                }
            }
        });

        this.boardTheBusButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                journeyManager.startTravelling();
                refreshButtons();

                // Fire update event for current activity
                currentActivityUpdatedEvent.setCurrentActivityEnum(CurrentActivityEnum.TRAVELLING);
                eventBus.post(currentActivityUpdatedEvent);
            }
        });

        this.finishRideButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                journeyManager.finishRide();

                // Stop the services
                getActivity().stopService(locationProviderService);
                getActivity().stopService(locationProcessorService);

                refreshButtons();
            }
        });

        this.uploadJourneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uploadJourney();
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
        this.refreshMap();
    }

    private void uploadJourney(){

        final ProgressDialog progressDialog = new ProgressDialog(getActivity(), R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        WebCallBack<Integer> saveRideCallback = new WebCallBack<Integer>() {
            @Override
            public void onSuccess(Integer data) {
                Stop previousEndStop = journeyManager.getRide().getEndStop();
                Ride ride = new Ride();
                ride.setStartStop(previousEndStop);
                ride.setJourneyId(data);

                journeyManager.setDefaults();
                journeyManager.setRide(ride);

                continueJourneyDialog.show();
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, JSONObject response) {
                SnackbarManager.showError(getView(), String.format("Journey upload has failed, status code: %d!", statusCode));

                progressDialog.dismiss();
            }
        };

        this.journeyManager.saveRide(saveRideCallback);
    }

    // Sets all tracking related text-fields
    private void refreshDataInterface(TrackerStateUpdatedEvent trackerStateUpdatedEvent){

        this.currentActivity.setText(String.valueOf(this.journeyManager.getCurrentActivityEnum()));
        this.remainingDistanceTextView.setText(this.decimalFormat.format(trackerStateUpdatedEvent.getDistanceFromGoal()) + "m");
        this.travelledDistanceTextView.setText(this.decimalFormat.format(trackerStateUpdatedEvent.getDistanceFromStart()) + "m");
        this.averageSpeedTextView.setText(this.decimalFormat.format(trackerStateUpdatedEvent.getAverageSpeed() * 3.6) + "km/h");
        this.maximumSpeedTextView.setText(this.decimalFormat.format(trackerStateUpdatedEvent.getMaximumSpeed() * 3.6) + "km/h");

        this.latestUserLatitude = trackerStateUpdatedEvent.getLatitude();
        this.latestUserLongitude = trackerStateUpdatedEvent.getLongitude();
    }

    // Sets all timer related text-fields
    private void refreshTimerInterface(TimerUpdatedEvent timerUpdatedEvent){

        this.waitingDurationTextView.setText(this.dateFormat.format(timerUpdatedEvent.getWaitingMilliseconds()));
        this.travellingDuration.setText(this.dateFormat.format(timerUpdatedEvent.getTravellingMilliseconds()));
        this.elapsedTimeTextView.setText(this.dateFormat.format(timerUpdatedEvent.getTravellingMilliseconds() + timerUpdatedEvent.getWaitingMilliseconds()));
    }

    // Hides and displays buttons according to the current state of the journey
    private void refreshButtons(){

        CurrentActivityEnum currentActivityEnum = this.journeyManager.getCurrentActivityEnum();
        JourneyStateEnum journeyStateEnum = this.journeyManager.getJourneyStateEnum();

        switch (journeyStateEnum){
            case SETUP_INCOMPLETE:
                this.startWaitingButton.setVisibility(View.VISIBLE);
                this.boardTheBusButton.setVisibility(View.GONE);
                this.finishRideButton.setVisibility(View.GONE);
                this.uploadJourneyButton.setVisibility(View.GONE);
                this.deleteJourneyButton.setVisibility(View.GONE);
                break;

            case READY_TO_START:
                this.startWaitingButton.setVisibility(View.VISIBLE);
                this.boardTheBusButton.setVisibility(View.GONE);
                this.finishRideButton.setVisibility(View.GONE);
                this.uploadJourneyButton.setVisibility(View.GONE);
                this.deleteJourneyButton.setVisibility(View.GONE);
                break;

            case RUNNING:
                switch(currentActivityEnum){
                    case PREPARING:
                        this.startWaitingButton.setVisibility(View.VISIBLE);
                        this.boardTheBusButton.setVisibility(View.GONE);
                        this.finishRideButton.setVisibility(View.GONE);
                        this.uploadJourneyButton.setVisibility(View.GONE);
                        this.deleteJourneyButton.setVisibility(View.GONE);
                        break;

                    case WAITING:
                        this.startWaitingButton.setVisibility(View.GONE);
                        this.boardTheBusButton.setVisibility(View.VISIBLE);
                        this.finishRideButton.setVisibility(View.GONE);
                        this.uploadJourneyButton.setVisibility(View.GONE);
                        this.deleteJourneyButton.setVisibility(View.GONE);
                        break;

                    case TRAVELLING:
                        this.startWaitingButton.setVisibility(View.GONE);
                        this.boardTheBusButton.setVisibility(View.GONE);
                        this.finishRideButton.setVisibility(View.VISIBLE);
                        this.uploadJourneyButton.setVisibility(View.GONE);
                        this.deleteJourneyButton.setVisibility(View.GONE);
                        break;
                }
                break;

            case FINISHED:
                this.startWaitingButton.setVisibility(View.GONE);
                this.boardTheBusButton.setVisibility(View.GONE);
                this.finishRideButton.setVisibility(View.GONE);
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

        Ride ride = this.journeyManager.getRide();
        Stop startStop = ride.getStartStop();
        Stop endStop = ride.getEndStop();

        if (this.googleMap != null){

            // Clear all markers
            this.googleMap.clear();

            MarkerOptions markerOptions = new MarkerOptions();

            // Add start-stop to the map
            if(startStop != null){
                        markerOptions.position(new LatLng(startStop.getLatitude(), startStop.getLongitude()))
                        .title(startStop.getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.edi_bus_marker))
                        .anchor((float) 0.5, (float) 0.5)
                        .rotation(startStop.getOrientation());

                this.googleMap.addMarker(markerOptions);
            }

            // Add end-stop to the map
            if (endStop != null){
                markerOptions = new MarkerOptions()
                        .position(new LatLng(endStop.getLatitude(), endStop.getLongitude()))
                        .title(endStop.getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.edi_bus_marker))
                        .anchor((float) 0.5, (float) 0.5)
                        .rotation(endStop.getOrientation());

                this.googleMap.addMarker(markerOptions);
            }

            // Add user to the map
            if (endStop != null && startStop != null){
                // TODO: remove this all
                if (this.latestUserLatitude == 0.0){
                    this.latestUserLatitude = startStop.getLatitude();
                }

                if (this.latestUserLongitude == 0.0){
                    this.latestUserLongitude = startStop.getLongitude();
                }

                // Move view over the user's current position
                if (!this.cameraCenteredOnUser){
                    this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(this.latestUserLatitude, this.latestUserLongitude),15));
                    this.cameraCenteredOnUser = true;
                }
            }
        }
    }

    public void onEventMainThread(TrackerStateUpdatedEvent trackerStateUpdatedEvent){

        // TODO: put this in the service?
        // If we are near the end, finish the tracking
        if (trackerStateUpdatedEvent.getDistanceFromGoal() < 50){

            this.journeyManager.finishRide();

            // Automatically upload trip and/or fire events
            if (this.journeyManager.getAutomaticFlow())
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

    public void onEventMainThread(RideActionFiredEvent rideActionFiredEvent){

        switch (rideActionFiredEvent.getRideActionEnum()){
            case NEW_RIDE:
                this.refreshButtons();
                this.refreshDataInterface(new TrackerStateUpdatedEvent());
                this.refreshTimerInterface(new TimerUpdatedEvent());
                this.refreshMap();
                break;
        }
    }

    public void onEventMainThread(TimerUpdatedEvent timerUpdatedEvent){

        if (this.journeyManager.getJourneyStateEnum().equals(JourneyStateEnum.RUNNING)){
            this.refreshTimerInterface(timerUpdatedEvent);
        }
    }

    public void onEvent(JourneyUploadRequestedEvent journeyUploadRequestedEvent){

        this.uploadJourney();
    }

    @Override
    public void onMapClick(LatLng latLng) {
    }
}
