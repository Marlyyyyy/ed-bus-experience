package com.marton.edibus.utilities;

import android.content.res.Resources;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.view.View;

import com.marton.edibus.R;

import java.util.HashMap;
import java.util.Map;


public class SnackbarManager {

    public static void showSnackbar(View view, String type, String message, Resources res) {
        Map<String, String> typesMap = new HashMap<String, String>();
        typesMap.put("success", res.getString(R.string.success_base));
        typesMap.put("error", res.getString(R.string.error_base));

        if (!typesMap.containsKey(type))
            throw new IllegalArgumentException("Allowed values for 'type' are: " + typesMap.keySet().toString());

        Snackbar.make(
                view,
                Html.fromHtml(typesMap.get(type) + message),
                Snackbar.LENGTH_SHORT
        ).show();
    }
}
