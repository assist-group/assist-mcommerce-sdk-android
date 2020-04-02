package ru.assisttech.sdk.processor;

import android.app.Activity;
import android.content.Context;

import java.net.MalformedURLException;
import java.net.URL;

import ru.assisttech.sdk.network.AssistNetworkEngine;
import ru.assisttech.sdk.storage.AssistTransaction;

/**
 * Базовый класс для классов вызывающих web сервисы Ассист
 */
public abstract class AssistBaseProcessor {

    private Context context;
    private Activity callerActivity;

    private AssistProcessorEnvironment environment;
    private AssistProcessorListener listener;
    private AssistNetworkEngine ne;
    private AssistTransaction transaction;

    private URL url;
    private URL authUrl;
    private boolean isRunning;

    AssistBaseProcessor(Context context, AssistProcessorEnvironment environment) {
        this.context = context;
        this.environment = environment;
    }

    public void setListener(AssistProcessorListener listener) {
        this.listener = listener;
    }

    public void setNetEngine(AssistNetworkEngine engine) {
        this.ne = engine;
    }

    public void setURL(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public URL getURL() {
        return url;
    }

    public void setAuthURL(String authUrl) {
        try {
            this.authUrl = new URL(authUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public URL getAuthURL() {
        return authUrl;
    }

    public void setTransaction(AssistTransaction t) {
        transaction = t;
    }

    public AssistTransaction getTransaction() {
        return transaction;
    }

    public void start(Activity caller) {
        callerActivity = caller;
        run();
        setRunning(true);
    }

    public void stop() {
        setListener(null);
        terminate();
        setRunning(false);
    }

    public boolean isRunning() {
        return isRunning;
    }

    protected abstract void run();

    protected abstract void terminate();

    /**
     * Called by successors to indicate that service is finished
     */
    protected void finish() {
        setListener(null);
        setRunning(false);
    }

    protected void setRunning(boolean value) {
        isRunning = value;
    }

    protected Context getContext() {
        return context;
    }

    protected Activity getCaller() {
        return callerActivity;
    }

    protected AssistProcessorEnvironment getEnvironment() {
        return environment;
    }

    protected AssistNetworkEngine getNetEngine() {
        return ne;
    }

    protected AssistProcessorListener getListener() {
        return listener;
    }

    protected boolean hasListener() {
        return listener != null;
    }

    /**
     * Default network connection error handler
     */
    protected class NetworkConnectionErrorListener implements AssistNetworkEngine.ConnectionErrorListener {
        @Override
        public void onConnectionError(String info) {
            if (hasListener()) {
                getListener().onNetworkError(getTransaction().getId(), info);
                finish();
            }
        }
    }
}
