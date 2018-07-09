package ru.assisttech.sdk.processor;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayInputStream;

import ru.assisttech.sdk.AssistMerchant;
import ru.assisttech.sdk.AssistResult;
import ru.assisttech.sdk.network.AssistNetworkEngine;
import ru.assisttech.sdk.network.HttpResponse;
import ru.assisttech.sdk.xml.XmlHelper;

public class AssistCancelProcessor extends AssistBaseProcessor {

    private static final String TAG = "AssistCancelProvider";

    public AssistCancelProcessor(Context context, AssistProcessorEnvironment environment) {
        super(context, environment);
    }

    @Override
    protected void run() {

        AssistMerchant m = getEnvironment().getMerchant();
        String request = buildRequest(m.getID(),
                m.getLogin(),
                m.getPassword(),
                getTransaction().getResult().getBillNumber()
        );

        Log.d(TAG, "Request: " + request);
        getNetEngine().postSOAP(getURL(),
                new NetworkConnectionErrorListener(),
                new OrderCancelParser(),
                request
        );
    }

    @Override
    protected void terminate() {
        getNetEngine().stopTasks();
    }

    private String buildRequest(String merchantID, String login, String password, String billNumber) {

        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws=\"http://www.paysecure.ru/ws/\">" +
                "<soapenv:Header/>" +
                "<soapenv:Body>" +
                "<ws:WSCancelRequestParams>" +
                "<billnumber>" + billNumber + "</billnumber>" +
                "<merchant_id>" + merchantID + "</merchant_id>" +
                "<login>" + login + "</login>" +
                "<password>" + password + "</password>" +
                "</ws:WSCancelRequestParams>" +
                "</soapenv:Body>" +
                "</soapenv:Envelope>";
    }

    /**
     * Parses order status response
     */
    private class OrderCancelParser implements AssistNetworkEngine.NetworkResponseProcessor {

        private String billnumber;
        private String orderstate;

        private boolean isError;
        private String errorMessage;

        public OrderCancelParser() {
        }

        @Override
        public void asyncProcessing(HttpResponse response) {
            try {
                Log.d(TAG, "Response: " + response);

                XmlPullParser parser = Xml.newPullParser();

                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(new ByteArrayInputStream(response.getData().getBytes()), null);
                parser.nextTag();
                parser.require(XmlPullParser.START_TAG, null, "soapenv:Envelope");

                if (XmlHelper.next(parser, "soapenv:Body")) {
                    if (XmlHelper.next(parser, "ws:WSCancelResponseParams") && XmlHelper.next(parser, "order")) {
                        while (XmlHelper.nextTag(parser)) {
                            String tName = parser.getName();
                            switch (tName) {
                                case "billnumber":
                                    billnumber = XmlHelper.readValue(parser, "billnumber");
                                    break;
                                case "orderstate":
                                    orderstate = XmlHelper.readValue(parser, "orderstate");
                                    break;
                                default:
                                    XmlHelper.skip(parser);
                                    break;
                            }
                        }
                    }
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
            } else {
                AssistResult result = new AssistResult();
                result.setOrderState(orderstate);
                result.setBillNumber(billnumber);
                if (hasListener()) {
                    getListener().onFinished(tID, result);
                }
            }
            finish();
        }
    }
}
