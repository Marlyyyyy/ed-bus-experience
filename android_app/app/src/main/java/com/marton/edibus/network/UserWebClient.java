package com.marton.edibus.network;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.marton.edibus.WebCallBack;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


@Singleton
public class UserWebClient {

    private static final String TAG = UserWebClient.class.getName();

    private static final String BASE_URL_AUTHENTICATION = "http://192.168.1.21:8000/auth";

    @Inject
    private WebClient webClient;

    public void get_token(String username, String password, final WebCallBack<JSONObject> callback) {

        RequestParams parameters = new RequestParams();
        parameters.put("username", username);
        parameters.put("password", password);

        String url = getAbsoluteUrl("/api/get_token/");
        webClient.post(url, parameters, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                callback.onSuccess(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // Called when response HTTP status is "4XX" (eg. 401, 403, 404)

                callback.onFailure(statusCode, errorResponse);
            }
        });
    }

    public void register(String username, String password, final WebCallBack<JSONObject> callback) {

        RequestParams parameters = new RequestParams();
        parameters.put("username", username);
        parameters.put("password", password);

        String url = "/api/accounts/";
        webClient.post(getAbsoluteUrl(url), parameters, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                callback.onSuccess(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // Called when response HTTP status is "4XX" (eg. 401, 403, 404)

                callback.onFailure(statusCode, errorResponse);
            }
        });
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL_AUTHENTICATION + relativeUrl;
    }
}
