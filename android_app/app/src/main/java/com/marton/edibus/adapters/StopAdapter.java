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

/********* Adapter class extends with BaseAdapter and implements with OnClickListener ************/
public class StopAdapter extends BaseAdapter implements View.OnClickListener {



    /*********** Declare Used Variables *********/
    private Activity activity;
    private ArrayList stops;
    private static LayoutInflater inflater = null;
    public Resources res;
    Stop stop = null;
    int i = 0;

    /*************  CustomAdapter Constructor *****************/
    public StopAdapter(Activity activity, ArrayList stops, Resources resources) {

        /********** Take passed values **********/
        activity = activity;
        this.stops = stops;
        res = resources;

        /***********  Layout inflator to call external xml layout () ***********/
        inflater = ( LayoutInflater )activity.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    /******** What is the size of Passed Arraylist Size ************/
    public int getCount() {

        if(stops.size()<=0)
            return 1;
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

    /****** Depends upon stops size called for each row , Create each ListView row *****/
    public View getView(int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        TextView text = null;

        if( convertView == null ){
            //We must create a View:
            vi = inflater.inflate(R.layout.stop_item, null);
        }

        /****** View Holder Object to contain tabitem.xml file elements ******/

        text = (TextView) vi.findViewById(R.id.text);

        /***** Get each Model object from Arraylist ********/
        stop = null;
        stop = (Stop) stops.get(position);

        /************  Set Model values in Holder elements ***********/

        text.setText(String.valueOf(stop.getId()));

        /******** Set Item Click Listner for LayoutInflater for each row *******/

        vi.setOnClickListener(new OnItemClickListener( position ));

        return vi;
    }

    @Override
    public void onClick(View v) {
        Log.v("CustomAdapter", "=====Row button clicked=====");
    }

    /********* Called when Item click in ListView ************/
    private class OnItemClickListener  implements View.OnClickListener {
        private int mPosition;

        OnItemClickListener(int position){
            mPosition = position;
        }

        @Override
        public void onClick(View arg0) {



        }
    }
}
