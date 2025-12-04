package com.zawww.e_simaksi.util;

import android.view.View;
import com.google.android.material.snackbar.Snackbar;

public class ErrorHandler {

    /**
     * Displays a Snackbar with a specified error message.
     *
     * @param view    The view to find a parent from, to display the Snackbar.
     * @param message The message to show in the Snackbar.
     */
    public static void showError(View view, String message) {
        if (view != null && message != null && !message.isEmpty()) {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
        }
    }
}
