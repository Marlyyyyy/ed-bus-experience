package com.marton.edibus;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.inject.Inject;
import com.marton.edibus.models.Service;
import com.marton.edibus.services.BusWebService;
import com.marton.edibus.services.UserWebService;

import java.util.List;

import roboguice.activity.RoboActivity;


public class MainActivity extends RoboActivity {

    @Inject BusWebService busWebService;
    @Inject UserWebService userWebService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebCallBack<List<Service>> webCallBack = new WebCallBack<List<Service>>() {
            @Override
            public void onSuccess(List<Service> data) {
                List<Service> myData = data;
            }
        };

        busWebService.getServicesForStop(36234945, webCallBack);
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
