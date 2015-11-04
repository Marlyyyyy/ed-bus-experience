package com.marton.edibus.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.inject.Inject;
import com.marton.edibus.R;
import com.marton.edibus.events.TrackerStateUpdatedEvent;
import com.marton.edibus.services.LocationProcessorService;
import com.marton.edibus.services.LocationProviderService;
import com.marton.edibus.utilities.JourneyManager;
import com.marton.edibus.utilities.SnackbarManager;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

public class JourneyTrackerFragment extends RoboFragment {

    private static final String TAG = JourneySetupFragment.class.getName();

    private EventBus eventBus = EventBus.getDefault();

    private Intent locationProviderService;

    private Intent locationProcessorService;

    @Inject private JourneyManager journeyManager;

    @InjectView(R.id.travelled_distance)
    TextView travelledDistanceTextView;

    @InjectView(R.id.remaining_distance)
    TextView remainingDistanceTextView;

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
        eventBus.register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_journey_tracker,container,false);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.locationProviderService = new Intent(getActivity(), LocationProviderService.class);
        this.locationProcessorService = new Intent(getActivity(), LocationProcessorService.class);


        // Set visibility of specific views
        this.refreshUserInterface();

        // Configure listeners for buttons
        startJourneyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (journeyManager.tripSetupComplete()) {
                    journeyManager.startTrip();
                    refreshUserInterface();

                    getActivity().startService(locationProviderService);
                    getActivity().startService(locationProcessorService);
                } else {
                    SnackbarManager.showSnackbar(getView(), "error", "Trip needs to be set up first!", getResources());
                }
            }
        });

        pauseJourneyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                journeyManager.pauseTrip();
                refreshUserInterface();
            }
        });

        continueJourneyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                journeyManager.continueTrip();
                refreshUserInterface();
            }
        });
        
        finishJourneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                journeyManager.finishTrip();
                refreshUserInterface();
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

    public void onEventMainThread(TrackerStateUpdatedEvent trackerState){

        // If we are near the end, finish the tracking
        if (trackerState.getDistanceFromGoal() < 50){

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

        // Update the UI
        this.remainingDistanceTextView.setText(String.valueOf(trackerState.getDistanceFromGoal()));
        this.travelledDistanceTextView.setText(String.valueOf(trackerState.getDistanceFromStart()));
    }

    private void refreshUserInterface(){

        switch (this.journeyManager.getJourneyState()){
            case NOT_STARTED:
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
}
