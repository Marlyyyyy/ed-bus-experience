package com.marton.edibus.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.inject.Inject;
import com.marton.edibus.R;
import com.marton.edibus.activities.JourneyActivity;
import com.marton.edibus.utilities.JourneyManager;
import com.marton.edibus.utilities.StatisticsManager;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

public class DashboardFragment extends RoboFragment {

    private static final String TAG = DashboardFragment.class.getName();

    private EventBus eventBus = EventBus.getDefault();

    @Inject
    private JourneyManager journeyManager;

    @InjectView(R.id.start_new_journey)
    private Button newJourneyButton;

    @InjectView(R.id.journeys)
    private TextView journeysTextView;

    @InjectView(R.id.total_waiting_time)
    private TextView totalWaitingTimeTextView;

    @InjectView(R.id.total_travelling_time)
    private TextView totalTravellingTimeTextView;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard,container,false);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.newJourneyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                journeyManager.setDefaults();
                startNewJourney();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        this.refreshUserInterface();
    }

    public void startNewJourney(){
        Log.d(TAG, "Start new journey");

        Intent intent = new Intent(this.getActivity(), JourneyActivity.class);
        startActivity(intent);
    }

    private void refreshUserInterface(){

        // Fill in statistics
        int journeys = StatisticsManager.readJourneysFromSharedPreferences();
        int totalWaitingTime = StatisticsManager.readTotalWaitingTimeFromSharedPreferences();
        int totalTravellingTime = StatisticsManager.readTotalTravellingTimeFromSharedPreferences();

        this.journeysTextView.setText(String.valueOf(journeys));
        this.totalWaitingTimeTextView.setText(String.valueOf(totalWaitingTime));
        this.totalTravellingTimeTextView.setText(String.valueOf(totalTravellingTime));
    }
}
