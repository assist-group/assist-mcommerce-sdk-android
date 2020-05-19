package ru.assisttech.assistsdk;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.github.dmstocking.optional.java.util.Optional;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.samsung.android.sdk.samsungpay.v2.PartnerInfo;
import com.samsung.android.sdk.samsungpay.v2.SamsungPay;
import com.samsung.android.sdk.samsungpay.v2.StatusListener;
import com.samsung.android.sdk.samsungpay.v2.payment.CardInfo;
import com.samsung.android.sdk.samsungpay.v2.payment.PaymentInfo;
import com.samsung.android.sdk.samsungpay.v2.payment.PaymentManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import ru.assisttech.sdk.AssistMerchant;
import ru.assisttech.sdk.AssistPaymentData;
import ru.assisttech.sdk.AssistResult;
import ru.assisttech.sdk.AssistSDK;
import ru.assisttech.sdk.FieldName;
import ru.assisttech.sdk.engine.AssistPayEngine;
import ru.assisttech.sdk.engine.PayEngineListener;
import ru.assisttech.sdk.engine.PaymentTokenType;
import ru.assisttech.sdk.storage.AssistTransaction;
import ru.assisttech.sdk.storage.AssistTransactionStorage;

/**
 * Экран подтверждения данных платежа и выбора способа оплаты web или AndroidPay если доступен
 */
public class ConfirmationActivity extends FragmentActivity {

    public static final String TAG = "ConfirmationActivity";

    private ApplicationConfiguration configuration;
    private AssistPayEngine engine;
    private AssistPaymentData data;

    private ProgressDialog progressDialog;


    // Google Pay (https://developers.google.com/pay/api/android/guides/tutorial)
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 42;
    private PaymentsClient paymentsClient;
    private Button btGooglePay;

    // Samsung Pay
    private Button btSamsungPay;
    private SamsungPay samsungPay;
    private Bundle bundle;
    private PaymentManager paymentManager;
    final String serviceId = "c84b694b18674b8f92e598";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        configuration = ApplicationConfiguration.getInstance();
        engine = AssistSDK.getPayEngine(this);
        data = configuration.getPaymentData();

