package com.marton.edibus.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.inject.Inject;
import com.marton.edibus.R;
import com.marton.edibus.enums.StopTypeEnum;
import com.marton.edibus.models.Stop;
import com.marton.edibus.models.Trip;
import com.marton.edibus.utilities.JourneyManager;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboDialogFragment;
import roboguice.inject.InjectView;


public class StopDialogFragment extends RoboDialogFragment {

    private EventBus eventBus = EventBus.getDefault();

    @Inject
    JourneyManager journeyManager;

    StopTypeEnum stopTypeEnum;

    TextView stopNameTextView;
    TextView stopDistanceTextView;
    Button stopSelectButton;

    public Stop stop;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle("Stop Details");
        View view = inflater.inflate(R.layout.dialog_stop, container, false);

        // Read the type of the stop that the user is about to choose
        Bundle bundle = this.getArguments();
        this.stopTypeEnum = (StopTypeEnum) bundle.get("STOP_TYPE");

        this.stopNameTextView = (TextView) view.findViewById(R.id.stop_name);
        this.stopDistanceTextView = (TextView) view.findViewById(R.id.stop_distance);
        this.stopSelectButton = (Button) view.findViewById(R.id.stop_select);

        this.stop = this.journeyManager.getReviewStop();
        this.stopNameTextView.setText(String.valueOf(this.stop.getName()));
        this.stopDistanceTextView.setText(String.valueOf(this.stop.getDistance()));

        this.stopSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStop(stop);

                // Close down the activity
                Activity currentActivity = getActivity();
                currentActivity.finish();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public int show(FragmentTransaction transaction, String tag){

        return super.show(transaction, tag);
    }

    private void selectStop(Stop stop){
        Trip trip = this.journeyManager.getTrip();
        switch (this.stopTypeEnum){
            case START:
                trip.setStartStop(stop);
                break;
            case END:
                trip.setEndStop(stop);
                break;
        }
        this.journeyManager.setTrip(trip);
    }
}
