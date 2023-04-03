package ru.assisttech.sdk.processor;

import ru.assisttech.sdk.AssistMerchant;
import ru.assisttech.sdk.AssistPaymentData;
import ru.assisttech.sdk.engine.AssistPayEngine;

/**
 * Provides environment for AssistService
 */
public class AssistProcessorEnvironment {

    private final AssistPayEngine engine;     /* pay engine instance that started service */
    private final AssistMerchant merchant;    /* Assist registered merchant - money destination */
    private final AssistPaymentData data;     /* payment parameters required by Assist system */
    private String deviceId;

    public AssistProcessorEnvironment(AssistPayEngine engine,
                                      AssistMerchant merchant,
                                      AssistPaymentData data) {
        this.engine = engine;
        this.merchant = merchant;
        this.data = data;
    }

    public AssistPayEngine getPayEngine() {
        return engine;
    }

    public AssistMerchant getMerchant() {
        return merchant;
    }

    public AssistPaymentData getData() {
        return data;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }
}