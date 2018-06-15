package ru.assisttech.sdk.processor;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import ru.assisttech.sdk.AssistResult;
import ru.assisttech.sdk.network.AssistNetworkEngine;
import ru.assisttech.sdk.network.HttpResponse;
import ru.assisttech.sdk.storage.AssistTransaction;
import ru.assisttech.sdk.xml.XmlHelper;

/**
 * Класс вызова сервиса запроса статуса заказа {@link ru.assisttech.sdk.AssistAddress#GET_ORDER_STATUS_SERVICE}
 */
public class AssistResultProcessor extends AssistBaseProcessor {

    private static final String TAG = "AssistResultProvider";

    public AssistResultProcessor(Context context, AssistProcessorEnvironment environment) {
        super(context, environment);
    }

    @Override
    protected void run() {
        String request = buildRequest();

        Log.d(TAG, "Request: " + request);

        getNetEngine().postSOAP(getURL(),
                new NetworkConnectionErrorListener(),
                new OrderResultParser(),
                request/*buildRequest()*/
        );
    }

    @Override
    protected void terminate() {
        getNetEngine().stopTasks();
    }

    // TODO
    private String buildRequest() {

        String regId = getEnvironment().getPayEngine().getRegistrationId();
        String deviceId = getEnvironment().getDeviceId();
        AssistTransaction t = getTransaction();

        return "<soapenv:Envelope " +
                "xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                "xmlns:ws=\"http://www.paysecure.ru/ws/\">" +
                "<soapenv:Header/>" +
                "<soapenv:Body>" +
                "<ws:orderresult>" +
                "<device_id>" + deviceId + "</device_id>" +
                "<registration_id>" + regId + "</registration_id>" +
                "<ordernumber>" + t.getOrderNumber() + "</ordernumber>" +
                "<merchant_id>" + t.getMerchantID() + "</merchant_id>" +
                "<date>" + t.getOrderDateUTC() + "</date>" +
                "</ws:orderresult>" +
                "</soapenv:Body>" +
                "</soapenv:Envelope>";
    }

    /**
     * Parses order status response
     */
    private class OrderResultParser implements AssistNetworkEngine.NetworkResponseProcessor {

        private OrderResult orderResult;
        private Fault fault;

        private boolean isError;
        private String errorMessage;

