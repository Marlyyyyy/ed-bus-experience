package com.marton.edibus.main.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.inject.Inject;
import com.marton.edibus.App;
import com.marton.edibus.R;
import com.marton.edibus.main.fragments.DashboardFragment;
import com.marton.edibus.main.fragments.DiaryFragment;
import com.marton.edibus.main.utilities.QuestionnaireManager;
import com.marton.edibus.shared.adapters.ViewPagerAdapter;
import com.marton.edibus.shared.events.LoginRequiredEvent;
import com.marton.edibus.shared.utilities.AuthenticationManager;
import com.marton.edibus.shared.widgets.SlidingTabLayout;

import de.greenrobot.event.EventBus;
import roboguice.activity.RoboActionBarActivity;

public class ContentActivity extends RoboActionBarActivity {

    private EventBus eventBus = EventBus.getDefault();

    @Inject
    private AuthenticationManager authenticationManager;

    private CharSequence titles[] = {"Dashboard", "Diary"};

    private int numberOfTabs = this.titles.length;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        // Register as a subscriber
        this.eventBus.register(this);

        // Launch authentication activity depending on if the user is logged in
        if (!this.authenticationManager.userAuthenticated()) {
            Intent intent = new Intent(ContentActivity.this, AuthenticationActivity.class);
            startActivity(intent);
            this.finish();
            return;
        }else{
            this.authenticationManager.authenticateWebRequests();
        }

        // Display questionnaire if required
        boolean questionnaireFilledIn = QuestionnaireManager.readQuestionnaireFilledInFromSharedPreferences();
        if (!questionnaireFilledIn){
            Intent intent = new Intent(ContentActivity.this, QuestionnaireActivity.class);
            startActivity(intent);
            this.finish();
            return;
        }

        // Configure sliding pages
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), this.titles, this.numberOfTabs) {
            @Override
            public Fragment getItem(int position) {

                if (position == 0) {
                    return new DashboardFragment();
                } else{
                    return new DiaryFragment();
                }
            }
        };

        // Assign ViewPager View and set the adapter
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Assign the Sliding Tab Layout View
        SlidingTabLayout tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        // To make the Tabs Fixed set this true. This makes the tabs Space Evenly in Available width
        tabs.setDistributeEvenly(true);

        // Set Custom Color for the Scroll bar indicator of the Tab View
        final Context context = this;
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return ContextCompat.getColor(context, R.color.tabsScrollColor);
            }
        });

        // Set the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_content, menu);
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
    public void onDestroy(){

        super.onDestroy();
    }

    public void onEvent(LoginRequiredEvent loginRequiredEvent){

        // Finish this activity, launch Authentication activity again
        Toast.makeText(App.getAppContext(), "Your old account cannot be used any longer.", Toast.LENGTH_LONG).show();

        this.authenticationManager.deAuthenticate();
        Intent intent = new Intent(ContentActivity.this, AuthenticationActivity.class);
        this.startActivity(intent);
        this.finish();
    }
}
