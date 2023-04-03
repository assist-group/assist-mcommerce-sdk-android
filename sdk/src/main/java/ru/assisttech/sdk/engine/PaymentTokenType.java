package ru.assisttech.sdk.engine;

import androidx.annotation.NonNull;

public enum PaymentTokenType {

    GOOGLE_PAY("2"),
    SAMSUNG_PAY("3"),
    MIR_PAY("6");

    PaymentTokenType(String value) {
        this.value = value;
    }

    private final String value;

    @NonNull
    @Override
    public String toString() {
        return value;
    }
}