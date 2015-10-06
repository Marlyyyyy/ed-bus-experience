package com.marton.edibus.services;


import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class WebService {

    private static final String BASE_URL_BUS = "http://192.168.0.9:8000/bus";
    private static final String BASE_URL_AUTHENTICATION = "http://192.168.0.9:8000/auth";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void getServicesForStop(int stopId) {

        RequestParams parameters = new RequestParams();
        parameters.put("stop_id", stopId);

        String url = "/api/get_services_for_stop";
        client.post(getAbsoluteBusUrl(url), parameters, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                if (statusCode == 200) {

                    /*data.getString("bla"))*/
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response){
                int statuscode = statusCode;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)

                int statuscode = statusCode;
            }


        });
    }

    private static String getAbsoluteBusUrl(String relativeUrl) {
        return BASE_URL_BUS + relativeUrl;
    }

    private static String getAbsoluteAuthenticationUrl(String relativeUrl) {
        return BASE_URL_AUTHENTICATION + relativeUrl;
    }
}
