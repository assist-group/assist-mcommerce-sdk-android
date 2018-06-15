package ru.assisttech.sdk.identification;

import android.os.Build;

import java.util.Locale;
import java.util.UUID;

/**
 * Информация об аппарате на который установлено приложение
 */
public class SystemInfo {

    private static final int UNIQUE_ID_STRING_MAX_LENGTH = 50;

    private static SystemInfo instance;
    private String uniqueId = "";

    public static SystemInfo getInstance() {
        if (instance == null) {
            instance = new SystemInfo();
        }
        return instance;
    }

    public String uniqueId() {
        return uniqueId;
    }

    private SystemInfo() {
        int androidSdk = android.os.Build.VERSION.SDK_INT;
        String model = Build.MODEL;
        String manufacturer = Build.MANUFACTURER;
        uniqueId = String.format(Locale.US, "%1$d,%2$s,%3$.15s,%4$s", androidSdk, manufacturer, model, UUID.randomUUID().toString());
        if (uniqueId.length() >= UNIQUE_ID_STRING_MAX_LENGTH) {
            uniqueId = uniqueId.substring(0, UNIQUE_ID_STRING_MAX_LENGTH - 1);
        }
    }
}
