package com.marton.edibus.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.marton.edibus.R;
import com.marton.edibus.enums.StopTypeEnum;
import com.marton.edibus.enums.TripActionEnum;
import com.marton.edibus.events.TripActionFiredEvent;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;


public class JourneyFeedbackFragment extends RoboFragment {

    private static final String TAG = JourneyFeedbackFragment.class.getName();

    private EventBus eventBus = EventBus.getDefault();

    private TripActionFiredEvent tripActionFiredEvent;

    @InjectView(R.id.journey_feedback_complete)
    Button feedbackCompleteButton;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        this.tripActionFiredEvent = new TripActionFiredEvent();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_journey_feedback,container,false);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configure listeners for buttons and switches
        this.feedbackCompleteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                tripActionFiredEvent.setTripActionEnum(TripActionEnum.FEEDBACK_COMPLETED);
                eventBus.post(tripActionFiredEvent);
            }
        });
    }
}
