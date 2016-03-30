package com.marton.edibus.shared.utilities;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.marton.edibus.App;
import com.marton.edibus.shared.network.UserClient;
import com.marton.edibus.shared.network.WebCallBack;
import com.marton.edibus.shared.network.WebClient;


@Singleton
public class AuthenticationManager {

    @Inject
    private UserClient userClient;

    @Inject
    private WebClient webClient;

    private static final String TOKEN_KEY = "token";

    public String getTokenFromCache(){
        return SharedPreferencesManager.readString(App.getAppContext(), TOKEN_KEY);
    }

    public boolean userAuthenticated(){
        return getTokenFromCache() != null;
    }

    // Authenticates the user with the provided username and password, and executes callback
    public void authenticate(String username, String password, final WebCallBack<String> callback){

        WebCallBack<String> authenticationCallback = new WebCallBack<String>() {

            @Override
            public void onSuccess(String token) {

                // Store the returned token
                SharedPreferencesManager.writeString(App.getAppContext(), TOKEN_KEY, token);
                callback.onSuccess(token);
            }
        };

        // Get the authentication token from the web-service
        this.userClient.getToken(username, password, authenticationCallback);
    }

    // Removes the authentication token from the shared preferences
    public void deAuthenticate(){
        SharedPreferencesManager.removeKeyValue(App.getAppContext(), TOKEN_KEY);
    }
}
