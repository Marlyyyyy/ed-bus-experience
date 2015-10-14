package com.marton.edibus.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.inject.Inject;
import com.marton.edibus.R;
import com.marton.edibus.WebCallBack;
import com.marton.edibus.network.UserWebService;
import com.marton.edibus.network.WebClient;
import com.marton.edibus.services.AuthenticationManager;

import org.json.JSONObject;

import roboguice.inject.InjectView;


public class LoginFragment extends Fragment {

    private static final String TAG = LoginFragment.class.getName();

    private @Inject
    UserWebService userWebService;
    private @Inject
    AuthenticationManager authenticationManager;
    private @Inject
    WebClient webClient;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.tab_login,container,false);

        return v;
    }

    public void login() {
        Log.d(TAG, "Login");

        // final ProgressDialog progressDialog = new ProgressDialog(AuthenticationActivity.this, R.style.AppTheme_Dark_Dialog);
        // progressDialog.setIndeterminate(true);
        // progressDialog.setMessage("Authenticating...");
        // progressDialog.show();

        // String username = usernameText.getText().toString();
        // String password = passwordText.getText().toString();

        WebCallBack<JSONObject> webCallBack = new WebCallBack<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                JSONObject myData = data;
                webClient.setAuthenticationToken(authenticationManager.getTokenFromCache());
                boolean isUserAuthenticated2 = authenticationManager.userAuthenticated();
                String stopPlease = "hey";
            }
        };

        authenticationManager.authenticate("Marton", "pw123", webCallBack);

    }
}
