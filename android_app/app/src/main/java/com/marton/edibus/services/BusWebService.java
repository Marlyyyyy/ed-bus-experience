package com.marton.edibus.services;


import android.util.Log;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.marton.edibus.WebCallBack;
import com.marton.edibus.models.Journey;
import com.marton.edibus.models.Service;
import com.marton.edibus.models.Stop;
import com.marton.edibus.models.Trip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import cz.msebera.android.httpclient.Header;


@Singleton
public class BusWebService {

    private static final String TAG = WebCallBack.class.getName();

    private static final String BASE_URL_BUS = "http://192.168.0.9:8000/bus";

    @Inject private UserWebService userWebService;

    private AsyncHttpClient client;

    public BusWebService(){
        client = new AsyncHttpClient();
    }

    public void getServicesForStop(int stopId, final WebCallBack<List<Service>> callback) {

        userWebService.login("Marton", "pw123", new WebCallBack<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                JSONObject newData = data;
            }
        });

        RequestParams parameters = new RequestParams();
        parameters.put("stop_id", stopId);

        String url = "/api/get_services_for_stop";
        client.get(getAbsoluteBusUrl(url), parameters, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Gson gson = new Gson();
                Service[] serviceArray = new Service[0];
                try {
                    JSONArray serviceJsonArray = response.getJSONArray("services");
                    serviceArray = gson.fromJson(serviceJsonArray.toString(), Service[].class);
                } catch (JSONException e) {
                    Log.e(TAG, "Was unable to get the array of services from Json");
                }
                List<Service> serviceList = Arrays.asList(serviceArray);
                callback.onSuccess(serviceList);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                // TODO: error response should be contained within the JsonObject
                callback.onFailure(statusCode, errorResponse.toString());
            }
        });
    }

    public void getClosestStops(double latitude, double longitude, int numberOfStops, final WebCallBack<List<Stop>> callback) {

        RequestParams parameters = new RequestParams();
        parameters.put("latitude", latitude);
        parameters.put("longitude", longitude);
        parameters.put("number_of_stops", numberOfStops);

        String url = "/api/get_closest_stops";
        client.get(getAbsoluteBusUrl(url), parameters, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Gson gson = new Gson();
                JSONArray stopJsonArray = null;
                try {
                    stopJsonArray = response.getJSONArray("stops");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // TODO: check for null
                Stop[] stopArray = gson.fromJson(stopJsonArray.toString(), Stop[].class);
                List<Stop> stopList = Arrays.asList(stopArray);
                callback.onSuccess(stopList);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                // TODO: error response should be contained within the JsonObject
                callback.onFailure(statusCode, errorResponse.toString());
            }
        });
    }

    public void uploadNewTrip(Trip trip, final WebCallBack callback) {
        uploadNewTrip(0, trip, callback);
    }

    public void uploadNewTrip(final int journeyId, Trip trip, final WebCallBack<Integer> callback) {

        RequestParams parameters = new RequestParams();
        if (journeyId != 0){
            parameters.put("journey_id", journeyId);
        }

        Gson gson = new Gson();
        String tripJson = gson.toJson(trip);
        parameters.put("trip", tripJson);

        String url = "/api/get_closest_stops";
        client.post(getAbsoluteBusUrl(url), parameters, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    int journeyId = response.getInt("journey_id");
                } catch (JSONException e) {
                    Log.e(TAG, "Was unable to get the journey ID integer from Json response");
                }
                callback.onSuccess(journeyId);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                // TODO: error response should be contained within the JsonObject
                callback.onFailure(statusCode, errorResponse.toString());
            }
        });
    }

    public void getDiaryForUser(String username, WebCallBack<List<Journey>> callback){

        RequestParams parameters = new RequestParams();
        String url = "/api/get_diary_for_user";
        client.get(getAbsoluteBusUrl(url), parameters, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                JSONArray journeys = null;

                try {
                    journeys = response.getJSONArray("journeys");
                } catch (JSONException e) {
                    Log.e(TAG, "Was unable to get the journey ID integer from Json response");
                }

                Gson gson = new Gson();

                Journey[] journey_array = gson.fromJson(journeys.toString(), Journey[].class);
                List<Journey> stopList = Arrays.asList(journey_array);
                // callback.onSuccess(journeyId);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                // TODO: error response should be contained within the JsonObject
                // callback.onFailure(statusCode, errorResponse.toString());
            }
        });

    }

    private static String getAbsoluteBusUrl(String relativeUrl) {
        return BASE_URL_BUS + relativeUrl;
    }
}
