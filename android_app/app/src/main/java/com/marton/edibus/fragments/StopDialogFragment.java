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

import roboguice.fragment.RoboDialogFragment;


public class StopDialogFragment extends RoboDialogFragment {

    @Inject
    private JourneyManager journeyManager;

    private StopTypeEnum stopTypeEnum;
    private boolean serviceSelected;

    private Stop stop;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle("Stop Details");
        View view = inflater.inflate(R.layout.dialog_stop, container, false);

        // Read the type of the stop that the user is about to choose
        Bundle bundle = this.getArguments();
        this.stopTypeEnum = (StopTypeEnum) bundle.get("STOP_TYPE");
        this.serviceSelected = (boolean) bundle.get("SERVICE_SELECTED");


        TextView stopNameTextView = (TextView) view.findViewById(R.id.stop_name);
        TextView stopDistanceTextView = (TextView) view.findViewById(R.id.stop_distance);
        Button stopSelectButton = (Button) view.findViewById(R.id.stop_select);

        this.stop = this.journeyManager.getReviewStop();
        stopNameTextView.setText(String.valueOf(this.stop.getName()));
        stopDistanceTextView.setText(String.valueOf(this.stop.getDistance()));

        stopSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStop(stop);
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

                if (this.serviceSelected){

                }else{
                    // Close down the activity
                    Activity currentActivity = getActivity();
                    currentActivity.finish();
                }
                break;
            case END:
                trip.setEndStop(stop);

                // Close down the activity
                Activity currentActivity = getActivity();
                currentActivity.finish();
                break;
        }
        this.journeyManager.setTrip(trip);
    }
}
