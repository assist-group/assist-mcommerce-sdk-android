package ru.assisttech.sdk.engine;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;

import ru.assisttech.sdk.AssistAddress;
import ru.assisttech.sdk.AssistMerchant;
import ru.assisttech.sdk.AssistPaymentData;
import ru.assisttech.sdk.AssistResult;
import ru.assisttech.sdk.FieldName;
import ru.assisttech.sdk.R;
import ru.assisttech.sdk.identification.InstallationInfo;
import ru.assisttech.sdk.identification.SystemInfo;
import ru.assisttech.sdk.network.AssistNetworkEngine;
import ru.assisttech.sdk.processor.AssistCancelProcessor;
import ru.assisttech.sdk.processor.AssistRecurrentPayProcessor;
import ru.assisttech.sdk.processor.AssistTokenPayProcessor;
import ru.assisttech.sdk.registration.AssistRegistrationData;
import ru.assisttech.sdk.registration.AssistRegistrationProvider;
import ru.assisttech.sdk.registration.RegistrationRequestBuilder;
import ru.assisttech.sdk.processor.AssistBaseProcessor;
import ru.assisttech.sdk.processor.AssistResultProcessor;
import ru.assisttech.sdk.processor.AssistProcessorEnvironment;
import ru.assisttech.sdk.processor.AssistProcessorListener;
import ru.assisttech.sdk.processor.AssistWebProcessor;
import ru.assisttech.sdk.storage.AssistTransaction;
import ru.assisttech.sdk.storage.AssistTransactionStorage;
import ru.assisttech.sdk.storage.AssistTransactionStorageImpl;

import static ru.assisttech.sdk.storage.AssistTransaction.PaymentMethod.CARD_TERMINAL;

/**
 * Initiates payment process, checks connection and registration
 */
public class AssistPayEngine {

    private static final String TAG = "AssistPayEngine";

    private String ServerUrl = AssistAddress.DEFAULT_SERVER;

    private Context appContext;
    private Activity callerActivity;

    private InstallationInfo instInfo;
    private AssistNetworkEngine netEngine;

    protected AssistBaseProcessor processor;
    private AssistTransactionStorage storage;

    private PayEngineListener engineListener;

    private ProgressDialog pd;
    private boolean connectionChecked;
    private boolean finished;
    private String deviceUniqueId;

    private static AssistPayEngine instance;

    public static synchronized AssistPayEngine getInstance(Context context) {
        if (instance == null) {
            instance = new AssistPayEngine(context);
        }
        return instance;
    }

    private AssistPayEngine(Context c) {
        appContext = c.getApplicationContext();
        netEngine = new AssistNetworkEngine(appContext);
        storage = new AssistTransactionStorageImpl(appContext, "c");
        instInfo = InstallationInfo.getInstance(appContext);

        SystemInfo sysInfo = SystemInfo.getInstance();
        deviceUniqueId = sysInfo.uniqueId();
    }

    public AssistProcessorEnvironment buildServiceEnvironment(AssistPaymentData data) {
        AssistMerchant m = new AssistMerchant(data.getMerchantID(), data.getLogin(), data.getPassword());
        return new AssistProcessorEnvironment(this, m, data);
    }

    public void setEngineListener(PayEngineListener listener) {
        engineListener = listener;
    }

    public PayEngineListener getEngineListener() {
        return engineListener;
    }

    public void addNetworkCertificate(Certificate cert) {
        netEngine.addCertificate(cert);
    }

    public AssistNetworkEngine getNetEngine() {
        return netEngine;
    }

