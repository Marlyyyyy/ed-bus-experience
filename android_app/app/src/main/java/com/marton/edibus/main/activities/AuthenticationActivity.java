package com.marton.edibus.main.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.google.inject.Inject;
import com.marton.edibus.R;
import com.marton.edibus.shared.network.WebCallBack;
import com.marton.edibus.shared.models.Log;
import com.marton.edibus.shared.network.UserClient;
import com.marton.edibus.shared.network.WebClient;
import com.marton.edibus.shared.utilities.AuthenticationManager;
import com.marton.edibus.shared.utilities.StatisticsManager;


import org.json.JSONObject;

import java.math.BigInteger;
import java.security.SecureRandom;

import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;

public class AuthenticationActivity extends RoboActionBarActivity {

    @InjectView(R.id.create_account)
    private Button createAccountButton;

    @InjectView(R.id.welcome_text)
    private TextView welcomeTextView;

    @InjectView(R.id.thank_you_text)
    private TextView thankYouTextView;

    @Inject
    private UserClient userClient;

    @Inject
    private WebClient webClient;

    @Inject
    private AuthenticationManager authenticationManager;

    private SecureRandom random = new SecureRandom();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        // Animate the welcome page
        Animation animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
        this.welcomeTextView.startAnimation(animationFadeIn);

        animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
        animationFadeIn.setStartOffset(1000l);
        this.thankYouTextView.startAnimation(animationFadeIn);

        animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
        animationFadeIn.setStartOffset(2000l);
        this.createAccountButton.startAnimation(animationFadeIn);

        this.createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUserAccount();
            }
        });
    }

    // Sets up a temporary user account, authenticates the user, resets local statistics
    private void createUserAccount(){

        final ProgressDialog progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating guest account...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final String username = generateUniqueString();
        final String password = generateUniqueString();

        this.userClient.register(username, password, new WebCallBack<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                authenticationManager.authenticate(username, password, new WebCallBack<String>() {
                    @Override
                    public void onSuccess(String data) {

                        webClient.setAuthenticationToken(data);

                        // Reset local statistics
                        StatisticsManager.clearStatistics();
                        Log.deleteAll(Log.class);

                        // Start the Content activity
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

        // Disable going back
        this.moveTaskToBack(true);
    }

    // Generates a unique string suitable for temporary user-names or passwords
    public String generateUniqueString() {
        return new BigInteger(130, random).toString(32);
    }
}
