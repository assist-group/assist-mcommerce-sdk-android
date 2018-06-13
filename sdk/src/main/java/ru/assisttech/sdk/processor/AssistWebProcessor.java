package ru.assisttech.sdk.processor;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Map;
import java.util.Map.Entry;

import ru.assisttech.sdk.AssistMerchant;
import ru.assisttech.sdk.AssistPaymentData;
import ru.assisttech.sdk.AssistResult;
import ru.assisttech.sdk.AssistSDK;
import ru.assisttech.sdk.FieldName;
import ru.assisttech.sdk.R;
import ru.assisttech.sdk.identification.InstallationInfo;
import ru.assisttech.sdk.network.AssistNetworkEngine.NetworkResponseProcessor;
import ru.assisttech.sdk.network.HttpResponse;

/**
 * Класс вызова и обработки ответов web сервиса Ассист {@link ru.assisttech.sdk.AssistAddress#WEB_SERVICE}
 * Используется совместно с классом {@link WebViewActivity}
 *
 * Парсит страницы во время проведения платежа для контроля за процессом оплаты и получения результата.
 */
public class AssistWebProcessor extends AssistBaseProcessor {
	
	private static final String TAG = "AssistWebService";
	private static final int NOT_FOUND = -1;

	private WebContentView webContentView;
	private URL errorURL;
	private CardPageScanner cardPageScanner;

	private boolean useCamera;
    private boolean resultParsingIsInProcess;

	interface WebContentView {
		void provideCardData();
		void close();
	}

	public AssistWebProcessor(Context context, AssistProcessorEnvironment environment, boolean useCamera) {
		super(context, environment);
		this.useCamera = useCamera;
		cardPageScanner = new CardPageScanner();
	}

	// Загрузить и проверить страницу на наличие полей ввода данных карты
    void lookForCardFields(URL page) {
        getNetEngine().loadAndProcessPage(
                page,
                new NetworkConnectionErrorListener(),
				cardPageScanner
        );
    }

    void parseResultPage(URL resultPageUrl) {
        if (!resultParsingIsInProcess) {
            resultParsingIsInProcess = true;
            getNetEngine().loadAndProcessPage(
                    resultPageUrl,
                    new NetworkConnectionErrorListener(),
                    new ResultPageProcessor()
            );
        }
    }

    void parseErrorPage(URL errorPageUrl) {
        errorURL = errorPageUrl;
        getNetEngine().loadAndProcessPage(
                errorPageUrl,
                new NetworkConnectionErrorListener(),
                new ErrorPageProcessor()
        );
    }

    /**
     * Supposed to be called from WebViewActivity to interrupt payment process
     */
    void stopPayment() {
        if (hasListener()) {
            getListener().onTerminated(getTransaction().getId());
        }
        finish();
    }
	
	void onWebContentViewCreated(WebContentView view) {
		webContentView = view;
//        if (hasListener()) {
//            getListener().onActivityCreated(activity);
//        }
	}
	
	boolean useCamera() {
		return useCamera;
	}

	boolean isCardPageDetected() {
		return cardPageScanner.isPageDetected();
	}

	private void onCardPageDetected() {
		webContentView.provideCardData();
	}

    @Override
    protected void run() {
        WebViewActivity.setIgnoreSslErrors(getNetEngine().isCustomSslCertificateUsed());
        getCaller().startActivity(new Intent(getCaller(), WebViewActivity.class));
    }

    @Override
    protected void terminate() {
		if (webContentView != null) {
			webContentView.close();
		}
        getNetEngine().stopTasks();
    }
	
