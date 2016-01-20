package com.marton.edibus;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.inject.Inject;
import com.marton.edibus.activities.AuthenticationActivity;
import com.marton.edibus.activities.ContentActivity;
import com.marton.edibus.network.WebClient;
import com.marton.edibus.utilities.AuthenticationManager;

import roboguice.activity.RoboActivity;


public class MainActivity extends RoboActivity {

    @Inject
    private AuthenticationManager authenticationManager;

    @Inject
    private WebClient webClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.launchAppropriateActivity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    private void launchAppropriateActivity(){

        // Launch the right activity depending on if the user is logged in
        Intent intent;
        if (this.authenticationManager.userAuthenticated()){

            this.authenticationManager.authenticateWebRequests();
            intent = new Intent(MainActivity.this, ContentActivity.class);
        }else{
            intent = new Intent(MainActivity.this, AuthenticationActivity.class);
        }

        startActivity(intent);
    }
}
