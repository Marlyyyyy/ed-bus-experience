package com.marton.edibus;

import android.util.Log;


public abstract class WebCallBack<T> {

    private static final String TAG = WebCallBack.class.getName();

    public abstract void onSuccess(T data);

    public void onFailure(int statusCode, String message){
        Log.e(TAG, String.format("%d, %s", statusCode, message));
    }
}