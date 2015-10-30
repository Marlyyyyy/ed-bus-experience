package com.marton.edibus.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.marton.edibus.R;
import com.marton.edibus.adapters.ViewPagerAdapter;
import com.marton.edibus.enums.JourneyTabEnum;
import com.marton.edibus.events.TripControlEvent;
import com.marton.edibus.fragments.JourneyFeedbackFragment;
import com.marton.edibus.fragments.JourneySetupFragment;
import com.marton.edibus.fragments.JourneyTrackerFragment;
import com.marton.edibus.utilities.SnackbarManager;
import com.marton.edibus.widgets.SlidingTabLayout;

import de.greenrobot.event.EventBus;
import roboguice.activity.RoboActionBarActivity;

public class JourneyActivity extends RoboActionBarActivity {

    private static final String TAG = JourneyActivity.class.getName();

    private EventBus eventBus = EventBus.getDefault();

    Toolbar toolbar;
    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence titles[] = {"Options", "Tracking", "Feedback"};
    int numberOfTabs = titles.length;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey);

        // Register as a subscriber
        eventBus.register(this);

        // Create The Toolbar and setting it as the Toolbar for the activity
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        adapter =  new ViewPagerAdapter(getSupportFragmentManager(), titles, numberOfTabs){
            @Override
            public Fragment getItem(int position){

                if(position == JourneyTabEnum.SETUP.ordinal())
                {
                    return new JourneySetupFragment();
                }
                else if(position == JourneyTabEnum.TRACKER.ordinal())
                {
                    return new JourneyTrackerFragment();
                }
                else
                {
                    return new JourneyFeedbackFragment();
                }
            }
        };

        // Assign ViewPager View and set the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Assign the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
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
        tabs.setViewPager(pager);
    }

    public void onEventMainThread(TripControlEvent tripControlEvent){

        switch(tripControlEvent.tripControlEnum){
            case SETUP_COMPLETE:
                this.pager.setCurrentItem(JourneyTabEnum.TRACKER.ordinal());
                break;
        }
    }
}
