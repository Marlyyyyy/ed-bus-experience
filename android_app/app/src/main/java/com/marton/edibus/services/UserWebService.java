package com.marton.edibus.services;

import com.google.inject.Singleton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.marton.edibus.App;
import com.marton.edibus.WebCallBack;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


@Singleton
public class UserWebService {

    private static final String TAG = UserWebService.class.getName();

    private static final String BASE_URL_AUTHENTICATION = "http://192.168.0.9:8000/auth";

    private AsyncHttpClient client;

    public UserWebService(){
        PersistentCookieStore myCookieStore = new PersistentCookieStore(App.getAppContext());
        client = new AsyncHttpClient();
        client.setCookieStore(myCookieStore);
    }

    public void login(String username, String password, final WebCallBack callback) {

        RequestParams parameters = new RequestParams();
        parameters.put("username", username);
        parameters.put("password", password);

        String url = "/api/login/";
        client.post(getAbsoluteUrl(url), parameters, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                callback.onSuccess(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // Called when response HTTP status is "4XX" (eg. 401, 403, 404)

                int statuscode = statusCode;
            }
        });
    }

    public void register(String username, String password, final WebCallBack<JSONObject> callback) {

        RequestParams parameters = new RequestParams();
        parameters.put("username", username);
        parameters.put("password", password);

        String url = "/api/accounts/";
        client.post(getAbsoluteUrl(url), parameters, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                callback.onSuccess(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // Called when response HTTP status is "4XX" (eg. 401, 403, 404)

                int statuscode = statusCode;
            }
        });
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL_AUTHENTICATION + relativeUrl;
    }
}