        @Override
        public void asyncProcessing(HttpResponse response) {
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                parser.setInput(new ByteArrayInputStream(response.getData().getBytes()), null);

                int eventType = parser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        switch (parser.getName()) {
                            case OrderResult.TAG:
                                orderResult = new OrderResult(parser);
                                break;
                            case "Fault":
                                fault = new Fault(parser);
                                break;
                        }
                    }
                    eventType = parser.next();
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
                if (orderResult != null && orderResult.getOrder() != null) {
                    result.setOrderState(orderResult.getOrder().orderstate);
                    result.setBillNumber(orderResult.getOrder().billnumber);
                    if (orderResult.getOrder().getLastOperation() != null) {
                        result.setExtra(orderResult.getOrder().getLastOperation().usermessage);
                        result.setCardholder(orderResult.getOrder().getLastOperation().cardholder);
                        result.setMeanNumber(orderResult.getOrder().getLastOperation().meannumber);
                        result.setMeantypeName(orderResult.getOrder().getLastOperation().meantypename);
                        result.setCardExpirationDate(orderResult.getOrder().getLastOperation().cardexpirationdate);
                    }
                } else if (fault != null) {
                    result.setExtra(fault.faultcode + " (" + fault.faultstring + ")");
                }
                if (hasListener()) {
                    getListener().onFinished(tID, result);
                }
            }
            finish();
        }
    }

    /**
     * Represents XML tag "orderresult" in SOAP response from orderresult.cfm
     */
    private class OrderResult {
        static final String TAG = "orderresult";
        ArrayList<Order> orders = new ArrayList<>();

        OrderResult(XmlPullParser parser) throws XmlPullParserException, IOException {
            while (!endOfTag(parser, OrderResult.TAG)) {
                parser.next();
                if (isTag(parser, Order.TAG)) {
                    orders.add(new Order(parser));
                }
            }
        }

        Order getOrder() {
            if (orders.isEmpty()) {
                return null;
            } else {
                return orders.get(orders.size() - 1);
            }
        }
    }

    /**
     * Represents XML tag "order" in SOAP response from orderresult.cfm
     */
    private class Order {
        static final String TAG = "order";
        String ordernumber;
        String billnumber;
        String orderstate;
        ArrayList<Operation> operations = new ArrayList<>();

        Order(XmlPullParser parser) throws XmlPullParserException, IOException {
            while (!endOfTag(parser, Order.TAG)) {
                if (parser.next() == XmlPullParser.START_TAG) {
                    switch (parser.getName()) {
                        case "ordernumber":
                            ordernumber = XmlHelper.readValue(parser, "ordernumber");
                            break;
                        case "billnumber":
                            billnumber = XmlHelper.readValue(parser, "billnumber");
                            break;
                        case "orderstate":
                            orderstate = XmlHelper.readValue(parser, "orderstate");
                            break;
                        case Operation.TAG:
                            operations.add(new Operation(parser));
                            break;
                    }
                }
            }
        }

        Operation getLastOperation() {
            if (operations.isEmpty()) {
                return null;
            } else {
                return operations.get(operations.size() - 1);
            }
        }
    }

    /**
     * Represents XML tag "operation" in SOAP response from orderresult.cfm
     */
    private class Operation {
        static final String TAG = "operation";
        String operationtype;
        String operationstate;
        String responsecode;
        String approvalcode;
        String usermessage;
        String cardholder;
        String cardexpirationdate;
        String meantypename;
        String meannumber;

        Operation(XmlPullParser parser) throws XmlPullParserException, IOException {
            while (!endOfTag(parser, Operation.TAG)) {
                if (parser.next() == XmlPullParser.START_TAG) {
                    switch (parser.getName()) {
                        case "operationtype":
                            operationtype = XmlHelper.readValue(parser, "operationtype");
                            break;
                        case "operationstate":
                            operationstate = XmlHelper.readValue(parser, "operationstate");
                            break;
                        case "responsecode":
                            responsecode = XmlHelper.readValue(parser, "responsecode");
                            break;
                        case "approvalcode":
                            approvalcode = XmlHelper.readValue(parser, "approvalcode");
                            break;
                        case "usermessage":
                            usermessage = XmlHelper.readValue(parser, "usermessage");
                            break;
                        case "cardholder":
                            cardholder = XmlHelper.readValue(parser, "cardholder");
                            break;
                        case "cardexpirationdate":
                            cardexpirationdate = XmlHelper.readValue(parser, "cardexpirationdate");
                            break;
                        case "meantypename":
                            meantypename = XmlHelper.readValue(parser, "meantypename");
                            break;
                        case "meannumber":
                            meannumber = XmlHelper.readValue(parser, "meannumber");
                            break;
                    }
                }
            }
        }
    }

    /**
     * Represents XML tag "Fault" in SOAP response from orderresult.cfm
     */
    private class Fault {
        static final String TAG = "Fault";
        String faultcode;
        String faultstring;

        Fault(XmlPullParser parser) throws XmlPullParserException, IOException {
            while (!endOfTag(parser, Fault.TAG)) {
                if (parser.next() == XmlPullParser.START_TAG) {
                    switch (parser.getName()) {
                        case "faultcode":
                            faultcode = XmlHelper.readValue(parser, "faultcode");
                            break;
                        case "faultstring":
                            faultstring = XmlHelper.readValue(parser, "faultstring");
                            break;
                    }
                }
            }
        }
    }

    private boolean endOfTag(XmlPullParser parser, String tagName) throws XmlPullParserException {
        return parser.getEventType() == XmlPullParser.END_TAG && parser.getName().equals(tagName);
    }

    private boolean isTag(XmlPullParser parser, String tagName) throws XmlPullParserException {
        return parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals(tagName);
    }
}
