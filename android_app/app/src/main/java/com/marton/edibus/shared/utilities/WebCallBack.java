package com.marton.edibus.shared.utilities;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.marton.edibus.App;
import com.marton.edibus.shared.events.LoginRequiredEvent;

import org.json.JSONObject;

import de.greenrobot.event.EventBus;

public abstract class WebCallBack<T> {

    private static final String TAG = WebCallBack.class.getName();

    private EventBus eventBus = EventBus.getDefault();

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

        switch(statusCode){
            case 401:
                this.eventBus.post(new LoginRequiredEvent());
                break;
            default:
                break;
        }
    }
}