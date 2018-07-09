package ru.assisttech.sdk.storage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.assisttech.sdk.AssistPaymentData;
import ru.assisttech.sdk.AssistResult;

public class AssistTransactionFilter {

    private Date dateBegin;
    private Date dateEnd;
    private String oState;
    private String amountMin;
    private String amountMax;
    private String currency;

    public Date getDateBegin() {
        return dateBegin;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public AssistResult.OrderState getOrderState() {
        if (oState != null)
            return AssistResult.OrderState.valueOf(oState);
        else
            return null;
    }

    public String getAmountMin() {
        return amountMin;
    }

    public String getAmountMax() {
        return amountMax;
    }

    public AssistPaymentData.Currency getCurrency() {
        if (currency != null)
            return AssistPaymentData.Currency.valueOf(currency);
        else
            return null;
    }

    public void setDateBegin(Date value) {
        dateBegin = value;
    }

    public void setDateEnd(Date value) {
        dateEnd = value;
    }

    public void setOrderState(AssistResult.OrderState value) {
        if (value != null)
            oState = value.toString();
        else
            oState = null;
    }

    public void setAmountMin(String value) {
        amountMin = value;
    }

    public void setAmountMax(String value) {
        amountMax = value;
    }

    public void setCurrency(AssistPaymentData.Currency value) {
        if (value != null)
            currency = value.toString();
        else
            currency = null;
    }

    public void reset() {
        dateBegin = null;
        dateEnd = null;
        oState = null;
        amountMin = null;
        amountMax = null;
        currency = null;
    }

    public boolean isSet() {
        return dateBegin != null ||
                dateEnd != null ||
                oState != null ||
                amountMin != null ||
                amountMax != null ||
                currency != null;
    }

    public boolean match(AssistTransaction t) {
        if (!isSet() || t == null)
            return false;

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.US);
        Date date;
        try {
            date = sdf.parse(t.getOrderDateDevice());
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }

        if (dateBegin != null && date.before(dateBegin))
            return false;

        if (dateEnd != null && date.after(dateEnd))
            return false;

        if (oState != null && !oState.equals(t.getResult().getOrderState().toString()))
            return false;

        int tAmount = (int) (Float.parseFloat(t.getOrderAmount()) * 10.0f);

        if (amountMin != null) {
            int filterMin = (int) (Float.parseFloat(amountMin) * 10.0f);
            if (tAmount < filterMin)
                return false;
        }

        if (amountMax != null) {
            int filterMax = (int) (Float.parseFloat(amountMax) * 10.0f);
            if (tAmount > filterMax)
                return false;
        }

        if (currency != null && !currency.equals(t.getOrderCurrency().toString()))
            return false;

        return true;
    }
}