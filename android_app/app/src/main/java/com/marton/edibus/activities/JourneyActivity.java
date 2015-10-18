package com.marton.edibus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.inject.Inject;
import com.marton.edibus.R;
import com.marton.edibus.adapters.StopAdapter;
import com.marton.edibus.events.MessageEvent;
import com.marton.edibus.network.BusWebService;
import com.marton.edibus.services.JourneyManager;

import de.greenrobot.event.EventBus;
import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;

public class JourneyActivity extends RoboActionBarActivity {

    private static final String TAG = ContentActivity.class.getName();

    private EventBus eventBus = EventBus.getDefault();

    @Inject
    JourneyManager journeyManager;

    @Inject
    BusWebService busWebService;

    @InjectView(R.id.choose_start_stop)
    Button startStopButton;

    private StopAdapter stopAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey);

        // Register as a subscriber
        eventBus.register(this);

        startStopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startNewStopChooser();
            }
        });
    }

    public void startNewStopChooser(){
        Log.d(TAG, "Choosing new stop");

        Intent intent = new Intent(this, StopActivity.class);
        startActivity(intent);
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

    public void onEvent(MessageEvent event){
        Log.d(TAG, "JourneyActivity has received the message event!");
    }

    @Override
    protected void onDestroy() {
        // Unregister
        eventBus.unregister(this);
        super.onDestroy();
    }
}
