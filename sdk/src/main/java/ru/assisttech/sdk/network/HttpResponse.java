package ru.assisttech.sdk.network;

import android.text.TextUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpResponse {

    private int responseCode;
    private Map<String, List<String>> headers;
    private String data;

    public HttpResponse(int responseCode, Map<String, List<String>> headers, String data) {
        this.responseCode = responseCode;
        this.headers = headers;
        this.data = data;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public List<String> getHeader(String name) {
        return headers.get(name);
    }

    public boolean hasHeader(String name) {
        return headers.containsKey(name);
    }

    public String getData() {
        return data;
    }

    public boolean hasData() {
        return !TextUtils.isEmpty(data);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("HttpResponse:\n[\n");
        builder.append("Response code: ").append(responseCode).append("\n");
        builder.append("Response headers:\n");
        if (headers != null && !headers.isEmpty()) {
            Set<Map.Entry<String, List<String>>> entrySet = headers.entrySet();
            for (Map.Entry<String, List<String>> entry : entrySet) {
                StringBuilder valueBuilder = new StringBuilder();
                List<String> valueList = entry.getValue();
                for (String value : valueList) {
                    valueBuilder.append(value).append(" ");
                }
                builder.append("    ")
                        .append(entry.getKey())
                        .append(" : ")
                        .append(valueBuilder.toString())
                        .append("\n");
            }
        } else {
            builder.append("    no headers\n");
        }
        builder.append("Response data:\n");
        if (TextUtils.isEmpty(data)) {
            builder.append("    no data\n");
        } else {
            builder.append(data).append("\n");
        }
        builder.append("]");
        return builder.toString();
    }
}