	/**
	 * Web request assembling (HTTP protocol, URL encoded pairs)
	 */	
	String buildRequest() {

        AssistPaymentData data = getEnvironment().getData();
        AssistMerchant m = getEnvironment().getMerchant();
        InstallationInfo iInfo = getEnvironment().getPayEngine().getInstInfo();

		StringBuilder content = new StringBuilder();
        try {
			SignatureProcessor sp = new SignatureProcessor();

    		Map<String, String> params = data.getFields();

			content.append(URLEncoder.encode("Merchant_ID", "UTF-8")).append("=");
			content.append(URLEncoder.encode(m.getID(), "UTF-8")).append("&");

			sp.check4Signature("Merchant_ID", m.getID());

            Log.d(TAG, "Request parameters:");
        	for (Entry<String, String> item: params.entrySet()) {
        		// Parameters to exclude from authorization request
				Log.d(TAG, item.getKey() + ": " + item.getValue());
        		if (!item.getKey().equals(FieldName.Shop)) {
	        		content.append(URLEncoder.encode(item.getKey(), "UTF-8")).append("=");
	        		content.append(URLEncoder.encode(item.getValue(), "UTF-8")).append("&");
        		}
				sp.check4Signature(item.getKey(), item.getValue());
        	}

			if (!params.containsKey(FieldName.Signature)) {
				content.append(URLEncoder.encode(FieldName.Signature, "UTF-8")).append("=");
				content.append(
					URLEncoder.encode(sp.calculateSignature(data.getPrivateKey()), "UTF-8")).append("&");
			}

            // Mobile application specific parameters
            content.append(URLEncoder.encode(FieldName.Device, "UTF-8")).append("=");
	        content.append(URLEncoder.encode("CommerceSDK", "UTF-8")).append("&");

	        content.append(URLEncoder.encode(FieldName.DeviceUniqueID, "UTF-8")).append("=");
	        content.append(URLEncoder.encode(iInfo.getDeiceUniqueId(), "UTF-8")).append("&");

	        content.append(URLEncoder.encode(FieldName.ApplicationName, "UTF-8")).append("=");
	        content.append(URLEncoder.encode(iInfo.appName(), "UTF-8")).append("&");

	        content.append(URLEncoder.encode(FieldName.ApplicationVersion, "UTF-8")).append("=");
	        content.append(URLEncoder.encode(iInfo.versionName(), "UTF-8")).append("&");

            content.append(URLEncoder.encode(FieldName.SDKVersion, "UTF-8")).append("=");
			content.append(URLEncoder.encode(AssistSDK.getSdkVersion(), "UTF-8")).append("&");

//	        content.append(URLEncoder.encode(FieldName.OsLanguage, "UTF-8")).append("=");
//	        content.append(URLEncoder.encode(sysInfo.language(), "UTF-8")).append("&");

            String regId = getEnvironment().getPayEngine().getRegistrationId();
	        content.append(URLEncoder.encode(FieldName.registration_id, "UTF-8")).append("=");
	        content.append(URLEncoder.encode(regId, "UTF-8")).append("&");

        	content.append(URLEncoder.encode("URL_RETURN_OK", "UTF-8")).append("=");
    		content.append(URLEncoder.encode(ResultPageProcessor.SUCCESS_URL, "UTF-8")).append("&");

        	content.append(URLEncoder.encode("URL_RETURN_NO", "UTF-8")).append("=");
    		content.append(URLEncoder.encode(ResultPageProcessor.DECLINE_URL, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
        	e.printStackTrace();
        }
        return content.toString();
	}

	/**
	 * Signature forming helper class
	 */
	private class SignatureProcessor {

		private static final String MERCHANT_ID = "Merchant_ID";
		private static final String ORDER_NUMBER = "OrderNumber";
		private static final String ORDER_AMOUNT = "OrderAmount";
		private static final String ORDER_CURRENCY = "OrderCurrency";
		private static final String CUSTOMER_NUMBER = "CustomerNumber";

		private String Merchant_ID;
		private String OrderNumber;
		private String OrderAmount;
		private String OrderCurrency;
		private String CustomerNumber;

		void check4Signature(String fieldName, String value) {
			switch (fieldName) {
				case MERCHANT_ID:
					Merchant_ID = value;
					break;
				case ORDER_NUMBER:
					OrderNumber = value;
					break;
				case ORDER_AMOUNT:
					OrderAmount = value;
					break;
				case ORDER_CURRENCY:
					OrderCurrency = value;
					break;
				case CUSTOMER_NUMBER:
					CustomerNumber = value;
					break;
			}
		}

		String calculateSignature(PrivateKey key) {

			String input = Merchant_ID + ";"
					     + OrderNumber + ";"
					     + OrderAmount + ";"
					     + OrderCurrency;

			if (!TextUtils.isEmpty(CustomerNumber)) {
				input += ";" + CustomerNumber;
			}

			String signatureB64 = null;

			if (key != null) {
				try {
					Signature sig = Signature.getInstance("MD5WithRSA");
					sig.initSign(key);
					sig.update(input.getBytes("UTF-8"));
					signatureB64 = Base64.encodeToString(sig.sign(), Base64.DEFAULT);

				} catch (NoSuchAlgorithmException|
						InvalidKeyException|
						SignatureException|
						UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			return ((signatureB64 != null) ? signatureB64 : "");
		}
	}
	
	/**
	 * Парсер страницы с информацией об ошибке
	 */
	private class ErrorPageProcessor implements NetworkResponseProcessor {

        private String errorInfo;

        @Override
		public void asyncProcessing(HttpResponse response) {
	   		try {
                // Parse HTML page
                Document doc = Jsoup.parse(response.getData());
                Element body = doc.body();
                Elements els = body.getAllElements();
                StringBuilder info = new StringBuilder();
                for (int i = 0; i < els.size(); i++) {
                    if (els.get(i).hasText()) {
                        info.append(els.get(i).text());
                        info.append(" ");
                    }
                }
                errorInfo = info.toString();
	   		} catch (NullPointerException e) {
	   			e.printStackTrace();
	   			errorInfo = buildMessage(R.string.response_checking_error, e.getMessage());
	   		}			    						    				
		}
		
		@Override
		public void syncPostProcessing() {				
			String resultInfo = "";
			String pageURL = errorURL.toString(); 
			int start = pageURL.indexOf("ErrorID=");
			if (start != NOT_FOUND) {
				start += ("ErrorID=").length();
				int end = pageURL.indexOf('&', start);
				if (end != NOT_FOUND) {
					resultInfo = pageURL.substring(start, end);
				} else {
					resultInfo = pageURL.substring(start);
				}
			}
			resultInfo += (": " + errorInfo);
            if (webContentView != null) {
                webContentView.close();
            }
            if (hasListener()) {
                getListener().onError(getTransaction().getId(), resultInfo);
            }
            finish();
		}
	}

	/**
	 * Класс парсинга HTML страницы на предмет наличия поля для ввода номера карты
	 * {@link CardPageScanner#CARD_NUMBER}
	 */
	private class CardPageScanner implements NetworkResponseProcessor {

		static final String CARD_NUMBER = "CardNumber";

		private boolean isDetected;

		boolean isPageDetected() {
			return isDetected;
		}

		@Override
		public void asyncProcessing(HttpResponse response) {
			isDetected = false;
			try {
				// Parse and check HTML page
				Document doc = Jsoup.parse(response.getData());
				Element cn = doc.getElementById(CARD_NUMBER);
				if (cn != null) {
					isDetected = true;
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void syncPostProcessing() {
			if (isDetected) {
				onCardPageDetected();
			}
		}

	}

	/**
	 * Парсер страницы с результатом платежа
	 */
	private class ResultPageProcessor implements NetworkResponseProcessor {
		
		static final String SUCCESS_URL = "http://succeed_payment.url/";
		static final String DECLINE_URL = "http://declined_payment.url/";

		private String info = null;
		private String billNumber = null;
		private String orderNumber = null;
		private boolean approved = true;
		private boolean error = false;

		@Override
		public void asyncProcessing(HttpResponse response) {

			Log.d(TAG, "Result page: " + response);
 			// Parse HTML page			 
    		try{
				Document doc = Jsoup.parse(response.getData());
				Element body = doc.body();
				
				Elements i1results = body.getElementsByClass("Info1_result");
				if (!i1results.isEmpty()){
					Element inf = i1results.first();
					info = inf.text();
				}

                Elements forms = body.getElementsByAttributeValue("name", "form1");
				if (!forms.isEmpty()){
					Element form = forms.first();					
					String data = form.toString();
					//Log.d(TAG, "data: " + data);
		    		int i = data.indexOf(SUCCESS_URL + "?billnumber=");
		    		if (i == NOT_FOUND) {
		    			approved = false;
		    			i = data.indexOf(DECLINE_URL + "?billnumber=");
		    			if (i != NOT_FOUND) {
							i += (DECLINE_URL + "?billnumber=").length();
						}
		    		} else {    			    
		    			i += (SUCCESS_URL + "?billnumber=").length();
		    		}
					
		    		if (i != NOT_FOUND) {
		    			int end = data.indexOf('&', i);
		    			if (end != NOT_FOUND) {
		    				billNumber = data.substring(i, end);
		    			}
		    		}

					i = data.indexOf("ordernumber=");
					if (i != NOT_FOUND) {
                        i += "ordernumber=".length();
						int end = data.indexOf('&', i);
						if (end != NOT_FOUND) {
                            try {
                                orderNumber = URLDecoder.decode(data.substring(i, end), "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                Log.e(TAG, "OrderNumber decoding error");
                            }
                        }
					}
				}
    		} catch (NullPointerException e) {
    			e.printStackTrace();  	    								
				info = buildMessage(R.string.response_checking_error, e.getMessage());
				error = true;					
    		} finally {
    			if(info == null)
    				info = "";
    			if(billNumber == null)
    				billNumber = "";	    			
    		}	
		}
		
		@Override
		public void syncPostProcessing() {
			AssistResult result = new AssistResult();
    		if (!error) {
	    		if (approved) {
					result.setOrderState(AssistResult.OrderState.APPROVED);
	    		} else {
					result.setOrderState(AssistResult.OrderState.DECLINED);
	    		}
	    		result.setBillNumber(billNumber);
                result.setOrderNumber(orderNumber);
    		}	
    		result.setExtra(info);
            if (hasListener()) {
                getListener().onFinished(getTransaction().getId(), result);
            }
            finish();
            resultParsingIsInProcess = false;
		}
	}

	private String buildMessage(int stringId, String exceptionText){
		return getContext().getString(stringId) + ". (" + exceptionText + ")";
	}	
}
