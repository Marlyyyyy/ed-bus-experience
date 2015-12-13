package com.marton.edibus;

import android.util.Log;
import android.widget.Toast;

public abstract class WebCallBack<T> {

    private static final String TAG = WebCallBack.class.getName();

    public abstract void onSuccess(T data);

    public void onFailure(int statusCode, String message){
        Log.e(TAG, String.format("%d, %s", statusCode, message));
        Toast toast = Toast.makeText(App.getAppContext(), String.format("%d: %s", statusCode, message), Toast.LENGTH_LONG);
        toast.show();
    }
}