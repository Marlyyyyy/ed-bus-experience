package com.marton.edibus.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

import com.google.inject.Inject;
import com.marton.edibus.R;
import com.marton.edibus.WebCallBack;
import com.marton.edibus.network.UserWebClient;
import com.marton.edibus.network.WebClient;
import com.marton.edibus.utilities.AuthenticationManager;


import org.json.JSONObject;

import java.math.BigInteger;
import java.security.SecureRandom;

import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;

public class AuthenticationActivity extends RoboActionBarActivity {

    @InjectView(R.id.create_account)
    Button createAccountButton;

    @InjectView(R.id.welcome_text)
    TextView welcomeTextView;

    @InjectView(R.id.thank_you_text)
    TextView thankYouTextView;

    @Inject
    UserWebClient userWebClient;

    @Inject
    WebClient webClient;

    @Inject
    AuthenticationManager authenticationManager;

    private SecureRandom random = new SecureRandom();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        // Animate the welcome page
        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(2000);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                welcomeTextView.setAlpha(1.0f);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        welcomeTextView.startAnimation(animation);

        this.createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUserAccount();
            }
        });
    }

    // Sets up a temporary user account, and authenticates the user
    private void createUserAccount(){

        final ProgressDialog progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating guest account...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final String username = generateUniqueString();
        final String password = generateUniqueString();

        this.userWebClient.register(username, password, new WebCallBack<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                authenticationManager.authenticate(username, password, new WebCallBack() {
                    @Override
                    public void onSuccess(Object data) {

                        // Prepare the web client with the authentication token
                        webClient.setAuthenticationToken(authenticationManager.getTokenFromCache());
                        Intent intent = new Intent(AuthenticationActivity.this, ContentActivity.class);
                        startActivity(intent);

                        progressDialog.dismiss();
                        // No need to keep this activity any more.
                        finish();
                    }
                });
            }
        });
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
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    // Generates a unique string suitable for temporary user-names or passwords
    public String generateUniqueString() {
        return new BigInteger(130, random).toString(32);
    }
}
