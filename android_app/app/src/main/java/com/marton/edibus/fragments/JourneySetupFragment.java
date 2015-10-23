package com.marton.edibus.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.inject.Inject;
import com.marton.edibus.R;
import com.marton.edibus.activities.StopActivity;
import com.marton.edibus.enums.StopEnum;
import com.marton.edibus.events.JourneyUpdateEvent;
import com.marton.edibus.events.MessageEvent;
import com.marton.edibus.models.Service;
import com.marton.edibus.models.Stop;
import com.marton.edibus.utilities.JourneyManager;
import com.marton.edibus.utilities.SnackbarManager;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;


public class JourneySetupFragment extends RoboFragment{

    private static final String TAG = JourneySetupFragment.class.getName();

    private EventBus eventBus = EventBus.getDefault();

    ServiceDialogFragment serviceDialog;

    @Inject
    JourneyManager journeyManager;

    @InjectView(R.id.choose_start_stop)
    Button startStopButton;

    @InjectView(R.id.choose_service)
    Button serviceButton;

    @InjectView(R.id.choose_end_stop)
    Button endStopButton;

    @InjectView(R.id.journey_root)
    View rootView;

    @InjectView(R.id.journey_start_stop)
    TextView journeyStartStopTextView;

    @InjectView(R.id.journey_service)
    TextView journeyServiceTextView;

    @InjectView(R.id.journey_end_stop)
    TextView journeyEndStopTextView;

    FragmentManager fragmentManager;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.fragment_journey_setup,container,false);

        fragmentManager = getFragmentManager();

        // Register as a subscriber
        eventBus.register(this);

        // Create Service selector dialog
        serviceDialog = new ServiceDialogFragment();

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configure listeners for buttons
        startStopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                launchStopChooserActivity(StopEnum.START);
            }
        });

        serviceButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                serviceDialog.show(fragmentManager, "Service Dialog Fragment");
            }
        });

        endStopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                launchStopChooserActivity(StopEnum.END);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();

        SnackbarManager.showSnackbar(rootView, "success", "Resumed", this.getResources());

        this.refreshUserInterface();
    }

    private void launchStopChooserActivity(StopEnum stop){
        Log.d(TAG, "Choosing new start stop");

        Intent intent = new Intent(getActivity(), StopActivity.class);
        intent.putExtra("STOP", stop);
        startActivity(intent);
    }

    // Takes care of refreshing the display of the current journey configuration
    private void refreshUserInterface(){
        Stop currentStartStop = journeyManager.getTrip().getStartStop();
        if (currentStartStop != null){
            journeyStartStopTextView.setText(String.valueOf(currentStartStop.getId()));
        }

        Stop currentEndStop = journeyManager.getTrip().getEndStop();
        if (currentEndStop != null){
            journeyEndStopTextView.setText(String.valueOf(currentEndStop.getId()));
        }

        Service currentService = journeyManager.getTrip().getService();
        if (currentService != null){
            journeyServiceTextView.setText(String.valueOf(currentService.getId()));
        }
    }

    public void onEvent(MessageEvent event){
        Log.d(TAG, "JourneyActivity has received the message event!");
    }

    public void onEvent(JourneyUpdateEvent event){
        refreshUserInterface();
    }
}
