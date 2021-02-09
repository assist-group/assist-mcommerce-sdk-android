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

import ru.assisttech.sdk.AssistMerchant;
import ru.assisttech.sdk.AssistPaymentData;
import ru.assisttech.sdk.AssistResult;
import ru.assisttech.sdk.FieldName;
import ru.assisttech.sdk.network.AssistNetworkEngine;
import ru.assisttech.sdk.network.HttpResponse;

public class AssistTokenPayProcessor extends AssistBaseProcessor {

    public static final String TAG = AssistTokenPayProcessor.class.getSimpleName();

    private static final String UTF8 = "UTF-8";

    String type;

    public AssistTokenPayProcessor(Context context, AssistProcessorEnvironment environment, String type) {
        super(context, environment);
        this.type = type;
    }

    @Override
    protected void run() {
        getNetEngine().postRequest(getURL(),
                new NetworkConnectionErrorListener(),
                new TokenPayResponseParser(),
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
        AssistMerchant m = getEnvironment().getMerchant();

        StringBuilder content = new StringBuilder();
        try {
            Map<String, String> params = data.getFields();

            content.append(URLEncoder.encode(FieldName.Merchant_ID, UTF8)).append("=");
            content.append(URLEncoder.encode(m.getID(), UTF8)).append("&");

            content.append(URLEncoder.encode(FieldName.Login, UTF8)).append("=");
            content.append(URLEncoder.encode(m.getLogin(), UTF8)).append("&");

            content.append(URLEncoder.encode(FieldName.Password, UTF8)).append("=");
            content.append(URLEncoder.encode(m.getPassword(), UTF8)).append("&");

            content.append(URLEncoder.encode("TokenType", UTF8)).append("=");
            content.append(URLEncoder.encode(type, UTF8)).append("&");

            content.append(URLEncoder.encode(FieldName.PaymentToken, UTF8)).append("=");
            content.append(URLEncoder.encode(params.get(FieldName.PaymentToken), UTF8)).append("&");

            if (data.getLink() != null) {
                content.append(URLEncoder.encode("outcfsid", UTF8)).append("=");
                final String cfsid = getCFSIDFromLink(data.getLink());
                content.append(cfsid != null ? URLEncoder.encode(cfsid, UTF8) : "").append("&");
            } else {
                content.append(URLEncoder.encode(FieldName.OrderNumber, UTF8)).append("=");
                content.append(URLEncoder.encode(params.get(FieldName.OrderNumber), UTF8)).append("&");

                content.append(URLEncoder.encode(FieldName.OrderAmount, UTF8)).append("=");
                content.append(URLEncoder.encode(params.get(FieldName.OrderAmount), UTF8)).append("&");

                content.append(URLEncoder.encode(FieldName.OrderCurrency, UTF8)).append("=");
                content.append(URLEncoder.encode(params.get(FieldName.OrderCurrency), UTF8)).append("&");

                if (params.get(FieldName.OrderComment) != null) {
                    content.append(URLEncoder.encode(FieldName.OrderComment, UTF8)).append("=");
                    content.append(URLEncoder.encode(params.get(FieldName.OrderComment), UTF8)).append("&");
                }

                content.append(URLEncoder.encode(FieldName.Lastname, UTF8)).append("=");
                content.append(URLEncoder.encode(params.get(FieldName.Lastname), UTF8)).append("&");

                content.append(URLEncoder.encode(FieldName.Firstname, UTF8)).append("=");
                content.append(URLEncoder.encode(params.get(FieldName.Firstname), UTF8)).append("&");

                content.append(URLEncoder.encode(FieldName.Email, UTF8)).append("=");
                content.append(URLEncoder.encode(params.get(FieldName.Email), UTF8)).append("&");
            }

            content.append(URLEncoder.encode(FieldName.ClientIP, UTF8)).append("=");
            if (params.get(FieldName.ClientIP) != null) {
                content.append(URLEncoder.encode(params.get(FieldName.ClientIP), UTF8)).append("&");
            } else {
                content.append(URLEncoder.encode("127.0.0.1", UTF8)).append("&");
            }

            content.append(URLEncoder.encode(FieldName.Format, UTF8)).append("=");
            content.append(URLEncoder.encode("4", UTF8));

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Encoding error", e);
        }

        Log.d(TAG, "Request:" + content.toString());

        return content.toString();
    }

    private static String getCFSIDFromLink(String link) {
        try {
            String cfsid = link.split("CFSID=")[1].split("&")[0];
            if (cfsid != null && cfsid.matches("[\\w]{24}")) {
                return cfsid;
            }
        } catch (Exception e) {
            Log.e(TAG, "Link parsing error. Check your CFSID");
        }
        return null;
    }

    private class TokenPayResponseParser implements AssistNetworkEngine.NetworkResponseProcessor {

        protected Map<String, String> responseFields;
        protected String testField = "responsecode";

        protected boolean isError;
        protected String errorMessage;

        public TokenPayResponseParser() {
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

            Log.d(TAG, "TokenPayResponseParser.asyncProcessing()");
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

            long tID = getTransaction().getId();
            if (isError) {
                if (hasListener()) {
                    getListener().onError(tID, errorMessage);
                }
            } else {
                AssistResult result = new AssistResult();
                final String tf = responseFields.get(testField);
                if (tf == null || !tf.isEmpty()) {
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