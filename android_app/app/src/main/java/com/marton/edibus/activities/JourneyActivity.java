package com.marton.edibus.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.marton.edibus.R;
import com.marton.edibus.adapters.ViewPagerAdapter;
import com.marton.edibus.enums.JourneyTabEnum;
import com.marton.edibus.events.TripActionFiredEvent;
import com.marton.edibus.fragments.JourneyFeedbackFragment;
import com.marton.edibus.fragments.JourneySetupFragment;
import com.marton.edibus.fragments.JourneyTrackerFragment;
import com.marton.edibus.widgets.SlidingTabLayout;

import de.greenrobot.event.EventBus;
import roboguice.activity.RoboActionBarActivity;

public class JourneyActivity extends RoboActionBarActivity {

    private static final String TAG = JourneyActivity.class.getName();

    private EventBus eventBus = EventBus.getDefault();

    private final String activityTitle = "Journey";

    Toolbar toolbar;
    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence titles[] = {"Options", "Tracking", "Feedback"};
    int numberOfTabs = titles.length;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_journey);

        // Register as a subscriber
        this.eventBus.register(this);

        // Create The Toolbar and setting it as the Toolbar for the activity
        this.toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        // Configure sliding pages
        this.adapter =  new ViewPagerAdapter(getSupportFragmentManager(), titles, numberOfTabs){
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
        this.pager = (ViewPager) findViewById(R.id.pager);
        this.pager.setAdapter(adapter);

        // Assign the Sliding Tab Layout View
        this.tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        // To make the Tabs Fixed set this true. This makes the tabs Space Evenly in Available width
        this.tabs.setDistributeEvenly(true);

        // Set Custom Color for the Scroll bar indicator of the Tab View
        this.tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        // Set the ViewPager For the SlidingTabsLayout
        this.tabs.setViewPager(pager);
    }

    @Override
    public void onStart(){
        super.onStart();

        final ActionBar actionBar = this.getSupportActionBar();

        if (actionBar != null){
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.actionbar_journey, null);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            TextView activityTitleTextView = (TextView) view.findViewById(R.id.activity_title);
            activityTitleTextView.setText(this.activityTitle);
        }
    }

    // Manages the flow of the journey
    public void onEventMainThread(TripActionFiredEvent tripActionFiredEvent){

        switch(tripActionFiredEvent.getTripActionEnum()){
            case NEW_TRIP:
                this.pager.setCurrentItem(JourneyTabEnum.SETUP.ordinal());
                break;
            case SETUP_COMPLETED:
                this.pager.setCurrentItem(JourneyTabEnum.TRACKER.ordinal());
                break;
            case TRIP_STARTED:
                this.pager.setCurrentItem(JourneyTabEnum.FEEDBACK.ordinal());
                break;
            case FEEDBACK_COMPLETED:
                this.pager.setCurrentItem(JourneyTabEnum.TRACKER.ordinal());
        }
    }
}
