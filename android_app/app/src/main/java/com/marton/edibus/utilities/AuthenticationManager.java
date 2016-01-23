package com.marton.edibus.utilities;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.marton.edibus.App;
import com.marton.edibus.WebCallBack;
import com.marton.edibus.network.UserClient;
import com.marton.edibus.network.WebClient;

import org.json.JSONException;
import org.json.JSONObject;


@Singleton
public class AuthenticationManager {

    @Inject
    private UserClient userWebservice;

    @Inject
    private WebClient webClient;

    private static final String TOKEN_KEY = "token";

    public String getTokenFromCache(){
        return SharedPreferencesManager.readString(App.getAppContext(), TOKEN_KEY);
    }

    public boolean userAuthenticated(){
        return getTokenFromCache() != null;
    }

    public void authenticate(String username, String password, final WebCallBack callback){

        WebCallBack<JSONObject> authenticationCallback = new WebCallBack<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                try {
                    String token = data.getString("token");
                    SharedPreferencesManager.writeString(App.getAppContext(), TOKEN_KEY, token);
                    authenticateWebRequests();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                callback.onSuccess(data);
            }
        };
        this.userWebservice.get_token(username, password, authenticationCallback);
    }

    // Prepares the web client with the authentication token
    public void authenticateWebRequests(){

        this.webClient.setAuthenticationToken(this.getTokenFromCache());
    }

    public void deAuthenticate(){
        SharedPreferencesManager.removeKeyValue(App.getAppContext(), TOKEN_KEY);
        this.webClient.unsetAuthenticationToken();
    }
}
