package com.marton.edibus.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

public class DashboardFragment extends RoboFragment {

    private static final String TAG = DashboardFragment.class.getName();

    @Inject
    private JourneyManager journeyManager;

    @InjectView(R.id.start_new_journey)
    private Button newJourneyButton;

    @InjectView(R.id.journeys)
    private TextView journeysTextView;

    @InjectView(R.id.total_distance)
    private TextView totalDistanceTextView;

    @InjectView(R.id.total_time)
    private TextView totalTimeTextView;

    @InjectView(R.id.average_speed)
    private TextView averageSpeedTextView;

    private DecimalFormat decimalFormat;

    private DateFormat dateFormat;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // Set up text view formats
        this.decimalFormat = new DecimalFormat("#.##");
        this.dateFormat = new SimpleDateFormat("m", Locale.UK);
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
        double distance = StatisticsManager.readTotalTravellingDistanceFromSharedPreferences();
        int totalTime = StatisticsManager.readTotalTravellingTimeFromSharedPreferences() + StatisticsManager.readTotalWaitingTimeFromSharedPreferences();
        double averageSpeed = 1000 * distance / (totalTime);

        this.journeysTextView.setText(String.valueOf(journeys));
        this.totalDistanceTextView.setText(String.valueOf(this.decimalFormat.format(distance)) + " m");
        this.totalTimeTextView.setText(String.valueOf(this.dateFormat.format(totalTime)) + " minutes");
        this.averageSpeedTextView.setText(String.valueOf(this.decimalFormat.format(averageSpeed)) + " km/h");
    }
}
