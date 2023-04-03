package ru.assisttech.sdk.processor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.net.MalformedURLException;
import java.net.URL;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import ru.assisttech.sdk.BuildConfig;
import ru.assisttech.sdk.R;
import ru.assisttech.sdk.cardreader.AssistCard;
import ru.assisttech.sdk.engine.AssistPayEngine;

/**
 * Класс Activity для отображения и ввода данных в процессе оплаты банковской картой через web сервис Ассист
 * {@link ru.assisttech.sdk.AssistAddress#WEB_SERVICE}
 * <p>
 * Используется совместно с {@link AssistWebProcessor}
 * <p>
 * Отображает содержимое HTML страниц в WebView.
 * Осуществляет контроль за загружаемыми страницами.
 * Заполняет поля для карточных данных на соответствующей странице.
 * Запускает сканирование банковской карты камерой смартфона с использованием библиотеки card.io
 */
public class WebViewActivity extends Activity implements AssistWebProcessor.WebContentView {

    private static final String TAG = "WebViewActivity";

    private static final String CARD_KEY = "wv_activity.scanned_card";
    private static final int SCAN_REQUEST_CODE = 3;
    private static final int MENU_ITEM_ID = 5;

    private ProgressBar progressBar;
    private FrameLayout placeholder;
    private WebView webView;
    private boolean enableMenu;

    private AssistCard assistCard;

    private AssistWebProcessor webProcessor;

    private static boolean ignoreSslErrors;

    public static void setIgnoreSslErrors(boolean value) {
        ignoreSslErrors = value;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webProcessor = AssistPayEngine.getInstance(this).getWebProcessor();
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webProcessor.useCamera()) {
            String url = webView.getUrl();
            if (!TextUtils.isEmpty(url)) {
                if (url.contains("pay.cfm") && webProcessor.isCardPageDetected()) {
                    enableMenu = true;
                    invalidateOptionsMenu();
                }
            }
        }
    }

    // Обработка поворота экрана.
    // Просто отсоединяем от ActivityLayout экземпляр WebView, чтобы не пересоздавать его и не перезагружать страницу.
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (webView != null) {
            placeholder.removeView(webView);
        }
        super.onConfigurationChanged(newConfig);
        initUI();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
        if (assistCard != null) {
            outState.putParcelable(CARD_KEY, assistCard);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);

        if (savedInstanceState.containsKey(CARD_KEY)) {
            assistCard = savedInstanceState.getParcelable(CARD_KEY);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initUI() {
        setContentView(R.layout.web_activity);
        progressBar = findViewById(R.id.progress_bar);
        placeholder = findViewById(R.id.web_fragment);

        if (webView == null) {
            webView = new WebView(this);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setLoadsImagesAutomatically(true);
            webView.getSettings().setUseWideViewPort(false);
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.getSettings().setDisplayZoomControls(false);
            webView.setWebViewClient(new PayWebViewClient());
            webView.setWebChromeClient(new PayWebChromeClient());
            if (webProcessor == null) {
                finish();
                return;
            }
            postRequest();
        }
        webView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        // Подсоединяем готовый WebView к ActivityLayout
        placeholder.addView(webView);

        webProcessor.onWebContentViewCreated(this);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack())
            webView.goBack();
        else
            showAlert();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (enableMenu) {
            menu.add(Menu.NONE, MENU_ITEM_ID, Menu.NONE, R.string.scan_card);
        }
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_ITEM_ID) {
            startCardScanning();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Обработка результата сканирование банковской карты камерой смартфона
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCAN_REQUEST_CODE) {
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {

                assistCard = new AssistCard();

                CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
                assistCard.setPan(scanResult.cardNumber);

                if (scanResult.isExpiryValid()) {
                    assistCard.setExpireMonth(scanResult.expiryMonth);
                    assistCard.setExpireYear(scanResult.expiryYear);
                }
                fillInCardFields(assistCard.getPan(), assistCard.getExpireMonth(), assistCard.getExpireYear());
            }
        }
    }

    public void startCardScanning() {
        Intent scanIntent = new Intent(this, CardIOActivity.class);
        // customize these values to suit your needs.
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true);        // default: true
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false);            // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false);    // default: false

        startActivityForResult(scanIntent, SCAN_REQUEST_CODE);
    }

    public void postRequest() {
        String url = webProcessor.getURL().toString();
        String content = webProcessor.buildRequest();
        Log.d(TAG, "POST address: " + url);
        Log.d(TAG, "POST content: " + content);
        webView.postUrl(url, content.getBytes());
    }

    public void fillInCardFields(String number, String month, String year) {
        String script = "function fillForm() {"
                + "    document.getElementById('CardNumber').value='" + number + "';"
                + "    var month = document.getElementById('ExpireMonth');"
                + "    for (var i = 0; i < month.options.length; i++) {"
                + "        if (month.options[i].value == '" + month + "') {"
                + "            month.selectedIndex = i;"
                + "            break;"
                + "        }"
                + "    }"
                + "    var year = document.getElementById('ExpireYear');"
                + "    for (var i = 0; i < year.options.length; i++) {"
                + "        if (year.options[i].value == '" + year + "') {"
                + "            year.selectedIndex = i;"
                + "            break;"
                + "        }"
                + "    }"
                + "};"
                + "fillForm();";
        webView.loadUrl("javascript:" + script);
        enableMenu = true;
        invalidateOptionsMenu();
    }

    /**
     * {@link AssistWebProcessor.WebContentView}
     */
    @Override
    public void provideCardData() {
        if (assistCard == null) {
            startCardScanning();
        } else {
            fillInCardFields(assistCard.getPan(), assistCard.getExpireMonth(), assistCard.getExpireYear());
        }
    }

    /**
     * {@link AssistWebProcessor.WebContentView}
     */
    @Override
    public void close() {
        finish();
    }

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }

    private void showAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dlg_title_warning));
        builder.setMessage(getString(R.string.dlg_msg_stop_payment_question));
        builder.setPositiveButton(getString(android.R.string.yes), (dialog, which) -> {
            webProcessor.stopPayment();
            finish();
        });
        builder.setNegativeButton(getString(android.R.string.no), (dialog, which) -> {});
        AlertDialog dlg = builder.create();
        dlg.show();
    }

    private URL StringToUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private class PayWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            Log.d(TAG, "Progress: " + newProgress);
            super.onProgressChanged(view, newProgress);
        }
    }

    private class PayWebViewClient extends WebViewClient {
        // Set webview to ignore SSL error for pages with certificates unknown for Android
        // We check certificates manually using HttpsURLConnection
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            if (ignoreSslErrors) {
                Log.e(TAG, "SSL Error ignored");
                handler.proceed();
            } else {
                Log.e(TAG, "SSL Error: " + error.toString());
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "shouldOverrideUrlLoading(): " + url);
            if (url.contains("result.cfm")) {
                webProcessor.parseResultPage(StringToUrl(url));
                return true;
            } else if (url.contains("body.cfm")) {
                webProcessor.parseErrorPage(StringToUrl(url));
                return true;
            } else if (url.contains("mirpay://pay.mironline.ru")) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(TAG, "PageStarted: " + url);
            showProgress();
            enableMenu = false;
            invalidateOptionsMenu();
            webView.setScrollX(0);
            webView.setScrollY(0);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d(TAG, "PageFinished: " + url);
            hideProgress();

            if (url.contains("body.cfm")) {
                webProcessor.parseErrorPage(StringToUrl(url));
            } else if (webProcessor.useCamera() && url.contains("pay.cfm")) {
                webProcessor.lookForCardFields(StringToUrl(url));
            }
        }
    }
}