package ru.assisttech.sdk.processor;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Keep;

import com.google.gson.GsonBuilder;

import ru.assisttech.sdk.AssistMerchant;
import ru.assisttech.sdk.AssistResult;
import ru.assisttech.sdk.network.AssistNetworkEngine;
import ru.assisttech.sdk.network.HttpResponse;

public class AssistDeclineByNumberProcessor extends AssistBaseProcessor {

    private static final String TAG = "AssistDeclByNProcessor";

    private NetworkConnectionErrorListener listener;

    public AssistDeclineByNumberProcessor(Context context, AssistProcessorEnvironment environment) {
        super(context, environment);
    }

    @Override
    protected void run() {
        listener = new NetworkConnectionErrorListener();

        AssistMerchant m = getEnvironment().getMerchant();

        getNetEngine().postJSON(getAuthURL(),
                listener,
                new AuthTokenParser(),
                buildAuthRequest(m.getLogin(), m.getPassword())
        );
    }

    @Override
    protected void terminate() {
        getNetEngine().stopTasks();
    }

    private String buildAuthRequest(String login, String password) {
        return "{" +
                "\"login\": \"" + login + "\"," +
                "\"password\": \"" + password + "\"," +
                "\"audience\":\"POS\"," +
                "\"components\":\"ws/cancelbynumber\"" +
                "}";
    }

    private String buildRequest(String merchantID, String orderNumber) {
        return "{" +
                "\"merchant_id\": \"" + merchantID + "\"," +
                "\"ordernumber\": \"" + orderNumber + "\"" +
                "}";
    }

    private void request(String token) {
        AssistMerchant m = getEnvironment().getMerchant();
        String request = buildRequest(m.getID(), getTransaction().getOrderNumber());

        Log.d(TAG, "Request: " + request);
        getNetEngine().postJSON(getURL(),
                listener,
                new OrderDeclineByNumberParser(),
                request,
                token
        );
    }

    private class AuthTokenParser implements AssistNetworkEngine.NetworkResponseProcessor {

        private AuthToken token;
        private Fault fault;
        private boolean isError;
        private String errorMessage;

        AuthTokenParser() {
        }

        @Override
        public void asyncProcessing(HttpResponse response) {
            try {
                Log.d(TAG, "Response: " + response);

                String responseData = response.getData();
                if (responseData.contains("firstcode")) {
                    fault = new GsonBuilder().create().fromJson(responseData, Fault.class);
                } else if (!responseData.isEmpty() && responseData.contains("_token")) {
                    token = new GsonBuilder().create().fromJson(responseData, AuthToken.class);
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
                isError = true;
            }
        }

        @Override
        public void syncPostProcessing() {

            long tID = getTransaction().getId();
            if (isError) {
                if (hasListener()) {
                    getListener().onError(tID, errorMessage);
                }
                finish();
            } else if (fault != null) {
                if (hasListener()) {
                    getListener().onError(tID, fault.getFirstCode() + ":" + fault.getSecondCode());
                }
                finish();
            } else {
                request(token.getAccessToken());
            }
        }

        @Keep
        class AuthToken {
            String access_token;
            String refresh_token;

            String getAccessToken() {
                return access_token;
            }
            String getRefreshToken() {
                return refresh_token;
            }
        }

        @Keep
        class Fault {
            String firstcode;
            String secondcode;

            String getFirstCode() {
                return firstcode;
            }
            String getSecondCode() {
                return secondcode;
            }
        }
    }

    private class OrderDeclineByNumberParser implements AssistNetworkEngine.NetworkResponseProcessor {

        private String orderstate;
        private Fault fault;
        private boolean isError;
        private String errorMessage;

        OrderDeclineByNumberParser() {
        }

        @Override
        public void asyncProcessing(HttpResponse response) {
            try {
                Log.d(TAG, "Response: " + response);

                String responseData = response.getData();
                if (responseData.contains("firstcode")) {
                    fault = new GsonBuilder().create().fromJson(responseData, Fault.class);
                } else if (!responseData.isEmpty() && responseData.contains("Declined")) {
                    orderstate = "Declined";
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
                isError = true;
            }
        }

        @Override
        public void syncPostProcessing() {

            long tID = getTransaction().getId();
            if (isError) {
                if (hasListener()) {
                    getListener().onError(tID, errorMessage);
                }
            } else if (fault != null) {
                if (hasListener()) {
                    getListener().onError(tID, fault.getFirstCode() + ":" + fault.getSecondCode());
                }
            } else if (orderstate == null) {
                if (hasListener()) {
                    getListener().onError(tID, "order can not be declined");
                }
            } else {
                AssistResult result = new AssistResult();
                result.setOrderState(orderstate);
                if (hasListener()) {
                    getListener().onFinished(tID, result);
                }
            }
            finish();
        }

        @Keep
        class Fault {
            String firstcode;
            String secondcode;

            String getFirstCode() {
                return firstcode;
            }
            String getSecondCode() {
                return secondcode;
            }
        }
    }
}
