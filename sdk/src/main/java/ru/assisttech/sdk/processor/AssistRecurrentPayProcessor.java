package ru.assisttech.sdk.processor;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import ru.assisttech.sdk.AssistPaymentData;
import ru.assisttech.sdk.AssistResult;
import ru.assisttech.sdk.AssistSDK;
import ru.assisttech.sdk.FieldName;
import ru.assisttech.sdk.FormatType;
import ru.assisttech.sdk.identification.InstallationInfo;
import ru.assisttech.sdk.network.AssistNetworkEngine;
import ru.assisttech.sdk.network.HttpResponse;

public class AssistRecurrentPayProcessor extends AssistBaseProcessor {

    public static final String TAG = AssistRecurrentPayProcessor.class.getSimpleName();

    public AssistRecurrentPayProcessor(Context context, AssistProcessorEnvironment environment) {
        super(context, environment);
    }

    @Override
    protected void run() {
        super.run();
        /*
         * Manual card data input
         */
        getNetEngine().postRequest(getURL(),
                new NetworkConnectionErrorListener(),
                new RecurrentPayResponseParser(),
                buildRequest()
        );
    }

    @Override
    protected void terminate() {
        getNetEngine().stopTasks();
    }

    /**
     * Web request assembling (HTTP protocol, URL encoded pairs)
     */
    String buildRequest() {

        AssistPaymentData data = getEnvironment().getData();
        InstallationInfo info = getEnvironment().getPayEngine().getInstInfo();

        StringBuilder content = new StringBuilder();
        try {
            Map<String, String> params = data.getFields();

            content.append(URLEncoder.encode(FieldName.Merchant_ID, "UTF-8")).append("=");
            content.append(URLEncoder.encode(data.getMerchantID(), "UTF-8")).append("&");

            content.append(URLEncoder.encode(FieldName.Login, "UTF-8")).append("=");
            content.append(URLEncoder.encode(data.getLogin(), "UTF-8")).append("&");

            content.append(URLEncoder.encode(FieldName.Password, "UTF-8")).append("=");
            content.append(URLEncoder.encode(data.getPassword(), "UTF-8")).append("&");

            content.append(URLEncoder.encode(FieldName.OrderNumber, "UTF-8")).append("=");
            content.append(URLEncoder.encode(params.get(FieldName.OrderNumber), "UTF-8")).append("&");

            if (params.get(FieldName.OrderComment) != null) {
                content.append(URLEncoder.encode(FieldName.OrderComment, "UTF-8")).append("=");
                content.append(URLEncoder.encode(params.get(FieldName.OrderComment), "UTF-8")).append("&");
            }

            //region v 1.4.2
            content.append(URLEncoder.encode(FieldName.BillNumber, "UTF-8")).append("=");
            content.append(URLEncoder.encode(params.get(FieldName.BillNumber), "UTF-8")).append("&");

            content.append(URLEncoder.encode(FieldName.Amount, "UTF-8")).append("=");
            content.append(URLEncoder.encode(params.get(FieldName.Amount), "UTF-8")).append("&");

            content.append(URLEncoder.encode(FieldName.Currency, "UTF-8")).append("=");
            content.append(URLEncoder.encode(params.get(FieldName.Currency), "UTF-8")).append("&");
            //endregion

            //region v 1.5.0
            content.append(URLEncoder.encode(FieldName.OrderAmount, "UTF-8")).append("=");
            content.append(URLEncoder.encode(params.get(FieldName.OrderAmount), "UTF-8")).append("&");

            content.append(URLEncoder.encode(FieldName.OrderCurrency, "UTF-8")).append("=");
            content.append(URLEncoder.encode(params.get(FieldName.OrderCurrency), "UTF-8")).append("&");
            //endregion

            content.append(URLEncoder.encode(FieldName.Device, "UTF-8")).append("=");
            content.append(URLEncoder.encode("CommerceSDK", "UTF-8")).append("&");

            content.append(URLEncoder.encode(FieldName.DeviceUniqueID, "UTF-8")).append("=");
            content.append(URLEncoder.encode(info.getDeiceUniqueId(), "UTF-8")).append("&");

            content.append(URLEncoder.encode(FieldName.ApplicationName, "UTF-8")).append("=");
            content.append(URLEncoder.encode(info.appName(), "UTF-8")).append("&");

            content.append(URLEncoder.encode(FieldName.ApplicationVersion, "UTF-8")).append("=");
            content.append(URLEncoder.encode(info.versionName(), "UTF-8")).append("&");

            content.append(URLEncoder.encode(FieldName.SDKVersion, "UTF-8")).append("=");
            content.append(URLEncoder.encode(AssistSDK.getSdkVersion(), "UTF-8")).append("&");

            content.append(URLEncoder.encode(FieldName.Format, "UTF-8")).append("=");
            content.append(URLEncoder.encode(FormatType.SOAP.getId(), "UTF-8")).append("&");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Request:" + content);

        return content.toString();
    }

    private class RecurrentPayResponseParser implements AssistNetworkEngine.NetworkResponseProcessor {

        protected Map<String, String> responseFields;
        protected String testField = "responsecode";

        protected boolean isError;
        protected String errorMessage;

        public RecurrentPayResponseParser() {
            responseFields = new HashMap<>();

            responseFields.put("ordernumber", "");
            responseFields.put("billnumber", "");
            responseFields.put("testmode", "");
            responseFields.put("ordercomment", "");
            responseFields.put("orderamount", "");
            responseFields.put("ordercurrency", "");
            responseFields.put("amount", "");
            responseFields.put("currency", "");
            responseFields.put("rate", "");
            responseFields.put("firstname", "");
            responseFields.put("lastname", "");
            responseFields.put("middlename", "");
            responseFields.put("email", "");
            responseFields.put("ipaddress", "");
            responseFields.put("meantypename", "");
            responseFields.put("meansubtype", "");
            responseFields.put("meannumber", "");
            responseFields.put("cardholder", "");
            responseFields.put("cardexpirationdate", "");
            responseFields.put("issuebank", "");
            responseFields.put("bankcountry", "");
            responseFields.put("orderdate", "");
            responseFields.put("orderstate", "");
            responseFields.put("responsecode", "");
            responseFields.put("message", "");
            responseFields.put("customermessage", "");
            responseFields.put("recommendation", "");
            responseFields.put("approvalcode", "");
            responseFields.put("protocoltypename", "");
            responseFields.put("processingname", "");
            responseFields.put("operationtype", "");
            responseFields.put("packetdate", "");
            responseFields.put("signature", "");
            responseFields.put("pareq", "");
            responseFields.put("ascurl", "");

            responseFields.put("faultcode", "");
            responseFields.put("faultstring", "");
        }

        @Override
        public void asyncProcessing(HttpResponse response) {

            Log.d(TAG, "RecurrentPayResponseParser.asyncProcessing()");
            Log.d(TAG, "Response: " + response);

            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(new ByteArrayInputStream(response.getData().getBytes()), null);

                int eventType = parser.getEventType();

                String currentTag = "";
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        currentTag = parser.getName();
                    } else if (eventType == XmlPullParser.END_TAG) {
                        currentTag = "";
                    } else if (eventType == XmlPullParser.TEXT) {
                        if (responseFields.containsKey(currentTag)) {
                            responseFields.put(currentTag, parser.getText());
                        }
                    }
                    eventType = parser.next();
                }
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
                isError = true;
            }
        }

        @Override
        public void syncPostProcessing() {
            Log.d(TAG, "syncPostProcessing()");

            long tID = getTransaction() != null ? getTransaction().getId() : -1L;
            if (isError) {
                if (hasListener()) {
                    getListener().onError(tID, errorMessage);
                }
            } else {
                AssistResult result = new AssistResult();
                if (!responseFields.get(testField).isEmpty()) {
                    /* Success */
                    result.setApprovalCode(responseFields.get("approvalcode"));
                    result.setBillNumber(responseFields.get("billnumber"));
                    result.setExtra(responseFields.get("responsecode") + " " + responseFields.get("customermessage"));
                    result.setMeantypeName(responseFields.get("meantypename"));
                    result.setMeanNumber(responseFields.get("meannumber"));
                    result.setCardholder(responseFields.get("cardholder"));
                    result.setCardExpirationDate(responseFields.get("cardexpirationdate"));

                    if ("AS000".equalsIgnoreCase(responseFields.get("responsecode"))) {
                        result.setOrderState(AssistResult.OrderState.APPROVED);
                    } else {
                        result.setOrderState(AssistResult.OrderState.DECLINED);
                    }
                } else {
                    /* Fault */
                    result.setExtra(responseFields.get("faultcode") + ": " + responseFields.get("faultstring"));
                }
                if (hasListener()) {
                    getListener().onFinished(tID, result);
                }
            }
            finish();
        }
    }
}
