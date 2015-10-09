package com.marton.edibus;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.marton.edibus.models.Service;
import com.marton.edibus.models.Stop;
import com.marton.edibus.services.BusWebService;
import com.marton.edibus.services.UserWebService;

import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebCallBack<JSONObject> webCallBack = new WebCallBack<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                JSONObject myData = data;
            }
        };
        UserWebService.register("Heffalumps", "Woozles", webCallBack);
        UserWebService.login("Heffalumps", "Woozles", webCallBack);
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
