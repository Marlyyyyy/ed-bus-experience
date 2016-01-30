package com.marton.edibus.shared.network;


import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.marton.edibus.shared.utilities.WebCallBack;
import com.marton.edibus.main.models.Questionnaire;
import com.marton.edibus.shared.models.Service;
import com.marton.edibus.shared.models.Stop;
import com.marton.edibus.shared.models.Ride;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import cz.msebera.android.httpclient.Header;


@Singleton
public class BusClient {

    private static final String TAG = BusClient.class.getName();

    private static final String BASE_URL_BUS = WebClient.BASE_URL + "bus";

    @Inject
    private WebClient webClient;

    public BusClient(){
    }

    public void getServicesForStop(int id, final WebCallBack<List<Service>> callback) {

        RequestParams parameters = new RequestParams();
        parameters.put("id", id);

        String url = "/api/services_for_stop/";
        this.webClient.get(getAbsoluteBusUrl(url), parameters, new JsonHttpResponseHandler() {

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

                callback.onFailure(statusCode, errorResponse);
            }
        });
    }

    public void getAllServices(final WebCallBack<List<Service>> callback) {

        String url = "/api/service/";
        this.webClient.get(getAbsoluteBusUrl(url), new RequestParams(), new JsonHttpResponseHandler() {

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

                callback.onFailure(statusCode, errorResponse);
            }
        });
    }

    public void getStopsForService(int id, int startStopId, final WebCallBack<List<Stop>> callback) {

        RequestParams parameters = new RequestParams();
        parameters.put("service_id", id);
        parameters.put("start_stop_id", startStopId);

        String url = "/api/stops_for_service/";
        this.webClient.get(getAbsoluteBusUrl(url), parameters, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Gson gson = new Gson();
                Stop[] stopArray = new Stop[0];
                try {
                    JSONArray stopJsonArray = response.getJSONArray("stops");
                    stopArray = gson.fromJson(stopJsonArray.toString(), Stop[].class);
                } catch (JSONException e) {
                    Log.e(TAG, "Was unable to get the array of stops from Json");
                }
                List<Stop> stopList = Arrays.asList(stopArray);
                callback.onSuccess(stopList);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                callback.onFailure(statusCode, errorResponse);
            }
        });
    }

    public void getStopsForService(int id, final WebCallBack<List<Stop>> callback) {

        RequestParams parameters = new RequestParams();
        parameters.put("service_id", id);

        String url = "/api/stops_for_service/";
        this.webClient.get(getAbsoluteBusUrl(url), parameters, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Gson gson = new Gson();
                Stop[] stopArray = new Stop[0];
                try {
                    JSONArray stopJsonArray = response.getJSONArray("stops");
                    stopArray = gson.fromJson(stopJsonArray.toString(), Stop[].class);
                } catch (JSONException e) {
                    Log.e(TAG, "Was unable to get the array of stops from Json");
                }
                List<Stop> stopList = Arrays.asList(stopArray);
                callback.onSuccess(stopList);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                callback.onFailure(statusCode, errorResponse);
            }
        });
    }

    public void getStopsWithinRadius(double latitude, double longitude, double radius, final WebCallBack<List<Stop>> callback) {

        RequestParams parameters = new RequestParams();
        parameters.put("latitude", latitude);
        parameters.put("longitude", longitude);
        parameters.put("radius", radius);

        String url = "/api/stops_within_radius/";
        this.webClient.get(getAbsoluteBusUrl(url), parameters, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Gson gson = new Gson();
                JSONArray stopJsonArray = null;
                try {
                    stopJsonArray = response.getJSONArray("stops");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Stop[] stopArray = new Stop[0];
                if (stopJsonArray != null) {
                    stopArray = gson.fromJson(stopJsonArray.toString(), Stop[].class);
                }
                List<Stop> stopList = Arrays.asList(stopArray);
                callback.onSuccess(stopList);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                callback.onFailure(statusCode, errorResponse);
            }
        });
    }

    public void uploadNewTrip(Ride ride, final WebCallBack<Integer> callback) {
        this.uploadNewTrip(0, ride, callback);
    }

    public void uploadNewTrip(final int journeyId, Ride ride, final WebCallBack<Integer> callback) {

        RequestParams parameters = new RequestParams();
        if (journeyId != 0){
            parameters.put("journey_id", journeyId);
        }

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        String tripJson = gson.toJson(ride);
        parameters.put("ride", tripJson);

        String url = "/api/ride/";
        this.webClient.post(getAbsoluteBusUrl(url), parameters, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                int journeyId = 0;
                try {
                    journeyId = response.getInt("journey_id");
                } catch (JSONException e) {
                    Log.e(TAG, "Was unable to get the journey ID integer from Json response");
                }
                callback.onSuccess(journeyId);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                callback.onFailure(statusCode, errorResponse);
            }
        });
    }

    public void uploadNewQuestionnaire(Questionnaire questionnaire, final WebCallBack<Boolean> callback) {

        RequestParams parameters = new RequestParams();

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        String questionnaireJson = gson.toJson(questionnaire);
        parameters.put("questionnaire", questionnaireJson);

        String url = "/api/questionnaire/";
        this.webClient.post(getAbsoluteBusUrl(url), parameters, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                callback.onSuccess(true);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                callback.onFailure(statusCode, errorResponse);
            }
        });
    }

    private static String getAbsoluteBusUrl(String relativeUrl) {
        return BASE_URL_BUS + relativeUrl;
    }
}
