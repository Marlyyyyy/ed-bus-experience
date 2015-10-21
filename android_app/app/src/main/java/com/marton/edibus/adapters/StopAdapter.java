package com.marton.edibus.adapters;


import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.marton.edibus.R;
import com.marton.edibus.models.Stop;

import java.util.ArrayList;


public class StopAdapter extends BaseAdapter implements View.OnClickListener {

    private Activity activity;
    private ArrayList stops;
    private static LayoutInflater inflater = null;
    public Resources res;
    Stop stop = null;

    public StopAdapter(Activity activity, ArrayList stops, Resources resources) {

        this.activity = activity;
        this.stops = stops;
        res = resources;

        inflater = (LayoutInflater)activity.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {

        if(stops.size() <= 0){
            return 1;
        }

        return stops.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    /********* Create a holder Class to contain inflated xml file elements *********/
    public static class ViewHolder{

        public TextView text;
        public TextView text1;
        public TextView textWide;
        public ImageView image;

    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        TextView text;

        if( convertView == null ){
            //We must create a View:
            view = inflater.inflate(R.layout.stop_item, null);
        }

        text = (TextView) view.findViewById(R.id.text);

        stop = null;
        stop = (Stop) stops.get(position);

        text.setText(String.valueOf(stop.getId()));

        return view;
    }

    @Override
    public void onClick(View v) {
        Log.v("CustomAdapter", "=====Row button clicked=====");
    }

    @Override
    public boolean isEnabled(int position)
    {
        return true;
    }
}
