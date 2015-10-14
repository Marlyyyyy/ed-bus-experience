package com.marton.edibus.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.inject.Inject;
import com.marton.edibus.R;
import com.marton.edibus.WebCallBack;
import com.marton.edibus.services.AuthenticationManager;
import com.marton.edibus.network.UserWebService;
import com.marton.edibus.network.WebClient;

import org.json.JSONObject;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

public class AuthenticationActivity extends RoboActivity {

    private static final String TAG = AuthenticationActivity.class.getName();

    private @Inject UserWebService userWebService;
    private @Inject AuthenticationManager authenticationManager;
    private @Inject WebClient webClient;

    @InjectView(R.id.input_email) EditText usernameText;
    @InjectView(R.id.input_password) EditText passwordText;
    @InjectView(R.id.btn_login) Button loginButton;
    @InjectView(R.id.link_signup) TextView signupLink;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }
}
