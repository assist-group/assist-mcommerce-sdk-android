package ru.assisttech.sdk.network;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

public class AssistNetworkEngine {

    private static final String TAG = "AssistNetworkEngine";

    private SSLContextFactory sslContextFactory;
    private SSLContext sslContext;
    private boolean useCustomSslCertificates;
    private String userAgent = "Mozilla";
    private CheckHTTPSConnectionTask connectionTask;
    private AssistNetworkTask networkTask;

    public interface ConnectionCheckListener {
        void onConnectionSuccess();

        void onConnectionFailure(String info);
    }

    public interface ConnectionErrorListener {
        void onConnectionError(String info);
    }

    public interface NetworkResponseProcessor {
        void asyncProcessing(HttpResponse response);    // Supposed to be called on PARALLEL thread

        void syncPostProcessing();                    // Supposed to be called on MAIN (UI) thread
    }

    public AssistNetworkEngine(Context context) {
        userAgent = new WebView(context).getSettings().getUserAgentString();
        try {
            sslContextFactory = new SSLContextFactory(context);
        } catch (SSLContextFactoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean addCertificate(Certificate cert) {
        return sslContextFactory.addCertificate(cert);
    }

    public boolean isCustomSslCertificateUsed() {
        return useCustomSslCertificates;
    }

    public void stopTasks() {

        if (connectionTask != null && !connectionTask.isCancelled()) {
            connectionTask.resetListeners();
            connectionTask.cancel(true);
        }

        if (networkTask != null && !networkTask.isCancelled()) {
            networkTask.resetListeners();
            networkTask.cancel(true);
        }
    }

    /**
     * Checks connection to server
     */
    public void checkHTTPSConnection(URL url, ConnectionCheckListener listener) {
        connectionTask = new CheckHTTPSConnectionTask(listener);
        connectionTask.execute(url);
    }

    /**
     *
     */
    public void loadAndProcessPage(URL url, ConnectionErrorListener listener, NetworkResponseProcessor processor) {
        networkTask = new HttpGETTask(url, listener, processor);
        networkTask.execute();
    }

    /**
     *
     */
    public void sendXPosRequest(URL url, ConnectionErrorListener listener, NetworkResponseProcessor processor, String request) {
        networkTask = new SoapPOSTTask(url, listener, processor, request);
        networkTask.execute();
    }

    /**
     *
     */
    public void postSOAP(URL url, ConnectionErrorListener listener, NetworkResponseProcessor processor, String request) {
        networkTask = new SoapPOSTTask(url, listener, processor, request);
        networkTask.execute();
    }

    /**
     *
     */
    public void postRequest(URL url, ConnectionErrorListener listener, NetworkResponseProcessor processor, String request) {
        networkTask = new HttpPostTask(url, listener, processor, request);
        networkTask.execute();
    }

    /**
     * Tries to establish https connection with server
     */
    private class CheckHTTPSConnectionTask extends AsyncTask<URL, Void, Void> {

        private ConnectionCheckListener listener;
        private String information;
        private int responseCode;
        private boolean success;

        public CheckHTTPSConnectionTask(ConnectionCheckListener listener) {
            this.listener = listener;
        }

        public void resetListeners() {
            listener = null;
        }

        private HttpsURLConnection createConnection(URL url, boolean useCustomCertificates) throws IOException {
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            try {
                if (useCustomCertificates) {
                    urlConnection.setSSLSocketFactory(new TLSSocketFactory(getSSLContext()));
                } else {
                    urlConnection.setSSLSocketFactory(new TLSSocketFactory());
                }
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            urlConnection.setInstanceFollowRedirects(true);
            urlConnection.addRequestProperty("Accept", "text/html");
            urlConnection.addRequestProperty("Accept-Encoding", "gzip, deflate");
            urlConnection.addRequestProperty("User-Agent", userAgent);
            urlConnection.setRequestMethod("GET");
            return urlConnection;
        }

        private boolean checkConnection(HttpsURLConnection urlConnection) throws IOException {
            urlConnection.connect();
            responseCode = urlConnection.getResponseCode();
            Log.d(TAG, "Server response code: " + responseCode);

            return (responseCode == HttpsURLConnection.HTTP_OK);
        }

        @Override
        protected Void doInBackground(URL... urls) {

            Log.d(TAG, "Start server verification...");
            success = false;
            HttpsURLConnection urlConnection = null;

            /* Default sslContextFactory */
            try {
                Log.d(TAG, "Create connection...");
                urlConnection = createConnection(urls[0], false);

                if (isCancelled()) {
                    return null;
                }

                Log.d(TAG, "Check connection");
                if (checkConnection(urlConnection)) {
                    setUseCustomSslCertificates(false);
                    success = true;
                    return null;
                }
            } catch (IOException e) {
                information = e.getMessage();
                Log.d(TAG, "Got exception: " + information);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                    urlConnection = null;
                }
            }

            /* Custom sslContextFactory; uses provided certificates */
            try {
                Log.d(TAG, "Create connection...");
                urlConnection = createConnection(urls[0], true);

                if (isCancelled()) {
                    return null;
                }

                Log.d(TAG, "Check connection");
                if (checkConnection(urlConnection)) {
                    setUseCustomSslCertificates(true);
                    success = true;
                    return null;
                }
            } catch (IOException e) {
                Log.d(TAG, "Got exception: " + information);
                information = e.getMessage();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            information = "Server connection error. Response code: " + responseCode;
            return null;
        }

        @Override
        protected void onPostExecute(Void value) {
            if (listener == null) return;
            if (success) {
                listener.onConnectionSuccess();
            } else {
                listener.onConnectionFailure(information);
            }
        }
    }

    /**
     * Base class for network task executed in parallel thread
     * Sends request to server and receives response
     */
    private abstract class AssistNetworkTask extends AsyncTask<Void, Void, Void> {

        private URL url;
        private ConnectionErrorListener listener;
        private NetworkResponseProcessor processor;
        private HttpResponse response;
        protected boolean connectionError;
        protected String info;

        public AssistNetworkTask(URL url, ConnectionErrorListener listener, NetworkResponseProcessor processor) {
            this.url = url;
            this.listener = listener;
            this.processor = processor;
        }

        public void resetListeners() {
            listener = null;
            processor = null;
        }

        protected URL getUrl() {
            return url;
        }

        @Override
        protected Void doInBackground(Void... params) {

            StringBuilder responseData = new StringBuilder();
            HttpRequest request = getRequest();

            HttpsURLConnection connection = null;
            try {
                connection = (HttpsURLConnection) getUrl().openConnection();
                try {
                    if (isCustomSslCertificateUsed()) {
                        connection.setSSLSocketFactory(new TLSSocketFactory(getSSLContext()));
                    } else {
                        connection.setSSLSocketFactory(new TLSSocketFactory());
                    }
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                    throw new IOException("Connection initialization failed");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    throw new IOException("Connection initialization failed");
                }
                connection.setInstanceFollowRedirects(true);
                connection.setUseCaches(false);
                connection.setDoInput(true);
                if (request.hasData())
                    connection.setDoOutput(true);

                connection.setRequestMethod(request.getMethod());
                if (request.getProperties() != null) {
                    Set<Entry<String, String>> set = request.getProperties().entrySet();
                    for (Entry<String, String> entry : set) {
                        connection.setRequestProperty(entry.getKey(), entry.getValue());
                    }
                }

                if (isCancelled()) {
                    return null;
                }

                if (request.hasData()) {
                    Log.d(TAG, request.toString());
                    //Send request
                    DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                    wr.writeBytes(request.getData());
                    wr.flush();
                    wr.close();
                }

                if (isCancelled()) {
                    return null;
                }

                int responseCode = connection.getResponseCode();
                Map<String, List<String>> responseHeaders = connection.getHeaderFields();

                // Get response data
                BufferedReader reader = null;
                try {
                    InputStream is = connection.getInputStream();
                    if (TextUtils.equals(connection.getContentEncoding(), "gzip")) {
                        GZIPInputStream gzipIs = new GZIPInputStream(is);
                        reader = new BufferedReader(new InputStreamReader(gzipIs, "UTF-8"));
                    } else {
                        reader = new BufferedReader(new InputStreamReader(is));
                    }

                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseData.append(line);
                        if (isCancelled())
                            break;
                    }
                } catch (IOException e) {
                    // Getting response data error. Possibly there is no data
                } finally {
                    if (reader != null)
                        reader.close();
                }
                connection.disconnect();
                connection = null;

                response = new HttpResponse(responseCode, responseHeaders, responseData.toString());
                Log.d(TAG, response.toString());
                if (processor != null) {
                    processor.asyncProcessing(response);
                }

            } catch (IOException e) {
                connectionError = true;
                info = e.getMessage();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void value) {
            if (connectionError) {
                if (listener != null)
                    listener.onConnectionError(info);
            } else {
                if (processor != null)
                    processor.syncPostProcessing();
            }
        }

        private HttpRequest getRequest() {
            HttpRequest request = new HttpRequest();
            request.setMethod("GET");
            request.addProperty("Accept-Encoding", "gzip");
            return customizeRequest(request);
        }

        /**
         * Callback for HTTP request properties providing {@link HttpRequest}}
         */
        protected abstract HttpRequest customizeRequest(HttpRequest request);
    }

    /**
     *
     */
    private class HttpGETTask extends AssistNetworkTask {
        public HttpGETTask(URL url, ConnectionErrorListener listener, NetworkResponseProcessor processor) {
            super(url, listener, processor);
        }

        @Override
        protected HttpRequest customizeRequest(HttpRequest request) {
            request.setMethod("GET");
            request.addProperty("Accept", "text/html");
            request.addProperty("User-Agent", userAgent);
            return request;
        }
    }

    /**
     *
     */
    private class HttpPostTask extends AssistNetworkTask {
        private String data;

        public HttpPostTask(URL url, ConnectionErrorListener listener, NetworkResponseProcessor processor, String request) {
            super(url, listener, processor);
            data = request;
        }

        @Override
        protected HttpRequest customizeRequest(HttpRequest request) {
            request.setMethod("POST");
            request.setData(data);
            request.addProperty("User-Agent", userAgent);
            request.addProperty("Content-Type", "application/x-www-form-urlencoded");
            request.addProperty("Content-Length", Integer.toString(data.length()));
            return request;
        }
    }

    /**
     * Posts SOAP request and parses response
     */
    private class SoapPOSTTask extends /*DebugWithoutNetworkTask*/AssistNetworkTask {
        private String data;

        public SoapPOSTTask(URL url, ConnectionErrorListener listener, NetworkResponseProcessor processor, String request) {
            super(url, listener, processor);
            data = request;
        }

        @Override
        protected HttpRequest customizeRequest(HttpRequest request) {
            request.setMethod("POST");
            request.setData(data);
            request.addProperty("Content-Type", "text/xml");
            request.addProperty("Content-Encoding", "UTF-8");
            request.addProperty("SOAPAction", getUrl().toString());
            request.addProperty("Content-Length", Integer.toString(data.length()));
            return request;
        }
    }

    /**
     * Creates SSL context
     */
    private SSLContext getSSLContext() {

        if (sslContext == null) {
            try {
                sslContext = sslContextFactory.createSSLContext();
            } catch (SSLContextFactoryException e) {
                e.printStackTrace();
            }
        }
        return sslContext;
    }

    private void setUseCustomSslCertificates(boolean value) {
        useCustomSslCertificates = value;
    }

    /**
     * Logs HTTP response headers
     * @param headers HTTP protocol headers
     */
//	private void debugHeaders(Map<String, List<String>> headers){
//
//		Set<Map.Entry<String, List<String>>> entrySet = headers.entrySet();
//		// Log header
//		Log.d(TAG, "Response headers:");
//		Log.d(TAG, "[");
//		for(Map.Entry<String, List<String>> entry: entrySet)
//		{
//			String str = "";
//			List<String> vList = entry.getValue();
//			for(String val: vList){
//				str += val;
//				str += " ";
//			}
//			Log.d(TAG, entry.getKey() + " : " + str);
//		}
//		Log.d(TAG, "]");
//	}
}
