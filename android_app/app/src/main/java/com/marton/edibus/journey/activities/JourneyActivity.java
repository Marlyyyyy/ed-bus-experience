package com.marton.edibus.journey.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.google.inject.Inject;
import com.marton.edibus.R;
import com.marton.edibus.shared.adapters.ViewPagerAdapter;
import com.marton.edibus.journey.enums.JourneyTabEnum;
import com.marton.edibus.journey.events.RideActionFiredEvent;
import com.marton.edibus.journey.fragments.JourneyFeedbackFragment;
import com.marton.edibus.journey.fragments.JourneySetupFragment;
import com.marton.edibus.journey.fragments.JourneyTrackerFragment;
import com.marton.edibus.journey.utilities.JourneyManager;
import com.marton.edibus.shared.widgets.SlidingTabLayout;

import de.greenrobot.event.EventBus;
import roboguice.activity.RoboActionBarActivity;

public class JourneyActivity extends RoboActionBarActivity {

    private EventBus eventBus = EventBus.getDefault();

    private final String activityTitle = "New Journey";

    private ViewPager pager;
    private CharSequence titles[] = {"Options", "Tracking", "Feedback"};
    private int numberOfTabs = this.titles.length;

    @Inject
    private JourneyManager journeyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_journey);

        // Configure sliding pages
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), this.titles, this.numberOfTabs) {
            @Override
            public Fragment getItem(int position) {

                if (position == JourneyTabEnum.SETUP.ordinal()) {
                    return new JourneySetupFragment();
                } else if (position == JourneyTabEnum.TRACKER.ordinal()) {
                    return new JourneyTrackerFragment();
                } else {
                    return new JourneyFeedbackFragment();
                }
            }
        };

        // Assign ViewPager View and set the adapter
        this.pager = (ViewPager) findViewById(R.id.pager);
        this.pager.setAdapter(adapter);

        // Assign the Sliding Tab Layout View
        SlidingTabLayout tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        // To make the Tabs Fixed set this true. This makes the tabs Space Evenly in Available width
        tabs.setDistributeEvenly(true);

        // Set Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        // Set the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(this.pager);
    }

    @Override
    public void onStart(){
        super.onStart();

        final ActionBar actionBar = this.getSupportActionBar();

        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            setTitle(this.activityTitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Dynamically changes the UI page of the journey recording
    public void onEventMainThread(RideActionFiredEvent rideActionFiredEvent){

        switch(rideActionFiredEvent.getRideActionEnum()){
            case NEW_RIDE_STARTED:
                this.pager.setCurrentItem(JourneyTabEnum.SETUP.ordinal());
                break;
            case SETUP_COMPLETED:
                this.pager.setCurrentItem(JourneyTabEnum.TRACKER.ordinal());
                break;
            case TRAVELLING_STARTED:
                this.pager.setCurrentItem(JourneyTabEnum.FEEDBACK.ordinal());
                break;
            case FEEDBACK_COMPLETED:
                this.pager.setCurrentItem(JourneyTabEnum.TRACKER.ordinal());
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        // Register as a subscriber
        this.eventBus.register(this);
    }

    @Override
    public void onPause(){
        super.onPause();

        // Register as a subscriber
        this.eventBus.unregister(this);
    }
}
