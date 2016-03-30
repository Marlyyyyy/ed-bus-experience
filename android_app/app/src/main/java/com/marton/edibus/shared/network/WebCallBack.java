package com.marton.edibus.shared.network;

import android.content.Context;
import android.widget.Toast;

import com.marton.edibus.App;
import com.marton.edibus.shared.events.LoginRequiredEvent;

import org.json.JSONObject;

import de.greenrobot.event.EventBus;

public abstract class WebCallBack<T> {

    private EventBus eventBus = EventBus.getDefault();

    // Executed when the web-service returns 200 OK response
    public abstract void onSuccess(T data);

    // Executed when the web-service does not return 200 OK response
    public void onFailure(int statusCode, JSONObject response){

        Context context = App.getAppContext();

        String message = response == null ? "No response was returned" : response.toString();
        Toast toast = Toast.makeText(context, String.format("Error: %d - %s",
                        statusCode, message), Toast.LENGTH_LONG);
        toast.show();

        if (statusCode == 401 || statusCode == 403) {
            this.eventBus.post(new LoginRequiredEvent());
        }
    }
}