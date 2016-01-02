package com.marton.edibus;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import org.json.JSONObject;

public abstract class WebCallBack<T> {

    private static final String TAG = WebCallBack.class.getName();

    public abstract void onSuccess(T data);

    public void onFailure(int statusCode, JSONObject response){

        Context context = App.getAppContext();

        String message;
        if (response == null){
            message = "No response was returned";
        }else{
            message = response.toString();
        }

        Log.e(TAG, String.format("%d, %s", statusCode, message));
        Toast toast = Toast.makeText(context, String.format("Error: %d - %s", statusCode, message), Toast.LENGTH_LONG);
        toast.show();
    }
}