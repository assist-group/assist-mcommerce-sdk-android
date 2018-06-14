package ru.assisttech.sdk;

import android.app.Activity;

import ru.assisttech.sdk.engine.AssistPayEngine;

/**
 *
 */
public class AssistSDK {

	private static final String SDK_VERSION = "1.5.0";

	public static AssistPayEngine getPayEngine(Activity activity) {
		return AssistPayEngine.getInstance(activity.getApplicationContext());
	}

    public static String getSdkVersion() {
        return SDK_VERSION;
    }
}
