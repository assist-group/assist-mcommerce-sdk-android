package ru.assisttech.sdk.registration;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import ru.assisttech.sdk.network.AssistNetworkEngine;
import ru.assisttech.sdk.network.HttpResponse;

public class AssistRegistrationProvider {

    private static final String TAG = "RegistrationProvider";

    //private static final String SOAP_ACTION = "http://www.paysecure.ru/ws/getregistration";

    private RegistrationResultListener registrationListener;
    private URL url;
    private AssistNetworkEngine ne;

    public interface RegistrationResultListener {
        void onRegistrationOk(String registrationID);

        void onRegistrationError(String faultCode, String faultString);
    }

    public AssistRegistrationProvider(Context context) {
    }

    public void setNetworkEngine(AssistNetworkEngine engine) {
        this.ne = engine;
    }

    public void setURL(String server) {
        try {
            url = new URL(server);
        } catch (MalformedURLException e) {
            Log.e(TAG, "URL error. ", e);
        }
    }

    public void setResultListener(RegistrationResultListener listener) {
        this.registrationListener = listener;
    }

    public void register(RegistrationRequestBuilder builder) {
        ne.postSOAP(
                url,
                new AssistNetworkEngine.ConnectionErrorListener() {
                    @Override
                    public void onConnectionError(String info) {
                        registrationListener.onRegistrationError("0", info);
                    }
                },
                new RegistrationResultProcessor(),
                builder.buildRequest()
        );
    }

    private class RegistrationResultProcessor implements AssistNetworkEngine.NetworkResponseProcessor {

        private static final String REGISTRATION_ID = "registration_id";
        private static final String FAULT_CODE = "faultcode";
        private static final String FAULT_STRING = "faultstring";

        private String id = "";
        private String faultcode = "";
        private String faultstring = "";

        @Override
        public void asyncProcessing(HttpResponse response) {
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
                        if (currentTag.equals(REGISTRATION_ID)) {
                            id = parser.getText();
                        } else if (currentTag.equals(FAULT_CODE)) {
                            faultcode = parser.getText();
                        } else if (currentTag.equals(FAULT_STRING)) {
                            faultstring = parser.getText();
                        }
                    }
                    eventType = parser.next();
                }
            } catch (XmlPullParserException | IOException e) {
                Log.e(TAG, "Registration response parsing error", e);
            }
        }

        @Override
        public void syncPostProcessing() {
            if (TextUtils.isEmpty(id)) {
                registrationListener.onRegistrationError(faultcode, faultstring);
            } else {
                registrationListener.onRegistrationOk(id);
            }
        }
    }
}
