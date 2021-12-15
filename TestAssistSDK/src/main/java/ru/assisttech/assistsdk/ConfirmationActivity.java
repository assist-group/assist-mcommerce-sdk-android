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
import com.samsung.android.sdk.samsungpay.v2.SpaySdk;
import com.samsung.android.sdk.samsungpay.v2.StatusListener;
import com.samsung.android.sdk.samsungpay.v2.payment.CardInfo;
import com.samsung.android.sdk.samsungpay.v2.payment.CustomSheetPaymentInfo;
import com.samsung.android.sdk.samsungpay.v2.payment.PaymentManager;
import com.samsung.android.sdk.samsungpay.v2.payment.sheet.AmountBoxControl;
import com.samsung.android.sdk.samsungpay.v2.payment.sheet.AmountConstants;
import com.samsung.android.sdk.samsungpay.v2.payment.sheet.CustomSheet;

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

    private static final String AMOUNT_CONTROL_ID = "amountControlId";
    private static final String PRODUCT_ITEM_ID = "productItemId";
    private static final String PRODUCT_TAX_ID = "productTaxId";
    private static final String PRODUCT_SHIPPING_ID = "productShippingId";
    private static final String PRODUCT_FUEL_ID = "productFuelId";

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
            TextView tvLink = findViewById(R.id.tvLink);

            tvServer.setText(configuration.getServer());

            tvMerchant.setText(
                    String.format(
                            Locale.getDefault(),
                            "%1$s [%2$s: %3$s]",
                            data.getMerchantID(), data.getLogin(), data.getPassword()
                    )
            );
            tvOrderNumber.setText(data.getFields().get(FieldName.OrderNumber));
            if (data.getLink() == null) {
                tvOrderAmount.setText(
                        String.format(
                                Locale.getDefault(),
                                "%1$s %2$s",
                                data.getFields().get(FieldName.OrderAmount), data.getFields().get(FieldName.OrderCurrency)
                        )
                );
                tvOrderComment.setText(data.getFields().get(FieldName.OrderComment));
                findViewById(R.id.llLink).setVisibility(View.GONE);
            } else {
                tvLink.setText(data.getLink());
                findViewById(R.id.llOrderAmount).setVisibility(View.GONE);
                findViewById(R.id.llOrderComment).setVisibility(View.GONE);
            }

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
                                                getOrderData(data.getFields().get(FieldName.OrderNumber));
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
                SpaySdk.PARTNER_SERVICE_TYPE, SpaySdk.ServiceType.INAPP_PAYMENT.toString()
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
     */
    public void requestPayment() {
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

    private void getOrderData(String orderNumber) {
        Log.d(TAG, "Get data for orderNumber=" + orderNumber);
        data.setOrderNumber(orderNumber);
        engine.setEngineListener(new OrderDataListener());
        showProgress(getString(R.string.please_wait));
        engine.getAmountForOrder(this, data);
    }

    private void showAlertDialog(Activity activity, String dlgTitle, String dlgMessage) {
        showAlertDialog(activity, dlgTitle, dlgMessage, false);
    }

    private void showAlertDialog(Activity activity, String dlgTitle, String dlgMessage, final boolean finish) {
        Log.d(TAG, "Show alert: " + dlgTitle);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(dlgTitle);
        builder.setMessage(dlgMessage);
        builder.setNeutralButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (finish) {
                    finish();
                }
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
            intent.putExtra(ViewResultActivity.TRANSACTION_ID_EXTRA, assistTransaction != null ? assistTransaction.getId() : -1);
            startActivity(intent);
        }
        @Override
        public void onCanceled(Activity activity, AssistTransaction assistTransaction) {
            hideProgress();
        }
        @Override
        public void onFailure(Activity activity, String info) {
            hideProgress();
            showAlertDialog(activity, getString(R.string.alert_dlg_title_error), info, true);
        }
        @Override
        public void onNetworkError(Activity activity, String message) {
            hideProgress();
            showAlertDialog(activity, getString(R.string.alert_dlg_title_network_error), message);
        }
    }

    private class OrderDataListener implements PayEngineListener {
        @Override
        public void onFinished(Activity activity, AssistTransaction t) {
            hideProgress();
            if (t != null) {
                data.setOrderAmount(t.getOrderAmount());
                data.setOrderCurrency(t.getOrderCurrency());
            }
            requestPayment();
        }
        @Override
        public void onCanceled(Activity activity, AssistTransaction t) {
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
            case SpaySdk.SPAY_NOT_SUPPORTED:
                btSamsungPay.setVisibility(View.INVISIBLE);
                break;
            case SpaySdk.SPAY_NOT_READY:
                int extraReason = bundle.getInt(SpaySdk.EXTRA_ERROR_REASON);
                switch(extraReason) {
                    case SpaySdk.ERROR_SPAY_APP_NEED_TO_UPDATE:
                        samsungPay.goToUpdatePage();
                        break;
                    case SpaySdk.ERROR_SPAY_SETUP_NOT_COMPLETED:
                        samsungPay.activateSamsungPay();
                        break;
                    default:
                        btSamsungPay.setVisibility(View.INVISIBLE);
                        Log.e(TAG, "Samsung PAY is not ready, extra reason: " + extraReason);
                }
                btSamsungPay.setVisibility(View.INVISIBLE);
                break;
            case SpaySdk.SPAY_READY:
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
            bundle.putString(SpaySdk.PARTNER_SERVICE_TYPE, SpaySdk.ServiceType.INAPP_PAYMENT.toString());

            PartnerInfo partnerInfo = new PartnerInfo(serviceId, bundle);
            paymentManager = new PaymentManager(this, partnerInfo);
            paymentManager.startInAppPayWithCustomSheet(makeCustomSheetPaymentInfo(), transactionListener);
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
    private final PaymentManager.CustomSheetTransactionInfoListener transactionListener
            = new PaymentManager.CustomSheetTransactionInfoListener() {
        // This callback is received when the user changes card on the custom payment sheet in Samsung Pay
        @Override
        public void onCardInfoUpdated(CardInfo selectedCardInfo, CustomSheet customSheet) {
            double amount = Double.parseDouble(data.getFields().get(FieldName.OrderAmount));
            /*
             * Called when the user changes card in Samsung Pay.
             * Newly selected cardInfo is passed so merchant app can update transaction amount
             * based on different card (if needed),
             */
            try {
                AmountBoxControl amountBoxControl = (AmountBoxControl) customSheet.getSheetControl(AMOUNT_CONTROL_ID);
                amountBoxControl.updateValue(PRODUCT_ITEM_ID, amount); //item price
                amountBoxControl.updateValue(PRODUCT_TAX_ID, 0); // sales tax
                amountBoxControl.updateValue(PRODUCT_SHIPPING_ID, 0); // Shipping fee
                amountBoxControl.updateValue(PRODUCT_FUEL_ID, 0, "Pending"); // additional item status
                amountBoxControl.setAmountTotal(amount, AmountConstants.FORMAT_TOTAL_PRICE_ONLY); // grand total
                customSheet.updateControl(amountBoxControl);
                // Call updateAmount() method. This is mandatory.
                paymentManager.updateSheet(customSheet);
            } catch (IllegalStateException | NullPointerException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSuccess(CustomSheetPaymentInfo response, String paymentCredential, Bundle extraPaymentData) {
            payToken(paymentCredential, PaymentTokenType.SAMSUNG_PAY);
        }

        @Override
        public void onFailure(int errorCode, Bundle errorData) {
            Toast.makeText(ConfirmationActivity.this, "Transaction : onFailure : " + errorCode,
                            Toast.LENGTH_LONG).show();
        }
    };

    private CustomSheetPaymentInfo makeCustomSheetPaymentInfo() {
        ArrayList<SpaySdk.Brand> brandList = new ArrayList<>();
        // If the supported brand is not specified, all card brands in Samsung Pay are
        // listed in the Payment Sheet.
        brandList.add(SpaySdk.Brand.VISA);
        brandList.add(SpaySdk.Brand.MASTERCARD);
        /*
         * Make the SheetControls you want and add them to custom sheet.
         * Place each control in sequence with AmountBoxControl listed last.
         */
        CustomSheet customSheet = new CustomSheet();
        AmountBoxControl amountBoxControl
                = new AmountBoxControl(AMOUNT_CONTROL_ID, data.getFields().get(FieldName.OrderCurrency));

        double amount = Double.parseDouble(data.getFields().get(FieldName.OrderAmount));

        amountBoxControl.addItem(PRODUCT_ITEM_ID, "Item", amount, ""); //item price
        amountBoxControl.addItem(PRODUCT_TAX_ID, "Tax", 0, ""); // sales tax
        amountBoxControl.addItem(PRODUCT_SHIPPING_ID, "Shipping", 0, ""); // Shipping fee
        amountBoxControl.addItem(PRODUCT_FUEL_ID, "Fuel", 0, ""); // additional item status
        amountBoxControl.setAmountTotal(amount, AmountConstants.FORMAT_TOTAL_PRICE_ONLY); // grand total
        customSheet.addControl(amountBoxControl);
        String merchantId = data.getMerchantID();
        return new CustomSheetPaymentInfo.Builder()
                .setMerchantId(merchantId)
                .setMerchantName("Sample Merchant")
                .setOrderNumber(data.getFields().get(FieldName.OrderNumber))
                .setPaymentProtocol(CustomSheetPaymentInfo.PaymentProtocol.PROTOCOL_3DS)
                .setAddressInPaymentSheet(CustomSheetPaymentInfo.AddressInPaymentSheet.DO_NOT_SHOW)
                .setAllowedCardBrands(brandList)
                .setCardHolderNameEnabled(true)
                .setRecurringEnabled(false)
                .setCustomSheet(customSheet)
                .build();
    }
}