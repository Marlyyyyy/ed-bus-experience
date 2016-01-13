package com.marton.edibus.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.media.Rating;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.marton.edibus.R;
import com.marton.edibus.models.Log;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;


public class DiaryFragment extends RoboFragment {

    private LogAdapter logAdapter;

    private ArrayList<Log> logs;

    @InjectView(R.id.logs_list)
    private ListView logsListView;

    @InjectView(R.id.no_rides_text)
    private TextView noRidesTextView;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        this.logs = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_diary,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        this.logAdapter = new LogAdapter(getActivity(), this.logs, getResources());
        this.logsListView.setAdapter(this.logAdapter);
    }

    @Override
    public void onResume(){
        super.onResume();

        this.logs.clear();
        this.logs.addAll(Log.listAll(Log.class));
        this.logAdapter.notifyDataSetChanged();

        if (this.logs.size() == 0){
            this.logsListView.setVisibility(View.GONE);
            this.noRidesTextView.setVisibility(View.VISIBLE);
        }else{
            this.logsListView.setVisibility(View.VISIBLE);
            this.noRidesTextView.setVisibility(View.GONE);
        }
    }

    private static class LogAdapter extends BaseAdapter implements View.OnClickListener {

        private ArrayList logs;
        private LayoutInflater inflater = null;
        private Log log = null;
        private DateFormat dateFormat;
        private DateFormat durationFormat;
        private DecimalFormat decimalFormat;

        public LogAdapter(Activity activity, ArrayList logs, Resources resources) {

            this.logs = logs;
            this.dateFormat = android.text.format.DateFormat.getDateFormat(activity.getApplicationContext());
            this.durationFormat = new SimpleDateFormat("mm:ss", Locale.UK);
            this.dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.UK);
            this.decimalFormat = new DecimalFormat("#.##");

            this.inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {

            return this.logs.size();
        }

        public Object getItem(int position) {
            return this.logs.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView == null){

                convertView = this.inflater.inflate(R.layout.item_log, null);

                TextView dateTextView = (TextView) convertView.findViewById(R.id.date);
                TextView serviceNameTextView = (TextView) convertView.findViewById(R.id.service_name);
                TextView distanceTextView = (TextView) convertView.findViewById(R.id.distance);
                TextView durationTextView = (TextView) convertView.findViewById(R.id.duration);
                TextView averageSpeedTextView = (TextView) convertView.findViewById(R.id.average_speed);
                RatingBar rideRating = (RatingBar) convertView.findViewById(R.id.rating_bar);

                this.log = (Log) this.getItem(position);

                // Set texts and change styles accordingly
                dateTextView.setText(this.dateFormat.format(this.log.getStartTime()));
                serviceNameTextView.setText(this.log.getServiceName());
                durationTextView.setText(this.durationFormat.format(this.log.getTravelDuration() + this.log.getWaitDuration()));
                distanceTextView.setText(String.valueOf(this.decimalFormat.format(this.log.getDistance())));
                averageSpeedTextView.setText(String.valueOf(this.decimalFormat.format(this.log.getAverageSpeed())));
                rideRating.setRating(this.log.getRating());
            }

            return convertView;
        }

        @Override
        public void onClick(View v) {
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }
    }
}
