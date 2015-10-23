package com.marton.edibus.activities;

import android.os.Bundle;

import com.marton.edibus.R;
import roboguice.activity.RoboActionBarActivity;

public class JourneyActivity extends RoboActionBarActivity {

    private static final String TAG = JourneyActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey);
    }
}
