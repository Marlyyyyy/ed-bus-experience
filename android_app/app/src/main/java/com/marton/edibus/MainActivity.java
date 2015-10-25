package com.marton.edibus;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.inject.Inject;
import com.marton.edibus.activities.AuthenticationActivity;
import com.marton.edibus.activities.ContentActivity;
import com.marton.edibus.utilities.AuthenticationManager;

import roboguice.activity.RoboActivity;


public class MainActivity extends RoboActivity {

    private static final String TAG = MainActivity.class.getName();

    @Inject
    AuthenticationManager authenticationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authenticationManager.deAuthenticate();

        Intent intent;
        if (authenticationManager.userAuthenticated()){
            intent = new Intent(this, ContentActivity.class);
        }else{
            intent = new Intent(this, AuthenticationActivity.class);
        }

        startActivity(intent);

        // No need to keep this activity anymore.
        this.finish();
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
}
