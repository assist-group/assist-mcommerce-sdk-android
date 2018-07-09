package ru.assisttech.sdk;

import android.app.Activity;

import ru.assisttech.sdk.engine.AssistPayEngine;

/**
 *
 */
public class AssistSDK {

    public static AssistPayEngine getPayEngine(Activity activity) {
        return AssistPayEngine.getInstance(activity.getApplicationContext());
    }

    public static String getSdkVersion() {
        return BuildConfig.VERSION_NAME;
    }
}
