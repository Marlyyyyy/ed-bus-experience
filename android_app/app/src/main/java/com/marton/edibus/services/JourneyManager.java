package com.marton.edibus.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.marton.edibus.WebCallBack;
import com.marton.edibus.models.Trip;
import com.marton.edibus.network.BusWebService;

@Singleton
public class JourneyManager {

    @Inject
    BusWebService busWebService;

    // The current trip of the user
    private Trip trip;

    public JourneyManager(){
        trip = new Trip();
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public void uploadTrip(WebCallBack callback){

        WebCallBack<Integer> serviceCallback = new WebCallBack<Integer>() {
            @Override
            public void onSuccess(Integer data) {
                trip = new Trip();
                trip.setJourneyId(data);
            }
        };

        if (this.trip.getJourneyId() == 0){
            busWebService.uploadNewTrip(this.trip, serviceCallback);
        }else{
            busWebService.uploadNewTrip(this.trip.getJourneyId(), this.trip, serviceCallback);
        }
    }
}
