package com.marton.edibus.utilities;

import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.marton.edibus.App;
import com.marton.edibus.R;


public class SnackbarManager {

    public static void showSucess(View view, String message){
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(ContextCompat.getColor(App.getAppContext(), R.color.snackbar_success));

        snackbar.show();
    }

    public static void showSnackbar(View view, String message) {

        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(ContextCompat.getColor(App.getAppContext(), R.color.snackbar_error));

        snackbar.show();
    }
}
