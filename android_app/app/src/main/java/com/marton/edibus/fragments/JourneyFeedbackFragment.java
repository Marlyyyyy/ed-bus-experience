package com.marton.edibus.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.inject.Inject;
import com.marton.edibus.R;
import com.marton.edibus.enums.RideActionEnum;
import com.marton.edibus.events.RideActionFiredEvent;
import com.marton.edibus.utilities.JourneyManager;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;


public class JourneyFeedbackFragment extends RoboFragment {

    private EventBus eventBus = EventBus.getDefault();

    private RideActionFiredEvent rideActionFiredEvent;

    @InjectView(R.id.journey_feedback_complete)
    Button feedbackCompleteButton;

    @InjectView(R.id.rating_bar)
    RatingBar ratingBar;

    @InjectView(R.id.seat_switch)
    Switch seatSwitch;

    @InjectView(R.id.seat_switch_layout)
    LinearLayout seatSwitchLayout;

    @InjectView(R.id.seat_switch_text)
    TextView seatSwitchTextView;

    @InjectView(R.id.greet_switch)
    Switch greetSwitch;

    @InjectView(R.id.greet_switch_layout)
    LinearLayout greetSwitchLayout;

    @InjectView(R.id.greet_switch_text)
    TextView greetSwitchTextView;

    @InjectView(R.id.people_waiting)
    EditText peopleWaitingEditText;

    @InjectView(R.id.people_boarding)
    EditText peopleBoardingEditText;

    @Inject
    JourneyManager journeyManager;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        this.rideActionFiredEvent = new RideActionFiredEvent();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_journey_feedback,container,false);

        return view;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Edit texts are only saved when the user clicks Save
        this.feedbackCompleteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Read input for people waiting
                int peopleWaiting = -1;
                String peopleWaitingText = String.valueOf(peopleWaitingEditText.getText());
                if (!peopleWaitingText.equals("")){
                    peopleWaiting = Integer.valueOf(peopleWaitingText);
                }
                journeyManager.getRide().setPeopleWaiting(peopleWaiting);

                // Read input for people boarding
                int peopleBoarding = -1;
                String peopleBoradingText = String.valueOf(peopleBoardingEditText.getText());
                if (!peopleBoradingText.equals("")){
                    peopleBoarding = Integer.valueOf(peopleBoradingText);
                }
                journeyManager.getRide().setPeopleBoarding(peopleBoarding);

                // Jump back to the Tracker page
                rideActionFiredEvent.setRideActionEnum(RideActionEnum.FEEDBACK_COMPLETED);
                eventBus.post(rideActionFiredEvent);
            }
        });

        // Rating bars and switches are saved whenever they get changed
        this.ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

                journeyManager.getRide().setRating(rating);
            }
        });

        this.seatSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                switchSeat(isChecked);
            }
        });

        this.seatSwitchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean previousState = seatSwitch.isChecked();
                seatSwitch.setChecked(!previousState);
                switchSeat(!previousState);
            }
        });

        this.greetSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                switchGreet(isChecked);
            }
        });

        this.greetSwitchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean previousState = greetSwitch.isChecked();
                greetSwitch.setChecked(!previousState);
                switchGreet(!previousState);
            }
        });
    }

    private void switchSeat(boolean isChecked){

        this.journeyManager.getRide().setSeat(isChecked);

        if (isChecked){
            this.seatSwitchTextView.setText("Yes");
        }else{
            this.seatSwitchTextView.setText("No");
        }
    }

    private void switchGreet(boolean isChecked){

        this.journeyManager.getRide().setGreet(isChecked);

        if (isChecked){
            this.greetSwitchTextView.setText("Yes");
        }else{
            this.greetSwitchTextView.setText("No");
        }
    }
}
