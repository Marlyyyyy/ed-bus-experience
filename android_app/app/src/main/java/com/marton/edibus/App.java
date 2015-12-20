package com.marton.edibus;

import android.content.Context;


public class App extends com.orm.SugarApp{

    private static Context context;

    public void onCreate(){
        super.onCreate();
        App.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return App.context;
    }
}
