package com.marton.edibus.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.marton.edibus.R;

import roboguice.fragment.RoboFragment;


public class JourneyFeedbackFragment extends RoboFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_journey_feedback,container,false);

        return view;
    }
}
