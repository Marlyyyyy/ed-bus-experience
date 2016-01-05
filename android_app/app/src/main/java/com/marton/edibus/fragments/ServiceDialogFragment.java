package com.marton.edibus.fragments;


import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.inject.Inject;
import com.marton.edibus.R;
import com.marton.edibus.adapters.ServiceAdapter;
import com.marton.edibus.events.JourneyUpdatedEvent;
import com.marton.edibus.models.Service;
import com.marton.edibus.network.BusWebClient;
import com.marton.edibus.utilities.JourneyManager;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboDialogFragment;
import roboguice.inject.InjectView;

public class ServiceDialogFragment extends RoboDialogFragment {

    private EventBus eventBus = EventBus.getDefault();

    @Inject
    private BusWebClient busWebService;

    @Inject
    private JourneyManager journeyManager;

    @InjectView(R.id.cancel)
    private Button cancelButton;

    private ListView serviceListView;

    private ArrayList<Service> availableServices;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);

        this.availableServices = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_service, container, false);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ArrayList<Service> services = this.journeyManager.getRide().getStartStop().getServices();
        if (services != null){
            this.availableServices = services;
            this.serviceListView = (ListView) view.findViewById(R.id.service_list);
            ServiceAdapter serviceAdapter = new ServiceAdapter(getActivity(), availableServices, getResources());
            this.serviceListView.setAdapter(serviceAdapter);
            this.serviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    // Select new service and fire update event
                    journeyManager.getRide().setService(availableServices.get(position));
                    eventBus.post(new JourneyUpdatedEvent());
                    getDialog().cancel();
                }
            });
        }

        this.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        Window window = getDialog().getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        window.setGravity(Gravity.CENTER);
    }

    @Override
    public int show(FragmentTransaction transaction, String tag){

        return super.show(transaction, tag);
    }
}
