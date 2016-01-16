package com.marton.edibus.network;


import com.google.inject.Singleton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

@Singleton
public class WebClient {

    private AsyncHttpClient client;

    public static final String BASE_URL = "http://192.168.161.227:8000/";
    // public static final String BASE_URL = "http://ec2-52-31-124-50.eu-west-1.compute.amazonaws.com:8000/";

    public WebClient(){
        this.client = new AsyncHttpClient();
    }

    public void get(String url, RequestParams requestParameters, AsyncHttpResponseHandler responseHandler){
            this.client.get(url, requestParameters, responseHandler);
    }

    public void post(String url, RequestParams requestParameters, AsyncHttpResponseHandler responseHandler){
        this.client.post(url, requestParameters, responseHandler);
    }

    public void setAuthenticationToken(String token){
        this.client.addHeader("Authorization", "JWT " + token);
    }
}
