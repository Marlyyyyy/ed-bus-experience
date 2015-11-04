package com.marton.edibus.fragments;


import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.inject.Inject;
import com.marton.edibus.R;
import com.marton.edibus.WebCallBack;
import com.marton.edibus.events.JourneyUpdatedEvent;
import com.marton.edibus.models.Service;
import com.marton.edibus.network.BusWebClient;
import com.marton.edibus.utilities.JourneyManager;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboDialogFragment;

public class ServiceDialogFragment extends RoboDialogFragment {

    private EventBus eventBus = EventBus.getDefault();

    @Inject
    BusWebClient busWebService;

    @Inject
    JourneyManager journeyManager;

    ListView serviceListView;

    ArrayList<Service> availableServices;

    List<String> availableServicesNames;

    ArrayAdapter<String> serviceAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_service, container, false);

        serviceListView = (ListView) view.findViewById(R.id.service_list);

        getDialog().setTitle("Select a service");

        availableServicesNames = new ArrayList<>();

        serviceAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, availableServicesNames);
        serviceListView.setAdapter(serviceAdapter);
        serviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Select new service and fire update event
                journeyManager.getTrip().setService(availableServices.get(position));
                eventBus.post(new JourneyUpdatedEvent());
                getDialog().cancel();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        WebCallBack<List<Service>> webCallBack = new WebCallBack<List<Service>>() {
            @Override
            public void onSuccess(List<Service> services) {
                for (int i=0; i<services.size(); i++){
                    availableServices = new ArrayList<>(services);
                    serviceAdapter.add(String.valueOf(availableServices.get(i).getId()));
                }
            }
        };

        busWebService.getServicesForStop(journeyManager.getTrip().getStartStopId(), webCallBack);
    }

    @Override
    public int show(FragmentTransaction transaction, String tag){

        return super.show(transaction, tag);
    }
}
