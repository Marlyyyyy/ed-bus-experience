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
import com.marton.edibus.events.TripControlEvent;
import com.marton.edibus.models.Stop;
import com.marton.edibus.models.TrackerState;
import com.marton.edibus.services.LocationProviderService;
import com.marton.edibus.utilities.DistanceCalculator;
import com.marton.edibus.utilities.JourneyManager;
import com.marton.edibus.utilities.SnackbarManager;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

public class JourneyTrackerFragment extends RoboFragment {

    private static final String TAG = JourneySetupFragment.class.getName();

    private EventBus eventBus = EventBus.getDefault();

    @Inject private JourneyManager journeyManager;

    @InjectView(R.id.elapsed_time)
    TextView elapsedTimeTextView;

    @InjectView(R.id.remaining_distance)
    TextView remainingDistanceTextView;

    @InjectView(R.id.passed_distance)
    TextView passedDistanceTextView;

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

        Intent locationServiceIntent = new Intent(getActivity(), LocationProviderService.class);
        getActivity().startService(locationServiceIntent);

        // Configure listeners for buttons
        startJourneyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (journeyManager.tripSetupComplete()){
                    journeyManager.startTrip();
                    startJourneyButton.setVisibility(View.GONE);
                    pauseJourneyButton.setVisibility(View.VISIBLE);
                }else{
                    SnackbarManager.showSnackbar(getView(), "error", "Trip needs to be set up first!", getResources());
                }

            }
        });

        pauseJourneyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                journeyManager.pauseTrip();
                pauseJourneyButton.setVisibility(View.GONE);
                continueJourneyButton.setVisibility(View.VISIBLE);
                finishJourneyButton.setVisibility(View.VISIBLE);
            }
        });

        continueJourneyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                journeyManager.continueTrip();
                continueJourneyButton.setVisibility(View.GONE);
                finishJourneyButton.setVisibility(View.GONE);
                pauseJourneyButton.setVisibility(View.VISIBLE);
            }
        });
        
        finishJourneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                journeyManager.finishTrip();
            }
        });
    }

    public void onEventMainThread(TripControlEvent tripControlEvent){

        switch(tripControlEvent.tripControlEnum){
            case START:
                break;
            case PAUSE:
                SnackbarManager.showSnackbar(getView(), "success", "Trip has paused", getResources());
                break;
            case FINISH:
                SnackbarManager.showSnackbar(getView(), "success", "Trip has finished, do something", getResources());
                break;
        }
    }

    public void onEventMainThread(TrackerState trackerState){

        // Calculate the remaining distance
        Stop endStop = this.journeyManager.getTrip().getEndStop();
        double remainingDistance = DistanceCalculator.getDistanceBetweenPoints(
                trackerState.getLatitude(), trackerState.getLongitude(), endStop.getLatitude(), endStop.getLongitude());
        trackerState.setDistanceFromGoal(remainingDistance);

        Stop startStop = this.journeyManager.getTrip().getStartStop();
        double distanceFromStart = DistanceCalculator.getDistanceBetweenPoints(
                trackerState.getLatitude(), trackerState.getLongitude(), startStop.getLatitude(), startStop.getLongitude());
        trackerState.setDistanceFromStart(distanceFromStart);

        // If we are near the end, finish the tracking
        if (remainingDistance < 50){

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
    }
}
