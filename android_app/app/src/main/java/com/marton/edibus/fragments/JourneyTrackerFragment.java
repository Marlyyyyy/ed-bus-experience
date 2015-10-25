package com.marton.edibus.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.marton.edibus.R;
import com.marton.edibus.enums.JourneyControlEnum;
import com.marton.edibus.events.JourneyControlEvent;
import com.marton.edibus.events.MessageEvent;
import com.marton.edibus.services.LocationService;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

public class JourneyTrackerFragment extends RoboFragment {

    private static final String TAG = JourneySetupFragment.class.getName();

    private EventBus eventBus = EventBus.getDefault();

    @InjectView(R.id.elapsed_time)
    TextView elapsedTimeTextView;

    @InjectView(R.id.start_journey)
    Button startJourneyButton;

    @InjectView(R.id.pause_journey)
    Button pauseJourneyButton;

    @InjectView(R.id.continue_journey)
    Button continueJourneyButton;

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

        Intent locationServiceIntent = new Intent(getActivity(), LocationService.class);
        getActivity().startService(locationServiceIntent);

        // Configure listeners for buttons
        startJourneyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                eventBus.post(new JourneyControlEvent(JourneyControlEnum.START));
                startJourneyButton.setVisibility(View.GONE);
                pauseJourneyButton.setVisibility(View.VISIBLE);
            }
        });

        pauseJourneyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                eventBus.post(new JourneyControlEvent(JourneyControlEnum.PAUSE));
                pauseJourneyButton.setVisibility(View.GONE);
                continueJourneyButton.setVisibility(View.VISIBLE);
            }
        });

        continueJourneyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                eventBus.post(new JourneyControlEvent(JourneyControlEnum.START));
                continueJourneyButton.setVisibility(View.GONE);
                pauseJourneyButton.setVisibility(View.VISIBLE);
            }
        });
    }

    public void onEventMainThread(MessageEvent event){

        elapsedTimeTextView.setText(event.message);
    }
}
