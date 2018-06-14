package ru.assisttech.assistsdk;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import ru.assisttech.sdk.AssistPaymentData;
import ru.assisttech.sdk.AssistSDK;
import ru.assisttech.sdk.engine.AssistPayEngine;

/**
 * Главный экран приложения.
 *
 * Здесь вводятся данные платежа, информация о магазине и сервере
 */
public class MainActivity extends FragmentActivity {

    private static final String TAG = "MainActivity";

    private static final String DEFAULT_MID = "679471";
    private static final String DEFAULT_LOGIN = "admin679471";
    private static final String DEFAULT_PASSWORD = "admin679471";

    private String[] urls = {
            "https://payments.t.paysecure.ru",
            "https://test.paysec.by",
            "https://payments.paysec.by",
            "https://test.paysecure.ru",
            "https://payments.paysecure.ru"
    };

    private EditText etMerchantId;
    private EditText etLogin;
    private EditText etPassword;
    private EditText etOrderNumber;
    private EditText etOrderAmount;
    private Spinner spCurrency;
    private EditText etOrderComment;
    private EditText etCustomerNumber;
    private EditText etSignature;
    private Spinner spURL;
    private CheckBox cbUseCamera;
    private Button btPay;
    private Button btLog;

    protected ApplicationConfiguration configuration;
    private AssistPayEngine engine;
    protected AssistPaymentData data;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        engine = AssistSDK.getPayEngine(this);
        engine.setServerURL("https://payments.t.paysecure.ru");

        initPaymentData();
        configuration = ApplicationConfiguration.getInstance();

        etMerchantId = (EditText)findViewById(R.id.etMerchantID);
        etLogin = (EditText)findViewById(R.id.etLogin);
        etPassword = (EditText)findViewById(R.id.etPassword);

        etOrderNumber = (EditText)findViewById(R.id.etOrderNumber);
        etOrderAmount = (EditText)findViewById(R.id.etOrderAmount);
        spCurrency = (Spinner)findViewById(R.id.spCurrency);
        etOrderComment = (EditText)findViewById(R.id.etOrderComment);
        etCustomerNumber = (EditText)findViewById(R.id.etCustomerNumber);
        etSignature = (EditText)findViewById(R.id.etSignature);
        spURL = (Spinner)findViewById(R.id.spURL);
        cbUseCamera = (CheckBox)findViewById(R.id.cbUseCamera);
        btPay = (Button)findViewById(R.id.btPay);

        // Set defaults
        etMerchantId.setText(DEFAULT_MID);
        etLogin.setText(DEFAULT_LOGIN);
        etPassword.setText(DEFAULT_PASSWORD);

        btLog = (Button)findViewById(R.id.btLog);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.currency,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        spCurrency.setAdapter(adapter);

        ArrayAdapter<String> urlsAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                urls
        );
        urlsAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        spURL.setAdapter(urlsAdapter);
        btPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPayment();
            }
        });
        btLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setConfiguration();
                startActivity(new Intent(MainActivity.this, TransactionsActivity.class));
            }
        });
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_customer:
                startActivity(new Intent(this, CustomerActivity.class));
                return true;
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("TrulyRandom")
    protected void initPaymentData() {
        data = new AssistPaymentData();
        data.clear();
        data.setYMPayment(false);
        data.setWMPayment(false);
        data.setQIWIPayment(false);
        data.setQIWIMegafonPayment(false);
        data.setQIWIMtsPayment(false);
        data.setQIWIBeelinePayment(false);

        // Generate key
        KeyPairGenerator kpg;
        try {
            kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            KeyPair keyPair = kpg.genKeyPair();
            data.setPrivateKey(keyPair.getPrivate());
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
    }

    private void assemblePaymentData() {
        data.setMerchantId(etMerchantId.getText().toString());
        data.setLogin(etLogin.getText().toString());
        data.setPassword(etPassword.getText().toString());
        data.setOrderNumber(etOrderNumber.getText().toString());
        data.setOrderAmount(etOrderAmount.getText().toString());

        int pos = spCurrency.getSelectedItemPosition();
        AssistPaymentData.Currency[] cur = {
                AssistPaymentData.Currency.RUB,
                AssistPaymentData.Currency.USD,
                AssistPaymentData.Currency.EUR,
                AssistPaymentData.Currency.BYN
        };
        data.setOrderCurrency(cur[pos]);
        data.setOrderComment(etOrderComment.getText().toString());
        data.setCustomerNumber(etCustomerNumber.getText().toString());
        if (etSignature.getText().length() > 0) {
            data.setSignature(etSignature.getText().toString());
        }

        CustomerActivity.setContactData(data);
        CustomerActivity.setCustomerData(data);
        CustomerActivity.setCustomerExData(data);
        SettingsActivity.setSettings(data);
        SettingsActivity.setPaymentMode(data);
        SettingsActivity.setRecurringData(data);
    }

    private void setConfiguration() {
        engine.setServerURL(urls[spURL.getSelectedItemPosition()]);
        configuration.setPayEngine(engine);
        configuration.setServer(urls[spURL.getSelectedItemPosition()]);
        configuration.setUseCamera(cbUseCamera.isChecked());
        configuration.setPaymentData(data);
    }

    private void startPayment() {
        assemblePaymentData();
        setConfiguration();
        // К экрану подтверждения данных оплаты
        Intent i = new Intent(this, ConfirmationActivity.class);
        startActivity(i);
    }

}
