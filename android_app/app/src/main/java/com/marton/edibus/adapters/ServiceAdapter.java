package com.marton.edibus.adapters;


import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.marton.edibus.R;
import com.marton.edibus.models.Service;

import java.util.ArrayList;


public class ServiceAdapter extends BaseAdapter implements View.OnClickListener {

    private ArrayList services;
    private LayoutInflater inflater = null;
    private Service service = null;
    private boolean smallServiceItem = false;
    private LinearLayout latestClickedView;

    public ServiceAdapter(Activity activity, ArrayList services, Resources resources) {

        this.services = services;

        this.inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        this.service = (Service) this.services.get(position);

        View view = convertView;
        if(convertView == null){
            if (this.smallServiceItem){
                view = this.inflater.inflate(R.layout.item_service_small, null);
            }else{
                view = this.inflater.inflate(R.layout.item_service_large, null);

                // This view only exists in the large layout
                TextView typeTextView = (TextView) view.findViewById(R.id.type);
                typeTextView.setText(this.service.getType());
            }
        }

        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView descriptionTextView = (TextView) view.findViewById(R.id.description);

        // Set texts and change styles accordingly
        nameTextView.setText(this.service.getName());
        descriptionTextView.setText(this.service.getDescription());

        return view;
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    public void setSmallServiceItem(boolean smallServiceItem) {
        this.smallServiceItem = smallServiceItem;
    }
}
