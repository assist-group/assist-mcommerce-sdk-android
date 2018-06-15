package ru.assisttech.sdk;

public enum FormatType {
    CSV("1"),
    WDDX("2"),
    XML("3"),
    SOAP("4");

    private final String id;

    FormatType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
