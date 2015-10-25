package com.marton.edibus.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.marton.edibus.fragments.LoginFragment;
import com.marton.edibus.fragments.SignupFragment;


public class ViewPagerAdapter extends FragmentPagerAdapter {

    CharSequence titles[];
    int numberOfTabs;


    // Build a Constructor and assign the passed Values to appropriate values in the class
    public ViewPagerAdapter(FragmentManager fm,CharSequence titles[], int numberOfTabs) {
        super(fm);

        this.titles = titles;
        this.numberOfTabs = numberOfTabs;
    }

    // Returns the titles for the Tabs in the Tab Strip
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    // Returns the Number of tabs for the tabs Strip
    @Override
    public int getCount() {
        return numberOfTabs;
    }

    // Needs to be personalised
    @Override
    public Fragment getItem(int position) {
        return null;
    }
}