        if (data != null) {
            TextView tvServer = findViewById(R.id.tvServer);
            TextView tvMerchant = findViewById(R.id.tvMerchant);
            TextView tvOrderNumber = findViewById(R.id.tvOrderNumber);
            TextView tvOrderAmount = findViewById(R.id.tvOrderAmount);
            TextView tvOrderComment = findViewById(R.id.tvOrderComment);

            tvServer.setText(configuration.getServer());

            tvMerchant.setText(
                    String.format(
                            Locale.getDefault(),
                            "%1$s [%2$s: %3$s]",
                            data.getMerchantID(), data.getLogin(), data.getPassword()
                    )
            );
            tvOrderNumber.setText(data.getFields().get(FieldName.OrderNumber));
            tvOrderAmount.setText(
                    String.format(
                            Locale.getDefault(),
                            "%1$s %2$s",
                            data.getFields().get(FieldName.OrderAmount), data.getFields().get(FieldName.OrderCurrency)
                    )
            );
            tvOrderComment.setText(data.getFields().get(FieldName.OrderComment));

            Button btPayWeb = findViewById(R.id.btPay);
            btPayWeb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    payWeb();
                }
            });

            enableGooglePay();
            enableSamsungPay();
        } else {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }
    }


    private void enableGooglePay() {

        // initialize a Google Pay API client for an environment suitable for testing
        paymentsClient = Wallet.getPaymentsClient(
                this,
                new Wallet.WalletOptions.Builder()
                        .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                        //.setEnvironment(WalletConstants.ENVIRONMENT_PRODUCTION)
                        .build()
        );


        final Optional<JSONObject> isReadyToPayJson = GooglePay.getIsReadyToPayRequest();
        if (!isReadyToPayJson.isPresent()) {
            return;
        }
        IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(isReadyToPayJson.get().toString());
        if (request == null) {
            return;
        }
        Task<Boolean> task = paymentsClient.isReadyToPay(request);
        task.addOnCompleteListener(
                new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        try {
                            boolean result = task.getResult(ApiException.class);
                            if (result) {
                                // show Google as a payment option
                                btGooglePay = findViewById(R.id.btGooglePay);
                                btGooglePay.setOnClickListener(
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                requestPayment(view);
                                            }
                                        });
                                btGooglePay.setVisibility(View.VISIBLE);
                            }
                        } catch (ApiException exception) {
                            // handle developer errors
                            Log.e(TAG, "Checking GooglePay feature error",exception);
                        }
                    }
                });
    }

    private void enableSamsungPay() {
        btSamsungPay = findViewById(R.id.btSamsungPay);

        bundle = new Bundle();
        bundle.putString(
                SamsungPay.PARTNER_SERVICE_TYPE, SamsungPay.ServiceType.INAPP_PAYMENT.toString()
        );

        PartnerInfo pInfo = new PartnerInfo(serviceId, bundle);
        samsungPay = new SamsungPay(this, pInfo);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            btSamsungPay.setVisibility(View.INVISIBLE);
        } else {
            samsungPay.getSamsungPayStatus(new StatusListener() {
                @Override
                public void onSuccess(int i, Bundle bundle) {
                    processSamsungPayStatus(i);
                }

                @Override
                public void onFail(int i, Bundle bundle) {
                    btSamsungPay.setVisibility(View.INVISIBLE);
                    Log.d(TAG, "checkSamsungPayStatus onFail() : " + i);
                }
            });
        }
        btSamsungPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSamsungPay();
            }
        });
    }

    /**
     * Handle a resolved activity from the Google Pay payment sheet
     *
     * @param requestCode the request code originally supplied to AutoResolveHelper in
     *     requestPayment()
     * @param resultCode the result code returned by the Google Pay API
     * @param data an Intent from the Google Pay API containing payment or error data
     * @see <a href="https://developer.android.com/training/basics/intents/result">Getting a result
     *     from an Activity</a>
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "requestCode: " + requestCode + "; resultCode: " + resultCode);
        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Log.d("TAG", "RESULT_OK");
                    try {
                        PaymentData paymentData = PaymentData.getFromIntent(data);
                        String json = paymentData.toJson();
                        Log.d(TAG, "PAYMENT_DATA: " + json);
                        // if using gateway tokenization, pass this token without modification
                        JSONObject paymentMethodData = new JSONObject(json).getJSONObject("paymentMethodData");
                        String paymentToken = paymentMethodData
                                .getJSONObject("tokenizationData")
                                .getString("token");

                        payToken(paymentToken, PaymentTokenType.GOOGLE_PAY);

                    } catch (Exception e) {
                        Log.e(TAG, "Getting GooglePay payment data error ", e);
                    }
                    break;
                case Activity.RESULT_CANCELED:
                    Log.d("TAG", "RESULT_CANCELED");
                    break;
                case AutoResolveHelper.RESULT_ERROR:
                    Log.d("TAG", "RESULT_ERROR");
                    Status status = AutoResolveHelper.getStatusFromIntent(data);
                    // Log the status for debugging.
                    // Generally, there is no need to show an error to the user.
                    // The Google Pay payment sheet will present any account errors.
                    Log.e(TAG, "Getting Google Pay token error: " + status);
                    break;
                default:
                    Log.d("TAG", "DEFAULT");
                    // Do nothing.
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Display the Google Pay payment sheet after interaction with the Google Pay payment button
     *
     * @param view optionally uniquely identify the interactive element prompting for payment
     */
    public void requestPayment(View view) {
        Optional<JSONObject> paymentDataRequestJson =
                GooglePay.getPaymentDataRequest(
                        "Example Merchant",
                        data.getMerchantID(),
                        data.getFields().get(FieldName.OrderAmount),
                        data.getFields().get(FieldName.OrderCurrency));
        if (!paymentDataRequestJson.isPresent()) {
            return;
        }
        PaymentDataRequest request =
                PaymentDataRequest.fromJson(paymentDataRequestJson.get().toString());
        if (request != null) {
            AutoResolveHelper.resolveTask(
                    paymentsClient.loadPaymentData(request), this, LOAD_PAYMENT_DATA_REQUEST_CODE);
        }
    }

    private void payWeb() {
        engine.setEngineListener(new PEngineListener());
        engine.payWeb(this, data, configuration.isUseCamera());
    }

    private void payToken(String token, PaymentTokenType type) {
        Log.d("TAG", "Pay with token: [" + token + "] " + type);
        data.setPaymentToken(token);
        engine.setEngineListener(new PEngineListener());
        showProgress(getString(R.string.please_wait));
        engine.payToken(this, data, type);
    }

    private void declineByNumber(String orderNumber) {
        Log.d(TAG, "Decline by orderNumber=" + orderNumber);
        AssistMerchant m = new AssistMerchant(data.getMerchantID(), data.getLogin(), data.getPassword());
        AssistTransactionStorage storage = engine.transactionStorage();
        Cursor cursor = storage.getData(orderNumber);
        if (cursor != null) {
            cursor.moveToFirst();
            AssistTransaction t = storage.transactionFromCursor(cursor);

            if (t != null) {
                AssistResult.OrderState state = t.getResult().getOrderState();
                if (AssistResult.OrderState.IN_PROCESS.equals(state) || AssistResult.OrderState.UNKNOWN.equals(state)) {
                    engine.setEngineListener(new PEngineListener());
                    showProgress(getString(R.string.please_wait));
                    engine.declineByNumberPayment(this, t, m);
                }
            }
        }
    }

    private void showAlertDialog(Activity activity, String dlgTitle, String dlgMessage) {
        Log.d(TAG, "Show alert: " + dlgTitle);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(dlgTitle);
        builder.setMessage(dlgMessage);
        builder.setNeutralButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    void showProgress(String message) {
        // Construct a progress dialog to prevent user from actions until connection is finished.
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            if (TextUtils.isEmpty(message)) {
                progressDialog.setMessage(getString(R.string.please_wait));
            } else {
                progressDialog.setMessage(message);
            }
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        } else {
            progressDialog.setMessage(message);
        }
    }

    void hideProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    /**
     * Слушатель результата проведения оплаты
     */
    private class PEngineListener implements PayEngineListener {
        @Override
        public void onFinished(Activity activity, AssistTransaction assistTransaction) {
            hideProgress();
            Intent intent = new Intent(ConfirmationActivity.this, ViewResultActivity.class);
            intent.putExtra(ViewResultActivity.TRANSACTION_ID_EXTRA, assistTransaction.getId());
            startActivity(intent);
        }
        @Override
        public void onCanceled(Activity activity, AssistTransaction assistTransaction) {
            hideProgress();

        }
        @Override
        public void onFailure(Activity activity, String info) {
            hideProgress();
            showAlertDialog(activity, getString(R.string.alert_dlg_title_error), info);
        }
        @Override
        public void onNetworkError(Activity activity, String message) {
            hideProgress();
            showAlertDialog(activity, getString(R.string.alert_dlg_title_network_error), message);
        }
    }

    private void processSamsungPayStatus(int status) {
        switch (status) {
            case SamsungPay.SPAY_NOT_SUPPORTED:
                btSamsungPay.setVisibility(View.INVISIBLE);
                break;
            case SamsungPay.SPAY_NOT_READY:
                int extra_reason = bundle.getInt(SamsungPay.EXTRA_ERROR_REASON);
                switch(extra_reason) {
                    case SamsungPay.ERROR_SPAY_APP_NEED_TO_UPDATE:
                        samsungPay.goToUpdatePage();
                        break;
                    case SamsungPay.ERROR_SPAY_SETUP_NOT_COMPLETED:
                        samsungPay.activateSamsungPay();
                        break;
                    default:
                        btSamsungPay.setVisibility(View.INVISIBLE);
                        Log.e(TAG, "Samsung PAY is not ready, extra reason: " + extra_reason);
                }
                btSamsungPay.setVisibility(View.INVISIBLE);
                break;
            case SamsungPay.SPAY_READY:
                // Samsung Pay is ready
                btSamsungPay.setVisibility(View.VISIBLE);
                break;
            default:
                // Not expected result
                btSamsungPay.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void startSamsungPay() {
        try {
            Bundle bundle = new Bundle();
            bundle.putString(SamsungPay.PARTNER_SERVICE_TYPE, SamsungPay.ServiceType.INAPP_PAYMENT.toString());

            PartnerInfo partnerInfo = new PartnerInfo(serviceId, bundle);
            paymentManager = new PaymentManager(this, partnerInfo);
            paymentManager.startInAppPay(makeTransactionDetails(), transactionListener);
        } catch (NullPointerException e) {
            Toast.makeText(this, "All mandatory fields cannot be null.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IllegalStateException e) {
            Toast.makeText(this, "IllegalStateException", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Amount values is not valid", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "PaymentInfo values not valid or all mandatory fields not set.",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /*
     * TransactionInfoListener is for listening to callback events of online (in-app) payments.
     * This is invoked when card or address is changed by the user on the payment sheet,
     * and also with the success or failure of online (in-app) payment.
     */
    private PaymentManager.TransactionInfoListener transactionListener =
            new PaymentManager.TransactionInfoListener() {
                // This callback is received when the user modifies or selects a new address
                // on the payment sheet.
                @Override
                public void onAddressUpdated(PaymentInfo paymentInfo) {
                    try {
                        /* Do address verification by merchant app
                         * setAddressInPaymentSheet(PaymentInfo.AddressInPaymentSheet.NEED_BILLING_SEND_SHIPPING)
                         * If you set NEED_BILLING_SEND_SHIPPING or NEED_BILLING_SPAY with like upper codes,
                         * you can get Billing Address with getBillingAddress().
                         * If you set NEED_BILLING_AND_SHIPPING or NEED_SHIPPING_SPAY,
                         * you can get Shipping Address with getShippingAddress().
                         */
                        PaymentInfo.Amount amount = new PaymentInfo.Amount.Builder()
                                .setCurrencyCode(data.getFields().get(FieldName.OrderCurrency))

                                .setTotalPrice(data.getFields().get(FieldName.OrderAmount))
                                .build();
                        paymentManager.updateAmount(amount);
                    } catch (IllegalStateException | NullPointerException e) {
                        e.printStackTrace();
                    }
                }
                // This callback is received when the user changes the card selected on the payment sheet
                // in Samsung Pay
                @Override
                public void onCardInfoUpdated(CardInfo selectedCardInfo) {
                    /*
                     * Called when the user changes card in Samsung Pay. Newly selected cardInfo is passed and
                     * merchant app can update transaction amount based on new card (if needed).
                     */
                    try {

                        PaymentInfo.Amount amount = new PaymentInfo.Amount.Builder()
                                .setCurrencyCode(data.getFields().get(FieldName.OrderCurrency))
                                .setItemTotalPrice(data.getFields().get(FieldName.OrderAmount))
                                .setShippingPrice("0")
                                .setTax("0")
                                .setTotalPrice(data.getFields().get(FieldName.OrderAmount))
                                .build();
                        // Call updateAmount() method. This is mandatory.
                        paymentManager.updateAmount(amount);
                    } catch (IllegalStateException | NullPointerException e) {
                        e.printStackTrace();
                    }
                }
                /*
                 * This callback is received when the online (in-app) payment transaction is approved by
                 * user and able to successfully generate in-app payload.
                 * The payload could be an encrypted cryptogram (direct in-app payment)
                 * or Payment Gateway's token reference ID (indirect in-app payment).
                 */
                @Override
                public void onSuccess(PaymentInfo response, String paymentCredential,
                                      Bundle extraPaymentData) {
                    payToken(paymentCredential, PaymentTokenType.SAMSUNT_PAY);

                }
                // This callback is received when the online payment transaction has failed.
                @Override
                public void onFailure(int errorCode, Bundle errorData) {
                    Toast.makeText(ConfirmationActivity.this, "Transaction : onFailure : "+ errorCode,
                            Toast.LENGTH_LONG).show();
                }
            };

    private PaymentInfo makeTransactionDetails() {
        ArrayList<PaymentManager.Brand> brandList = new ArrayList<>();
        // If the supported card brand is not specified, all card brands in Samsung Pay are
        // listed in the Payment Sheet. Only Visa and Mastercard are currently supported.
        brandList.add(PaymentManager.Brand.MASTERCARD);
        brandList.add(PaymentManager.Brand.VISA);

        PaymentInfo.Amount amount = new PaymentInfo.Amount.Builder()
                .setCurrencyCode(data.getFields().get(FieldName.OrderCurrency))
                .setItemTotalPrice(data.getFields().get(FieldName.OrderAmount))
                .setShippingPrice("0")
                .setTax("0")
                .setTotalPrice(data.getFields().get(FieldName.OrderAmount))
                .build();
        PaymentInfo.Builder paymentInfoBuilder = new PaymentInfo.Builder();
        String merchantId = data.getMerchantID();
        PaymentInfo paymentInfo = paymentInfoBuilder
                .setMerchantId(merchantId)
                .setMerchantName("Sample Merchant")
                .setOrderNumber(data.getFields().get(FieldName.OrderNumber))
                .setPaymentProtocol(PaymentInfo.PaymentProtocol.PROTOCOL_3DS)
                /* Include NEED_BILLING_SEND_SHIPPING option for AddressInPaymentSheet if merchant needs
                * the billing address from Samsung Pay but wants to send the shipping address to Samsung Pay.
                * Both billing and shipping address will be shown on the payment sheet.
                */
                .setAddressInPaymentSheet(PaymentInfo.AddressInPaymentSheet.DO_NOT_SHOW)
                //.setShippingAddress(shippingAddress)
                .setAllowedCardBrands(brandList)
                .setCardHolderNameEnabled(true)
                .setRecurringEnabled(false)
                .setAmount(amount)
                .build();
        return paymentInfo;
    }
}
