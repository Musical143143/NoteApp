package com.rayan.noteapp;

import android.content.Context;
import android.content.SharedPreferences;

public final class AppLock {

    private static final String PREF =
            "noteapp_security";

    private static final String KEY_ENABLED =
            "pin_enabled";

    private static final String KEY_PIN =
            "pin";

    private AppLock() {
    }

    private static SharedPreferences prefs(
            Context context) {

        return context.getSharedPreferences(
                PREF,
                Context.MODE_PRIVATE
        );
    }

    public static boolean isEnabled(
            Context context) {

        return prefs(context)
                .getBoolean(
                        KEY_ENABLED,
                        false
                );
    }

    public static String getPin(
            Context context) {

        return prefs(context)
                .getString(
                        KEY_PIN,
                        ""
                );
    }

    public static void enable(
            Context context,
            String pin) {

        prefs(context)
                .edit()
                .putBoolean(
                        KEY_ENABLED,
                        true
                )
                .putString(
                        KEY_PIN,
                        pin
                )
                .apply();
    }

    public static void disable(
            Context context) {

        prefs(context)
                .edit()
                .putBoolean(
                        KEY_ENABLED,
                        false
                )
                .remove(KEY_PIN)
                .apply();
    }

    public static boolean verify(
            Context context,
            String pin) {

        String saved =
                getPin(context);

        return saved != null
                && saved.equals(pin);
    }
}
