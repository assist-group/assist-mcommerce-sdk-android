package ru.assisttech.assistsdk;

import com.github.dmstocking.optional.java.util.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Google Pay API request configurations
 *
 * @see <a href="https://developers.google.com/pay/api/android/">Google Pay API Android
 *     documentation</a>
 */
class GooglePay {

    // Test
    static final String GOOGLE_PAY_MERCHANT_ID = "02510116604241796260";
    // Prod
    //public static final String GOOGLE_PAY_MERCHANT_ID = "16590966430175452581";

    static final String GOOGLE_PAY_GATEWAY = "assist";

    /**
     * Create a Google Pay API base request object with properties used in all requests
     *
     * @return Google Pay API base request object
     * @throws JSONException
     */
    private static JSONObject getBaseRequest() throws JSONException {
        return new JSONObject()
                .put("apiVersion", 2)
                .put("apiVersionMinor", 0);
    }

    /**
     * Identify your gateway and your app's gateway merchant identifier
     *
     * <p>The Google Pay API response will return an encrypted payment method capable of being charged
     * by a supported gateway after payer authorization
     *
     * <p>TODO: check with your gateway on the parameters to pass
     *
     * @return payment data tokenization for the CARD payment method
     * @throws JSONException
     * @see <a
     *     href="https://developers.google.com/pay/api/android/reference/object#PaymentMethodTokenizationSpecification">PaymentMethodTokenizationSpecification</a>
     */
    private static JSONObject getTokenizationSpecification(String gatewayMerchantId) throws JSONException {
        JSONObject tokenizationSpecification = new JSONObject();
        tokenizationSpecification.put("type", "PAYMENT_GATEWAY");
        tokenizationSpecification.put(
                "parameters",
                new JSONObject()
                        .put("gateway", GOOGLE_PAY_GATEWAY)
                        .put("gatewayMerchantId", gatewayMerchantId));

        return tokenizationSpecification;
    }

    /**
     * Card networks supported by your app and your gateway
     *
     * <p>TODO: confirm card networks supported by your app and gateway
     *
     * @return allowed card networks
     * @see <a
     *     href="https://developers.google.com/pay/api/android/reference/object#CardParameters">CardParameters</a>
     */
    private static JSONArray getAllowedCardNetworks() {
        return new JSONArray()
                .put("MASTERCARD")
                .put("VISA")
                .put("MIR");
    }

    /**
     * Card authentication methods supported by your app and your gateway
     *
     * <p>TODO: confirm your processor supports Android device tokens on your supported card networks
     *
     * @return allowed card authentication methods
     * @see <a
     *     href="https://developers.google.com/pay/api/android/reference/object#CardParameters">CardParameters</a>
     */
    private static JSONArray getAllowedCardAuthMethods() {
        return new JSONArray()
                .put("CRYPTOGRAM_3DS");
//                .put("PAN_ONLY");
    }

    /**
     * Describe your app's support for the CARD payment method
     *
     * <p>The provided properties are applicable to both an IsReadyToPayRequest and a
     * PaymentDataRequest
     *
     * @return a CARD PaymentMethod object describing accepted cards
     * @throws JSONException
     * @see <a
     *     href="https://developers.google.com/pay/api/android/reference/object#PaymentMethod">PaymentMethod</a>
     */
    private static JSONObject getBaseCardPaymentMethod() throws JSONException {
        JSONObject cardPaymentMethod = new JSONObject();
        cardPaymentMethod.put("type", "CARD");
        cardPaymentMethod.put(
                "parameters",
                new JSONObject()
                        .put("allowedAuthMethods", GooglePay.getAllowedCardAuthMethods())
                        .put("allowedCardNetworks", GooglePay.getAllowedCardNetworks()));

        return cardPaymentMethod;
    }

    /**
     * Describe the expected returned payment data for the CARD payment method
     *
     * @return a CARD PaymentMethod describing accepted cards and optional fields
     * @throws JSONException
     * @see <a
     *     href="https://developers.google.com/pay/api/android/reference/object#PaymentMethod">PaymentMethod</a>
     */
    private static JSONObject getCardPaymentMethod(String gatewayMerchantId) throws JSONException {
        JSONObject cardPaymentMethod = GooglePay.getBaseCardPaymentMethod();
        cardPaymentMethod.put("tokenizationSpecification", GooglePay.getTokenizationSpecification(gatewayMerchantId));

        return cardPaymentMethod;
    }

    /**
     * Provide Google Pay API with a payment amount, currency, and amount status
     *
     * @return information about the requested payment
     * @throws JSONException
     * @see <a
     *     href="https://developers.google.com/pay/api/android/reference/object#TransactionInfo">TransactionInfo</a>
     */
    private static JSONObject getTransactionInfo(String amount, String currency) throws JSONException {
        JSONObject transactionInfo = new JSONObject();
        transactionInfo.put("totalPrice", amount);
        transactionInfo.put("totalPriceStatus", "FINAL");
        transactionInfo.put("currencyCode", currency);

        return transactionInfo;
    }

    /**
     * Information about the merchant requesting payment information
     *
     * @return information about the merchant
     * @throws JSONException
     * @see <a
     *     href="https://developers.google.com/pay/api/android/reference/object#MerchantInfo">MerchantInfo</a>
     */
    private static JSONObject getMerchantInfo(String merchantName) throws JSONException {
        return new JSONObject()
                .put("merchantId", GOOGLE_PAY_MERCHANT_ID)
                .put("merchantName", merchantName);

    }

    /**
     * An object describing accepted forms of payment by your app, used to determine a viewer's
     * readiness to pay
     *
     * @return API version and payment methods supported by the app
     * @see <a
     *     href="https://developers.google.com/pay/api/android/reference/object#IsReadyToPayRequest">IsReadyToPayRequest</a>
     */
    public static Optional<JSONObject> getIsReadyToPayRequest() {
        try {
            JSONObject isReadyToPayRequest = GooglePay.getBaseRequest();
            isReadyToPayRequest.put("allowedPaymentMethods", new JSONArray().put(getBaseCardPaymentMethod()));
            return Optional.of(isReadyToPayRequest);
        } catch (JSONException e) {
            return Optional.empty();
        }
    }

    /**
     * An object describing information requested in a Google Pay payment sheet
     *
     * @return payment data expected by your app
     * @see <a
     *     href="https://developers.google.com/pay/api/android/reference/object#PaymentDataRequest">PaymentDataRequest</a>
     */
    public static Optional<JSONObject> getPaymentDataRequest(String merchantName, String gatewayMerchantId, String amount, String currency) {
        try {
            JSONObject paymentDataRequest = GooglePay.getBaseRequest();
            paymentDataRequest.put("allowedPaymentMethods", new JSONArray().put(GooglePay.getCardPaymentMethod(gatewayMerchantId)));
            paymentDataRequest.put("transactionInfo", GooglePay.getTransactionInfo(amount, currency));
            paymentDataRequest.put("merchantInfo", GooglePay.getMerchantInfo(merchantName));
            return Optional.of(paymentDataRequest);
        } catch (JSONException e) {
            return Optional.empty();
        }
    }
}

