package com.marton.edibus.fragments;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.inject.Inject;
import com.marton.edibus.R;
import com.marton.edibus.WebCallBack;
import com.marton.edibus.enums.RideStateEnum;
import com.marton.edibus.enums.JourneyStateEnum;
import com.marton.edibus.enums.RideActionEnum;
import com.marton.edibus.events.JourneyUploadRequestedEvent;
import com.marton.edibus.events.RideFinishedEvent;
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
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;


public class JourneyTrackerFragment extends RoboFragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private EventBus eventBus = EventBus.getDefault();

    private Intent locationProviderService;

    private Intent locationProcessorService;

    private GoogleMap googleMap;

    private Polyline userRoutePolyline;

    private Marker startStopMarker;

    private Marker endStopMarker;

    private boolean cameraCenteredOnUser;

    private RideActionFiredEvent rideActionFiredEvent;

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

                eventBus.post(new RideActionFiredEvent(RideActionEnum.NEW_RIDE_STARTED));
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

                // Move on to the feedback page
                rideActionFiredEvent.setRideActionEnum(RideActionEnum.TRAVELLING_STARTED);
                eventBus.post(rideActionFiredEvent);

                journeyManager.startTravelling();
                refreshButtons();
            }
        });

        this.finishRideButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                journeyManager.finishRide();
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

        this.refreshStopMarkers();
    }

    @Override
    public void onPause(){
        super.onPause();

        // Register as a subscriber
        this.eventBus.unregister(this);
    }

    @Override
    public void onDestroy(){

        // Stop the location services
        this.getActivity().stopService(this.locationProviderService);
        this.getActivity().stopService(this.locationProcessorService);
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.googleMap = googleMap;
        this.googleMap.setMyLocationEnabled(true);

        this.refreshStopMarkers();

        this.userRoutePolyline = this.googleMap.addPolyline(new PolylineOptions().width(5).color(Color.BLUE));
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
                super.onFailure(statusCode, response);

                SnackbarManager.showError(getView(), String.format("Journey upload has failed, status code: %d!", statusCode));
                progressDialog.dismiss();
            }
        };

        this.journeyManager.saveRide(saveRideCallback);
    }

    // Sets all tracking related text-fields
    private void refreshDataInterface(TrackerStateUpdatedEvent trackerStateUpdatedEvent){

        this.currentActivity.setText(String.valueOf(this.journeyManager.getRideStateEnum()));
        this.remainingDistanceTextView.setText(this.decimalFormat.format(trackerStateUpdatedEvent.getDistanceFromGoal()) + "m");
        this.travelledDistanceTextView.setText(this.decimalFormat.format(trackerStateUpdatedEvent.getDistanceFromStart()) + "m");
        this.averageSpeedTextView.setText(this.decimalFormat.format(trackerStateUpdatedEvent.getAverageSpeed() * 3.6) + "km/h");
        this.maximumSpeedTextView.setText(this.decimalFormat.format(trackerStateUpdatedEvent.getMaximumSpeed() * 3.6) + "km/h");
    }

    // Sets all timer related text-fields
    private void refreshTimerInterface(TimerUpdatedEvent timerUpdatedEvent){

        this.waitingDurationTextView.setText(this.dateFormat.format(timerUpdatedEvent.getWaitingMilliseconds()));
        this.travellingDuration.setText(this.dateFormat.format(timerUpdatedEvent.getTravellingMilliseconds()));
        this.elapsedTimeTextView.setText(this.dateFormat.format(timerUpdatedEvent.getTravellingMilliseconds() + timerUpdatedEvent.getWaitingMilliseconds()));
    }

    // Redraws all stop markers on the map
    private void refreshStopMarkers(){

        Ride ride = this.journeyManager.getRide();
        Stop startStop = ride.getStartStop();
        Stop endStop = ride.getEndStop();

        if (this.googleMap != null){

            // Clear stop markers
            if (this.startStopMarker != null){
                this.startStopMarker.remove();
            }

            if (this.endStopMarker != null){
                this.endStopMarker.remove();
            }

            MarkerOptions markerOptions = new MarkerOptions();

            // Add start-stop to the map
            if(startStop != null){
                markerOptions.position(new LatLng(startStop.getLatitude(), startStop.getLongitude()))
                        .title(startStop.getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.edi_bus_marker))
                        .anchor((float) 0.5, (float) 0.5)
                        .rotation(startStop.getOrientation());

                this.startStopMarker = this.googleMap.addMarker(markerOptions);
            }

            // Add end-stop to the map
            if (endStop != null){
                markerOptions = new MarkerOptions()
                        .position(new LatLng(endStop.getLatitude(), endStop.getLongitude()))
                        .title(endStop.getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.edi_bus_marker))
                        .anchor((float) 0.5, (float) 0.5)
                        .rotation(endStop.getOrientation());

                this.endStopMarker = this.googleMap.addMarker(markerOptions);
            }
        }
    }

    // Hides and displays buttons according to the current state of the journey
    private void refreshButtons(){

        JourneyStateEnum journeyStateEnum = this.journeyManager.getJourneyStateEnum();
        RideStateEnum rideStateEnum = this.journeyManager.getRideStateEnum();

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
                switch(rideStateEnum){
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

    private void registerNewUserPosition(TrackerStateUpdatedEvent trackerStateUpdatedEvent){

        double userLatitude = trackerStateUpdatedEvent.getLatitude();
        double userLongitude = trackerStateUpdatedEvent.getLongitude();

        if (userLatitude == 0.0 || userLongitude == 0.0) {
            return;
        }

        // Move view over the user's current position
        if (!this.cameraCenteredOnUser) {
            this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLatitude, userLongitude), 15));
            this.cameraCenteredOnUser = true;
        }

        // Add new coordinate points to the existing polyline, only if they're different from the last added one
        List<LatLng> userRoutePoints = this.userRoutePolyline.getPoints();

        if (userRoutePoints.size() > 0){
            LatLng lastPoint = userRoutePoints.get(userRoutePoints.size()-1);
            if (lastPoint.latitude != userLatitude || lastPoint.longitude != userLongitude){
                userRoutePoints.add(new LatLng(userLatitude, userLongitude));
                this.userRoutePolyline.setPoints(userRoutePoints);
            }
        }else{
            userRoutePoints.add(new LatLng(userLatitude, userLongitude));
            this.userRoutePolyline.setPoints(userRoutePoints);
        }
    }

    public void onEventMainThread(TrackerStateUpdatedEvent trackerStateUpdatedEvent){

        // Refresh the UI
        this.registerNewUserPosition(trackerStateUpdatedEvent);
        this.refreshDataInterface(trackerStateUpdatedEvent);
    }

    public void onEventMainThread(RideActionFiredEvent rideActionFiredEvent){

        switch (rideActionFiredEvent.getRideActionEnum()){
            case NEW_RIDE_STARTED:
                this.refreshButtons();
                this.refreshDataInterface(new TrackerStateUpdatedEvent());
                this.refreshTimerInterface(new TimerUpdatedEvent());
                this.refreshStopMarkers();
                break;
            case WAITING_STARTED:
                this.refreshStopMarkers();
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

    public void onEvent(RideFinishedEvent rideFinishedEvent){

        // Stop the services
        getActivity().stopService(locationProviderService);
        getActivity().stopService(locationProcessorService);
    }

    @Override
    public void onMapClick(LatLng latLng) {
    }
}
