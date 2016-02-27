package com.marton.edibus.shared.network;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.marton.edibus.App;
import com.marton.edibus.shared.utilities.SharedPreferencesManager;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


@Singleton
public class UserClient {

    private static final String BASE_URL_AUTHENTICATION = WebClient.BASE_URL + "auth";

    @Inject
    private WebClient webClient;

    public void get_token(String username, String password, final WebCallBack<String> callback) {

        RequestParams parameters = new RequestParams();
        parameters.put("username", username);
        parameters.put("password", password);

        String url = getAbsoluteUrl("/api/get_token/");
        this.webClient.post(url, parameters, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                String token = "";
                try {
                    token = response.getString("token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                callback.onSuccess(token);
            }

            @Override
            // Called when response HTTP status is "4XX" (eg. 401, 403, 404)
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                callback.onFailure(statusCode, errorResponse);
            }
        });
    }

    public void register(String username, String password, final WebCallBack<JSONObject> callback) {

        RequestParams parameters = new RequestParams();
        parameters.put("username", username);
        parameters.put("password", password);

        String url = "/api/accounts/";
        this.webClient.post(getAbsoluteUrl(url), parameters, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                callback.onSuccess(response);
            }

            @Override
            // Called when response HTTP status is "4XX" (eg. 401, 403, 404)
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                callback.onFailure(statusCode, errorResponse);
            }
        });
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL_AUTHENTICATION + relativeUrl;
    }
}
