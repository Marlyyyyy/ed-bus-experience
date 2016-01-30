package com.marton.edibus.journey.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.marton.edibus.R;
import com.marton.edibus.journey.activities.StopSetupActivity;
import com.marton.edibus.journey.adapters.ServiceAdapter;
import com.marton.edibus.journey.enums.StopTypeEnum;
import com.marton.edibus.journey.utilities.JourneyManager;
import com.marton.edibus.shared.models.Ride;
import com.marton.edibus.shared.models.Service;
import com.marton.edibus.shared.models.Stop;

import java.text.DecimalFormat;
import java.util.ArrayList;

import roboguice.fragment.RoboDialogFragment;
import roboguice.inject.InjectView;


public class StopDialogFragment extends RoboDialogFragment {

    @Inject
    private JourneyManager journeyManager;

    @InjectView(R.id.stop_title)
    private TextView stopTitleTextView;

    @InjectView(R.id.stop_distance)
    private TextView stopDistanceTextView;

    @InjectView(R.id.cancel)
    private Button cancelButton;

    @InjectView(R.id.select)
    private Button selectButton;

    @InjectView(R.id.service_list_view)
    private ListView servicesListView;

    @InjectView(R.id.services_layout)
    private LinearLayout servicesLayout;

    @InjectView(R.id.service_list_view)
    private ListView serviceListView;

    private StopTypeEnum stopTypeEnum;

    private DecimalFormat decimalFormat;

    private Stop stop;

    // The list of services belonging to the stop on display
    private ArrayList<Service> services;

    private Service selectedService;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);

        // Set up text view formats
        this.decimalFormat = new DecimalFormat(".##");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_stop, container, false);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        // Read the type of the stop that the user is about to choose
        Bundle bundle = this.getArguments();
        this.stopTypeEnum = (StopTypeEnum) bundle.get("STOP_TYPE");
        this.stop = this.journeyManager.getReviewStop();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.stopTitleTextView.setText(String.valueOf(this.stop.getName()));
        this.stopDistanceTextView.setText(String.valueOf(this.decimalFormat.format(this.stop.getDistance())) + " m");
        this.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        this.selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStop(stop);
            }
        });

        // Initialise the list of services when selecting a start stop
        if (this.stop.getServices() != null && this.stopTypeEnum.equals(StopTypeEnum.START)){
            services = new ArrayList<>(this.stop.getServices());
            ServiceAdapter serviceAdapter = new ServiceAdapter(getActivity(), services, getResources());
            serviceAdapter.setSmallServiceItem(true);
            servicesListView.setAdapter(serviceAdapter);

            // Make the ListView items selectable
            this.serviceListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            this.serviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectedService = services.get(position);
                }
            });
        }else{
            this.servicesLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        Window window = getDialog().getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        window.setGravity(Gravity.CENTER);
    }

    @Override
    public int show(FragmentTransaction transaction, String tag){

        return super.show(transaction, tag);
    }

    private void selectStop(Stop stop){

        Ride ride = this.journeyManager.getRide();

        switch (this.stopTypeEnum){
            case START:

                ride.setStartStop(stop);

                if (this.selectedService != null){

                    ride.setService(this.selectedService);

                    // Start end-stop selector activity
                    Activity currentActivity = getActivity();
                    Intent intent = new Intent(currentActivity, StopSetupActivity.class);
                    intent.putExtra("STOP", StopTypeEnum.END);
                    this.startActivity(intent);

                    // Close down the activity
                    currentActivity.finish();
                }else{

                    // Close down the activity
                    Activity currentActivity = getActivity();
                    currentActivity.finish();
                }
                break;
            case END:

                ride.setEndStop(stop);

                // Close down the activity
                Activity currentActivity = getActivity();
                currentActivity.finish();
                break;
        }
    }
}
