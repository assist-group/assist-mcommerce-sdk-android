package ru.assisttech.sdk.network;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * HTTP request parameters include:
 * - request method GET or POST
 * - HTTP header fields (added with addProperty() method)
 * - request data (payload)
 */
class HttpRequest {

    private String method;
    private String data;
    private Map<String, String> properties;

    public HttpRequest() {
        properties = new HashMap<>();
    }

    public void setMethod(String value) {
        method = value;
    }

    public String getMethod() {
        return method;
    }

    public void setData(String value) {
        data = value;
    }

    public String getData() {
        return data;
    }

    public boolean hasData() {
        return !TextUtils.isEmpty(data);
    }

    public void addProperty(String name, String value) {
        properties.put(name, value);
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("HttpRequest:\n[\n");
        builder.append("Request method: ").append(method).append("\n");
        builder.append("Request headers:\n");
        if (properties != null && !properties.isEmpty()) {
            Set<Map.Entry<String, String>> entrySet = properties.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                builder.append("    ")
                        .append(entry.getKey())
                        .append(" : ")
                        .append(entry.getValue())
                        .append("\n");
            }
        } else {
            builder.append("    no headers\n");
        }
        builder.append("Request data:\n");
        if (TextUtils.isEmpty(data)) {
            builder.append("    no data\n");
        } else {
            builder.append(data).append("\n");
        }
        builder.append("]");
        return builder.toString();
    }
}
