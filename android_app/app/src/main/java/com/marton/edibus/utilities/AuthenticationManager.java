package com.marton.edibus.utilities;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.marton.edibus.App;
import com.marton.edibus.WebCallBack;
import com.marton.edibus.network.UserWebService;
import com.marton.edibus.network.WebClient;

import org.json.JSONException;
import org.json.JSONObject;


@Singleton
public class AuthenticationManager {

    private @Inject
    UserWebService userWebservice;
    private @Inject
    WebClient webClient;

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
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                callback.onSuccess(data);
            }
        };
        userWebservice.get_token(username, password, authenticationCallback);
    }

    public void deAuthenticate(){
        SharedPreferencesManager.removeKeyValue(App.getAppContext(), TOKEN_KEY);
    }
}
