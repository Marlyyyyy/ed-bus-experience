package com.marton.edibus.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.marton.edibus.R;
import com.marton.edibus.models.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;


public class DiaryFragment extends RoboFragment {

    private LogAdapter logAdapter;

    private ArrayList<Log> logs;

    // TODO: probably don't inject
    @InjectView(R.id.logs_list)
    private ListView logsListView;

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
    }

    private static class LogAdapter extends BaseAdapter implements View.OnClickListener {

        private ArrayList logs;
        private LayoutInflater inflater = null;
        private Log log = null;
        private DateFormat dateFormat;
        private DateFormat durationFormat;

        public LogAdapter(Activity activity, ArrayList services, Resources resources) {

            this.logs = services;
            this.dateFormat = android.text.format.DateFormat.getDateFormat(activity.getApplicationContext());
            this.durationFormat = this.dateFormat = new SimpleDateFormat("mm:ss");

            this.inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {

            if(this.logs.size() <= 0){
                return 1;
            }

            return this.logs.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            this.log = (Log) this.logs.get(position);

            View view = convertView;
            if(convertView == null){
                view = this.inflater.inflate(R.layout.item_log, null);
            }

            TextView dateTextView = (TextView) view.findViewById(R.id.date);
            TextView serviceNameTextView = (TextView) view.findViewById(R.id.service_name);
            TextView distanceTextView = (TextView) view.findViewById(R.id.distance);
            TextView durationTextView = (TextView) view.findViewById(R.id.duration);
            TextView averageSpeedTextView = (TextView) view.findViewById(R.id.average_speed);

            // Set texts and change styles accordingly
            dateTextView.setText(this.dateFormat.format(this.log.getStartTime()));
            serviceNameTextView.setText(this.log.getServiceName());
            durationTextView.setText(this.durationFormat.format(this.log.getTravelDuration() + this.log.getWaitDuration()));
            distanceTextView.setText(String.valueOf(this.log.getDistance()));
            averageSpeedTextView.setText(String.valueOf(this.log.getAverageSpeed()));

            return view;
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
