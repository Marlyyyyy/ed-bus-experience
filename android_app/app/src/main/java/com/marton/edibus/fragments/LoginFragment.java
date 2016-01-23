package com.marton.edibus.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.inject.Inject;
import com.marton.edibus.R;
import com.marton.edibus.WebCallBack;
import com.marton.edibus.activities.ContentActivity;
import com.marton.edibus.network.UserClient;
import com.marton.edibus.network.WebClient;
import com.marton.edibus.utilities.AuthenticationManager;

import org.json.JSONObject;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;


public class LoginFragment extends RoboFragment {

    private static final String TAG = LoginFragment.class.getName();

    private @Inject
    UserClient userWebService;
    private @Inject AuthenticationManager authenticationManager;
    private @Inject WebClient webClient;

    @InjectView(R.id.input_email) EditText usernameText;
    @InjectView(R.id.input_password) EditText passwordText;
    @InjectView(R.id.btn_login) Button loginButton;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.fragment_login,container,false);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    public void login() {
        Log.d(TAG, "Login");

        final ProgressDialog progressDialog = new ProgressDialog(this.getActivity(), R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.setCancelable(false);
        progressDialog.show();


        WebCallBack<JSONObject> webCallBack = new WebCallBack<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                JSONObject myData = data;
                webClient.setAuthenticationToken(authenticationManager.getTokenFromCache());

                progressDialog.dismiss();

                Activity currentActivity = getActivity();
                Intent intent = new Intent(currentActivity, ContentActivity.class);
                startActivity(intent);

                // No need to keep this activity anymore.
                currentActivity.finish();
            }
        };

        authenticationManager.authenticate("Marton", "pw123", webCallBack);

    }
}
