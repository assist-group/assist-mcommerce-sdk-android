package ru.assisttech.sdk.storage;

import android.text.TextUtils;

import java.util.ArrayList;

import ru.assisttech.sdk.AssistPaymentData.Currency;
import ru.assisttech.sdk.AssistResult;

public class AssistTransaction {

    public static final int UNREGISTERED_ID = -1;
    private long id;
    private String merchantID;
    private String orderDateUTC;
    private String orderDateDevice;
    private String orderNumber;
    private String orderComment;
    private String orderAmount;
    private Currency orderCurrency;
    private PaymentMethod paymentMethod;
    private boolean requireUserSignature;
    private byte[] userSignature;
    private AssistResult result;
    private ArrayList<AssistOrderItem> orderItems;

    public enum PaymentMethod {
        CARD_MANUAL,
        CARD_PHOTO_SCAN,
        CARD_TERMINAL,
        CASH
    }

    public AssistTransaction() {
        result = new AssistResult(AssistResult.OrderState.UNKNOWN);
        id = UNREGISTERED_ID;
    }

    public boolean isStored() {
        return id != UNREGISTERED_ID;
    }

    public long getId() {
        return id;
    }

    public String getMerchantID() {
        return merchantID;
    }

    public String getOrderDateUTC() {
        return orderDateUTC;
    }

    public String getOrderDateDevice() {
        return orderDateDevice;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getOrderComment() {
        return orderComment;
    }

    public String getOrderAmount() {
        return orderAmount;
    }

    public Currency getOrderCurrency() {
        return orderCurrency;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public boolean isRequireUserSignature() {
        return requireUserSignature;
    }

    public byte[] getUserSignature() {
        return userSignature;
    }

    public AssistResult getResult() {
        return result;
    }

    public boolean hasOrderItems() {
        return orderItems != null;
    }

    public ArrayList<AssistOrderItem> getOrderItems() {
        return orderItems;
    }

    public void setId(long value) {
        id = value;
    }

    public void setMerchantID(String value) {
        merchantID = value;
    }

    public void setOrderDateUTC(String value) {
        orderDateUTC = value;
    }

    public void setOrderDateDevice(String value) {
        orderDateDevice = value;
    }

    public void setOrderNumber(String value) {
        orderNumber = value;
    }

    public void setOrderComment(String value) {
        orderComment = value;
    }

    public void setOrderAmount(String value) {
        orderAmount = value;
        if (orderAmount != null) {
            if (orderAmount.contains(",")) {
                orderAmount = orderAmount.replace(",", ".");
            }
            orderAmount = formatAmount(orderAmount);
        }
    }

    public void setOrderCurrency(Currency value) {
        orderCurrency = value;
    }

    public void setPaymentMethod(PaymentMethod value) {
        paymentMethod = value;
    }

    public void setRequireUserSignature(boolean value) {
        requireUserSignature = value;
    }

    public void setUserSignature(byte[] value) {
        userSignature = value;
    }

    public void setResult(AssistResult value) {
        result = value;
    }

    public void setOrderItems(ArrayList<AssistOrderItem> items) {
        orderItems = items;
    }

    public static String formatAmount(String amount) {
        if (TextUtils.isEmpty(amount))
            return amount;

        String outAmount;
        int diff = -1;
        if (amount.contains(".")) {
            diff = amount.length() - 1 - amount.indexOf(".");
        } else if (amount.contains(",")) {
            diff = amount.length() - 1 - amount.indexOf(",");
        }
        switch (diff) {
            case 0:
                outAmount = amount.concat("00");
                break;
            case 1:
                outAmount = amount.concat("0");
                break;
            default:
                outAmount = amount;
                break;
        }
        return outAmount;
    }
}
