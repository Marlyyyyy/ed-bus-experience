package com.marton.edibus.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.inject.Inject;
import com.marton.edibus.R;
import com.marton.edibus.activities.StopSetupActivity;
import com.marton.edibus.enums.StopTypeEnum;
import com.marton.edibus.enums.RideActionEnum;
import com.marton.edibus.events.JourneySetupUpdatedEvent;
import com.marton.edibus.events.RideActionFiredEvent;
import com.marton.edibus.models.Service;
import com.marton.edibus.models.Stop;
import com.marton.edibus.utilities.JourneyManager;
import com.marton.edibus.utilities.SnackbarManager;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;


public class JourneySetupFragment extends RoboFragment{

    private EventBus eventBus = EventBus.getDefault();

    private ServiceDialogFragment serviceDialog;

    private RideActionFiredEvent rideActionFiredEvent;

    @Inject
    private JourneyManager journeyManager;

    @InjectView(R.id.choose_start_stop)
    private LinearLayout startStopLayout;

    @InjectView(R.id.choose_service)
    private LinearLayout serviceLayout;

    @InjectView(R.id.choose_end_stop)
    private LinearLayout endStopLayout;

    @InjectView(R.id.journey_root)
    private View rootView;

    @InjectView(R.id.journey_start_stop)
    private TextView journeyStartStopTextView;

    @InjectView(R.id.journey_service)
    private TextView journeyServiceTextView;

    @InjectView(R.id.journey_end_stop)
    private TextView journeyEndStopTextView;

    @InjectView(R.id.auto_journey_switch)
    private Switch automaticJourneyHandlerSwitch;

    @InjectView(R.id.journey_setup_complete)
    private Button continueButton;

    private FragmentManager fragmentManager;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);

        this.fragmentManager = getFragmentManager();

        // Create Service selector dialog
        this.serviceDialog = new ServiceDialogFragment();

        // Initialise event objects
        this.rideActionFiredEvent = new RideActionFiredEvent();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_journey_setup,container,false);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configure listeners for buttons and switches
        this.startStopLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                launchStopChooserActivity(StopTypeEnum.START);
            }
        });

        this.serviceLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (journeyManager.getRide().getStartStop() != null){
                    serviceDialog.show(fragmentManager, "Service Dialog Fragment");
                }else{
                    SnackbarManager.showError(v, "Please select a start stop first!");
                }
            }
        });

        this.endStopLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (journeyManager.getRide().getService() != null) {
                    launchStopChooserActivity(StopTypeEnum.END);
                } else {
                    SnackbarManager.showError(v, "Please select a service first!");
                }
            }
        });

        this.automaticJourneyHandlerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                journeyManager.setAutomaticFlow(isChecked);
            }
        });

        this.continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (journeyManager.rideSetupComplete()) {
                    rideActionFiredEvent.setRideActionEnum(RideActionEnum.SETUP_COMPLETED);
                    eventBus.post(rideActionFiredEvent);
                } else {
                    SnackbarManager.showError(rootView, "Setup is incomplete.");
                }
            }
        });

        this.refreshUserInterface();
    }

    @Override
    public void onResume(){
        super.onResume();

        // Register as a subscriber
        this.eventBus.register(this);

        this.refreshUserInterface();
    }

    @Override
    public void onPause(){
        super.onPause();

        // Register as a subscriber
        this.eventBus.unregister(this);
    }

    // Starts a new activity responsible for the stop selection
    private void launchStopChooserActivity(StopTypeEnum stop){

        Intent intent = new Intent(getActivity(), StopSetupActivity.class);
        intent.putExtra("STOP", stop);
        this.startActivity(intent);
    }

    // Refreshes the display of the current journey configuration
    private void refreshUserInterface(){

        // Start Stop button
        Stop currentStartStop = this.journeyManager.getRide().getStartStop();
        if (currentStartStop != null){
            this.journeyStartStopTextView.setText(String.valueOf(currentStartStop.getName()));
        }else{
            this.journeyStartStopTextView.setText("None");
        }

        // Service button
        Service currentService = this.journeyManager.getRide().getService();
        if (currentService != null) {
            this.journeyServiceTextView.setText(String.valueOf(currentService.getName()));
            this.serviceLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.ColorPrimaryLight));
        } else if (currentStartStop != null){
            this.journeyServiceTextView.setText("None");
            this.serviceLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.ColorPrimaryLight));
        }else{
            this.journeyServiceTextView.setText("None");
            this.serviceLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.ColorPrimaryUnavailable));
        }

        // End Stop button
        Stop currentEndStop = this.journeyManager.getRide().getEndStop();
        if (currentEndStop != null) {
            this.journeyEndStopTextView.setText(String.valueOf(currentEndStop.getName()));
            this.endStopLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.ColorPrimaryLight));
        }else if (currentService != null && currentStartStop != null ){
            this.journeyEndStopTextView.setText("None");
            this.endStopLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.ColorPrimaryLight));
        }else{
            this.journeyEndStopTextView.setText("None");
            this.endStopLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.ColorPrimaryUnavailable));
        }

        // Continue button
        if (this.journeyManager.rideSetupComplete()){
            this.continueButton.setEnabled(true);
            this.continueButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.ColorPrimary));
        }else{
            this.continueButton.setEnabled(false);
            this.continueButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.ColorPrimaryUnavailable));
        }
    }

    public void onEvent(JourneySetupUpdatedEvent event){
        this.refreshUserInterface();
    }

    public void onEventMainThread(RideActionFiredEvent rideActionFiredEvent){
        switch (rideActionFiredEvent.getRideActionEnum()){
            case NEW_RIDE_STARTED:
                this.refreshUserInterface();
                break;
        }
    }
}
