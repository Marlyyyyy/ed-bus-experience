package com.marton.edibus.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.inject.Inject;
import com.marton.edibus.R;
import com.marton.edibus.WebCallBack;
import com.marton.edibus.models.Trip;
import com.marton.edibus.services.JourneyManager;

import java.util.Date;

import roboguice.activity.RoboActionBarActivity;

public class JourneyActivity extends RoboActionBarActivity {

    @Inject
    JourneyManager journeyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey);

        Trip trip = journeyManager.getTrip();
        trip.setStartStopId(1);
        trip.setEndStopId(2);
        trip.setEndTime(new Date());
        trip.setSeat(true);
        trip.setServiceId(3);
        trip.setStartTime(new Date());
        trip.setTravelDuration(10000);
        trip.setWaitDuration(10000);trip.setRating((float) 4.5);

        journeyManager.setTrip(trip);
        journeyManager.uploadTrip(new WebCallBack() {
            @Override
            public void onSuccess(Object data) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_journey, menu);
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