    public void setServerURL(String url) {
        if (!TextUtils.isEmpty(url)) {
            try {
                String protocol = new URL(url).getProtocol();
                if (TextUtils.isEmpty(protocol)) {
                    ServerUrl = "https://" + url;
                } else {
                    ServerUrl = url;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    public AssistTransactionStorage transactionStorage() {
        return storage;
    }

    public InstallationInfo getInstInfo() {
        return instInfo;
    }


    public void payWeb(Activity caller, AssistPaymentData data, boolean useCamera) {
        payWeb(caller, buildServiceEnvironment(data), useCamera);
    }

    /**
     * Web based payment process
     * Uses device camera for card data input
     */
    public void payWeb(Activity caller, AssistProcessorEnvironment environment, boolean useCamera) {

        if (isEngineReady()) {
            saveCallerActivity(caller);
            setFinished(false);

            AssistPaymentData data = environment.getData();
            data.setMobileDevice("5");  // Tells Assist server what web pages to show

            AssistTransaction t = createTransaction(
                    data.getMerchantID(),
                    data,
                    useCamera ? AssistTransaction.PaymentMethod.CARD_PHOTO_SCAN :
                            AssistTransaction.PaymentMethod.CARD_MANUAL
            );

            processor = new AssistWebProcessor(getContext(), environment, useCamera);
            processor.setNetEngine(netEngine);
            processor.setURL(getWebServiceUrl());
            processor.setListener(new WebProcessorListener());
            processor.setTransaction(t);

            checkRegistration();
        }
    }

    public void payToken(Activity caller, AssistPaymentData data, String type) {
        payToken(caller, buildServiceEnvironment(data), type);
    }

    /**
     * Платеж с токеном Android Pay
     */
    public void payToken(Activity caller, AssistProcessorEnvironment environment, String type) {

        if (isEngineReady()) {
            saveCallerActivity(caller);
            setFinished(false);

            AssistPaymentData data = environment.getData();

            AssistTransaction t = createTransaction(
                    data.getMerchantID(),
                    data,
                    CARD_TERMINAL
            );

            processor = new AssistTokenPayProcessor(getContext(), environment, type);
            processor.setNetEngine(netEngine);
            processor.setURL(getTokenPayeServiceUrl());
            processor.setListener(new TokenPayProcessorListener());
            processor.setTransaction(t);

            checkRegistration();
        }
    }

    /**
     * Recurrent Pay
     */
    public void payRecurrent(Activity caller, AssistProcessorEnvironment environment) {

        if (isEngineReady()) {
            saveCallerActivity(caller);
            setFinished(false);

            AssistPaymentData data = environment.getData();

            AssistTransaction t = createTransaction(
                    data.getMerchantID(),
                    data,
                    CARD_TERMINAL
            );

            processor = new AssistRecurrentPayProcessor(getContext(), environment);
            processor.setNetEngine(netEngine);
            processor.setURL(getRecurrentUrl());
            processor.setListener(new RecurrentPayProcessorListener());
            processor.setTransaction(t);

            checkRegistration();
        }
    }

    /**
     * Отмена платежа. Возможна только для успешных платежей.
     */
    public void cancelPayment(Activity caller, AssistTransaction t, String mid, String login, String password) {

        if (isEngineReady()) {
            saveCallerActivity(caller);
            setFinished(false);

            AssistMerchant m = new AssistMerchant(mid, login, password);
            AssistProcessorEnvironment env = new AssistProcessorEnvironment(this, m, null);
            processor = new AssistCancelProcessor(getContext(), env);
            processor.setNetEngine(netEngine);
            processor.setURL(getCancelUrl());
            processor.setListener(new CancelProcessorListener());
            processor.setTransaction(t);
            checkRegistration();
        }
    }

    /**
     * Gets result for certain payment
     *
     * @param id transaction ID {@link AssistTransaction#getId()}
     */
    public void getOrderResult(Activity caller, long id) {
        if (isEngineReady()) {
            saveCallerActivity(caller);
            setFinished(false);
            processor = initResultProcessor(id);
            checkRegistration();
        }
    }

    public void stopPayment(Activity caller) {
        setFinished(true);
        if ((processor != null) && (processor.isRunning())) {
            processor.stop();
            getEngineListener().onCanceled(caller, processor.getTransaction());
            processor = null;
        }
    }

    public String getRegistrationId() {
        return getInstInfo().getAppRegId();
    }

    public String getDeviceId() {
        return getInstInfo().getDeiceUniqueId();
    }

    /**
     * Checks application registration in Assist Payment System
     */
    private void checkRegistration() {
        /* Check network connection first */
        if (!connectionChecked) {
            checkNetworkConnection();
            return;
        }
        if (getRegistrationId() == null) {
            startRegistration();
        } else {
            deviceUniqueId = getDeviceId();
            startPayment();
        }
    }

    /**
     * Запуск вызова сервиса регистрации приложения в Ассист {@link AssistAddress#REGISTRATION_SERVICE}
     * Полученные регистрационные данные используются при проведении платежа {@link #payWeb(Activity, AssistProcessorEnvironment, boolean)}
     */
    private void startRegistration() {
        AssistRegistrationData regData = new AssistRegistrationData();
        regData.setApplicationName(instInfo.appName());
        regData.setAppVersion(instInfo.versionName());
        regData.setDerviceID(deviceUniqueId);

        AssistRegistrationProvider rp = new AssistRegistrationProvider(getContext());
        rp.setNetworkEngine(netEngine);
        rp.setURL(getRegistrationUrl());
        rp.setResultListener(new RegistrationResultListener());
        rp.register(new RegistrationRequestBuilder(regData));
    }

    private void onRegistrationSuccess(String deviceId, String registrationID) {
        getInstInfo().setAppRegID(deviceId, registrationID);
    }

    /**
     * HTTPS connection's certificate check procedure
     */
    private void checkNetworkConnection() {
        Log.d(TAG, "Check URL: " + ServerUrl);
        try {
            URL u = new URL(ServerUrl + AssistAddress.WEB_SERVICE);
            Activity callerActivity = getCallerActivity();
            if (callerActivity != null) {
                showProgressDialog(callerActivity, R.string.connection_check);
            }
            netEngine.checkHTTPSConnection(u, new ConnectionCheckListener());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


    public AssistTransaction createTransaction(String merchantID, AssistPaymentData data, AssistTransaction.PaymentMethod method) {
        AssistTransaction t = new AssistTransaction();
        t.setMerchantID(merchantID);
        t.setOrderAmount(data.getFields().get(FieldName.OrderAmount));
        t.setOrderComment(data.getFields().get(FieldName.OrderComment));
        t.setOrderCurrency(AssistPaymentData.Currency.valueOf(data.getFields().get(FieldName.OrderCurrency)));
        t.setOrderNumber(data.getFields().get(FieldName.OrderNumber));
        if (data.getOrderItems() != null) {
            t.setOrderItems(data.getOrderItems());
        }
        t.setPaymentMethod(method);
        /* Check if transaction requires user signature */
        if (data.getFields().containsKey(FieldName.PaymentMode)) {
            String mode = data.getFields().get(FieldName.PaymentMode);
            t.setRequireUserSignature(AssistPaymentData.PaymentMode.POSKeyEntry.equals(mode));
        }
        return t;
    }

    private void startProcessor() {
        processor.start(getCallerActivity());
    }

    /**
     * Transaction saving and payment beginning
     */
    private void startPayment() {
        Log.d(TAG, "startPayment()");
        if (!processor.getTransaction().isStored()) {
            storage.add(processor.getTransaction()); // TODO: Check return value (whether transaction added successfully or not)
        }
        startProcessor();
    }

    /**
     * Internal method to get payment result after payment process completion.
     * Does not check application registration
     *
     * @param id transaction id {@link AssistTransaction#getId()}
     */
    private void getResult(long id) {
        processor = initResultProcessor(id);
        startProcessor();
    }

    private AssistResultProcessor getResultProcessor() {
        AssistProcessorEnvironment env = new AssistProcessorEnvironment(this, null, null);
        env.setDeviceId(getDeviceId());
        return new AssistResultProcessor(getContext(), env);
    }

    /**
     * @param id transaction id {@link AssistTransaction#getId()}
     * @return {@link AssistResultProcessor}
     */
    private AssistResultProcessor initResultProcessor(long id) {
        AssistResultProcessor rp = getResultProcessor();
        rp.setNetEngine(netEngine);
        rp.setURL(getGetOrderStatusUrl());
        rp.setListener(new ResultProcessorListener());
        rp.setTransaction(getTransaction(id));
        return rp;
    }

    /**
     * Obtain transaction form DB
     *
     * @param id transaction id {@link AssistTransaction#getId()}
     */
    protected AssistTransaction getTransaction(long id) {
        return storage.getTransaction(id);
    }

    /**
     * Update transaction in DB with new {@link AssistResult}
     *
     * @param id     ID of transaction to cancel {@link AssistTransaction#getId()}
     * @param result result of transaction {@link AssistResult}
     */
    private void updateTransaction(long id, AssistResult result) {
        storage.updateTransactionResult(id, result);
    }


    private void engineInitializationFailed(String info) {
        processor = null;
        if (!isFinished()) {
            getEngineListener().onFailure(getCallerActivity(), info);
        }
    }

    protected Context getContext() {
        return appContext;
    }

    private String getServerUrl() {
        return ServerUrl;
    }

    private String getRegistrationUrl() {
        return getServerUrl() + AssistAddress.REGISTRATION_SERVICE;
    }

    private String getWebServiceUrl() {
        return getServerUrl() + AssistAddress.WEB_SERVICE;
    }

    private String getTokenPayeServiceUrl() {
        return getServerUrl() + AssistAddress.TOKENPAY_SERVICE;
    }

    private String getRecurrentUrl() {
        return getServerUrl() + AssistAddress.RECURRENT_URL;
    }

    private String getGetOrderStatusUrl() {
        return getServerUrl() + AssistAddress.GET_ORDER_STATUS_SERVICE;
    }

    private String getCancelUrl() {
        return getServerUrl() + AssistAddress.CANCEL_SERVICE;
    }

    private boolean isEngineReady() {
        return processor == null || !processor.isRunning();
    }

    private void saveCallerActivity(Activity caller) {
        callerActivity = caller;
    }

    private Activity getCallerActivity() {
        return callerActivity;
    }

    private boolean isFinished() {
        return finished;
    }

    private void setFinished(boolean value) {
        this.finished = value;
    }

    private void showProgressDialog(Activity activity, int messageResId) {
        if (pd != null) {
            return;
        }
        pd = new ProgressDialog(activity);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage(appContext.getString(messageResId));
        pd.show();
    }

    private void closeProgressDialog() {
        if (pd != null) {
            pd.dismiss();
            pd = null;
        }
    }

    public AssistWebProcessor getWebProcessor() {
        return (AssistWebProcessor) processor;
    }

    /**
     * Connection check result processing
     */
    private class ConnectionCheckListener implements AssistNetworkEngine.ConnectionCheckListener {

        @Override
        public void onConnectionSuccess() {
            Log.d(TAG, "Connection check success");
            closeProgressDialog();
            connectionChecked = true;
            checkRegistration();
        }

        @Override
        public void onConnectionFailure(String info) {
            Log.d(TAG, "Connection check error. " + info);
            closeProgressDialog();
            connectionChecked = false;
            engineInitializationFailed(getContext().getString(R.string.connection_error));
        }
    }

    /**
     * Слушатель результата регистрации приложения {@link AssistRegistrationProvider}
     */
    private class RegistrationResultListener implements AssistRegistrationProvider.RegistrationResultListener {

        @Override
        public void onRegistrationOk(String registrationID) {
            Log.d(TAG, "onRegistrationOk(): " + registrationID);
            onRegistrationSuccess(deviceUniqueId, registrationID);
            startPayment();
        }

        @Override
        public void onRegistrationError(String faultCode, String faultString) {
            Log.d(TAG, "onRegistrationError(): " + faultCode + ": " + faultString);
            engineInitializationFailed(getContext().getString(R.string.registration_error));
        }
    }

    /**
     * Базовый слушатель результата
     */
    private abstract class BaseProcessorListener implements AssistProcessorListener {

        @Override
        public void onNetworkError(long id, String message) {
            Log.d(TAG, "onNetworkError() " + message);
            getEngineListener().onNetworkError(getCallerActivity(), message);
        }

        @Override
        public void onTerminated(long id) {
            Log.d(TAG, "onTerminated()");
            getEngineListener().onCanceled(getCallerActivity(), getTransaction(id));
        }

        @Override
        public void onActivityCreated(Activity newActivity) {
            Log.d(TAG, "onActivityCreated()");
            if (newActivity != null) {
                saveCallerActivity(newActivity);
            }
        }
    }

    /**
     * Слушатель результата вызова сервисе web оплаты {@link AssistWebProcessor}
     */
    private class WebProcessorListener extends BaseProcessorListener {

        @Override
        public void onFinished(long id, AssistResult result) {
            Log.d(TAG, "WebProcessorListener.onFinished() id = : " + String.valueOf(id) + "; " + result.getExtra());
            if (!TextUtils.isEmpty(result.getOrderNumber())) {
                transactionStorage().updateTransactionOrderNumber(id, result.getOrderNumber());
            }
            if (!isFinished()) {
                getResult(id);
            }
        }

        @Override
        public void onError(long id, String message) {
            Log.d(TAG, "WebProcessorListener.onError() " + message);
            transactionStorage().deleteTransaction(id);
            getEngineListener().onFailure(getCallerActivity(), message);
        }
    }

    /**
     * Слушатель результата вызова сервиса запроса статуса заказа {@link AssistResultProcessor}
     */
    private class ResultProcessorListener extends BaseProcessorListener {

        @Override
        public void onFinished(long id, AssistResult result) {
            Log.d(TAG, "ResultProcessorListener.onFinished() " + result.getOrderState());
            updateTransaction(id, result);
            getEngineListener().onFinished(getCallerActivity(), getTransaction(id));
        }

        @Override
        public void onError(long id, String message) {
            Log.d(TAG, "ResultProcessorListener.onError() " + message);
            getEngineListener().onFailure(getCallerActivity(), message);
        }
    }

    /**
     * Слушатель результата вызова сервиса отмены заказа {@link AssistCancelProcessor}
     */
    private class CancelProcessorListener extends BaseProcessorListener {
        @Override
        public void onFinished(long id, AssistResult result) {
            Log.d(TAG, "CancelProcessorListener.onFinished() " + result.getOrderState());
            updateTransaction(id, result);
            getEngineListener().onFinished(getCallerActivity(), getTransaction(id));
        }

        @Override
        public void onError(long id, String message) {
            Log.d(TAG, "CancelProcessorListener.onError() " + message);
            getEngineListener().onFailure(getCallerActivity(), message);
        }
    }

    /**
     * Слушатель результата вызова сервиса оплаты заказа с токеном {@link AssistTokenPayProcessor}
     */
    private class TokenPayProcessorListener extends BaseProcessorListener {
        @Override
        public void onFinished(long id, AssistResult result) {
            Log.d(TAG, "TokenPayProcessorListener.onFinished() " + result.getOrderState());
            updateTransaction(id, result);
            getEngineListener().onFinished(getCallerActivity(), getTransaction(id));
        }

        @Override
        public void onError(long id, String message) {
            Log.d(TAG, "TokenPayProcessorListener.onError() " + message);
            getEngineListener().onFailure(getCallerActivity(), message);
        }
    }

    /**
     * Слушатель результата вызова сервиса оплаты заказа с токеном {@link AssistRecurrentPayProcessor}
     */
    public class RecurrentPayProcessorListener extends BaseProcessorListener {
        @Override
        public void onFinished(long id, AssistResult result) {
            Log.d(TAG, "RecurrentPayProcessorListener.onFinished() " + result.getOrderState());
            updateTransaction(id, result);
            getEngineListener().onFinished(getCallerActivity(), getTransaction(id));
        }

        @Override
        public void onError(long id, String message) {
            Log.d(TAG, "RecurrentPayProcessorListener.onError() " + message);
            getEngineListener().onFailure(getCallerActivity(), message);
        }
    }
}
