package ru.assisttech.sdk.identification;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;

public class InstallationInfo {

    private static final String INSTALLATION = ".mobilepayinstallation";
    private static final String APP_REG_ID = "ApplicationRegId";

    private static InstallationInfo instance;
    private Context context;
    private SharedPreferences settings;
    private String appName;
    private String versionName;

    public static InstallationInfo getInstance(Context context) {
        if (instance == null)
            instance = new InstallationInfo(context);
        return instance;
    }

    private InstallationInfo(Context context) {
        this.context = context;
        getNames(context);
        // Restore preferences
        String name = context.getApplicationInfo().packageName + INSTALLATION;
        settings = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public String getAppRegId() {
        String deviceId = settings.getString(APP_REG_ID, null);
        return (deviceId != null) ? settings.getString(deviceId, null) : null;
    }

    public String getDeiceUniqueId() {
        return settings.getString(APP_REG_ID, null);
    }

    public void setAppRegID(String deviceId, String registrationID) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(APP_REG_ID, deviceId);
        editor.putString(deviceId, registrationID);
        // Commit the edits!
        editor.commit();
    }

    public String appName() {
        return appName;
    }

    public String versionName() {
        return versionName;
    }

    private void getNames(Context context) {

        Resources res = context.getResources();
        appName = res.getText(res.getIdentifier("app_name", "string", context.getPackageName())).toString();

        versionName = "";
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo pacInfo = manager.getPackageInfo(context.getPackageName(), 0);
            appName = pacInfo.packageName;
            versionName = pacInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
