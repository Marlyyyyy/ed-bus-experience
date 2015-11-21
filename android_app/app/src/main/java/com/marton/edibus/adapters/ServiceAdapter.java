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
import com.marton.edibus.models.Service;

import java.util.ArrayList;


public class ServiceAdapter extends BaseAdapter implements View.OnClickListener {

    private Activity activity;
    private ArrayList services;
    private LayoutInflater inflater = null;
    public Resources resources;
    Service service = null;

    public ServiceAdapter(Activity activity, ArrayList services, Resources resources) {

        this.activity = activity;
        this.services = services;
        this.resources = resources;

        this.inflater = (LayoutInflater)activity.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {

        if(this.services.size() <= 0){
            return 1;
        }

        return this.services.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if( convertView == null ){
            view = this.inflater.inflate(R.layout.item_service, null);
        }

        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView descriptionTextView = (TextView) view.findViewById(R.id.description);


        this.service = null;
        this.service = (Service) this.services.get(position);

        nameTextView.setText(String.valueOf(this.service.getId()));
        descriptionTextView.setText(String.valueOf(this.service.getDescription()));

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
