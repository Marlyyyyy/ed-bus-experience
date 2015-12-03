package com.marton.edibus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.inject.Inject;
import com.marton.edibus.R;
import com.marton.edibus.utilities.JourneyManager;
import com.marton.edibus.utilities.StatisticsManager;

import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;

public class ContentActivity extends RoboActionBarActivity {

    private static final String TAG = ContentActivity.class.getName();

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

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

    private void refreshUserInterface(){

        // Fill in statistics
        int journeys = StatisticsManager.readJourneysFromSharedPreferences();
        int totalWaitingTime = StatisticsManager.readTotalWaitingTimeFromSharedPreferences();
        int totalTravellingTime = StatisticsManager.readTotalTravellingTimeFromSharedPreferences();

        this.journeysTextView.setText(String.valueOf(journeys));
        this.totalWaitingTimeTextView.setText(String.valueOf(totalWaitingTime));
        this.totalTravellingTimeTextView.setText(String.valueOf(totalTravellingTime));
    }

    public void startNewJourney(){
        Log.d(TAG, "Start new journey");

        Intent intent = new Intent(this, JourneyActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_content, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
