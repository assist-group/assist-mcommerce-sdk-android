package ru.assisttech.sdk;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.assisttech.sdk.storage.AssistOrderItem;

public class AssistPaymentData {

    private Map<String, String> values;
    private String merchantID;
    private String login;
    private String password;
    private PrivateKey privateKey;
    private ArrayList<AssistOrderItem> orderItems;

    public AssistPaymentData() {
        values = new HashMap<>();
    }

    public void setMerchantId(String value) {
        merchantID = value;
    }

    public void setLogin(String value) {
        login = value;
    }

    public void setPassword(String value) {
        password = value;
    }

    public void setDelay(boolean value) {
        values.put(FieldName.Delay, Integer.toString(value ? 1 : 0));
    }

    public void setCustomerNumber(String value) {
        values.put(FieldName.CustomerNumber, value);
    }

    public void setOrderNumber(String value) {
        values.put(FieldName.OrderNumber, value);
    }

    public void setLanguage(Lang value) {
        values.put(FieldName.Language, value.toString());
    }

    public void setOrderComment(String value) {
        values.put(FieldName.OrderComment, value);
    }

    public void setOrderAmount(String value) {
        values.put(FieldName.OrderAmount, value);
    }

    public void setOrderItems(ArrayList<AssistOrderItem> items) {
        orderItems = items;
    }

    public void setOrderCurrency(Currency value) {
        values.put(FieldName.OrderCurrency, value.toString());
    }

    public void setPaymentToken(String value) {
        values.put(FieldName.PaymentToken, value.toString());
    }

    public void setLastname(String value) {
        values.put(FieldName.Lastname, value);
    }

    public void setFirstname(String value) {
        values.put(FieldName.Firstname, value);
    }

    public void setMiddlename(String value) {
        values.put(FieldName.Middlename, value);
    }

    public void setEmail(String value) {
        values.put(FieldName.Email, value);
    }

    public void setAddress(String value) {
        values.put(FieldName.Address, value);
    }

    public void setHomePhone(String value) {
        values.put(FieldName.HomePhone, value);
    }

    public void setWorkPhone(String value) {
        values.put(FieldName.WorkPhone, value);
    }

    public void setMobilePhone(String value) {
        values.put(FieldName.MobilePhone, value);
    }

    public void setFax(String value) {
        values.put(FieldName.Fax, value);
    }

    public void setCountry(String value) {
        values.put(FieldName.Country, value);
    }

    public void setState(String value) {
        values.put(FieldName.State, value);
    }

    public void setCity(String value) {
        values.put(FieldName.City, value);
    }

    public void setZip(String value) {
        values.put(FieldName.Zip, value);
    }

    public void setCardPayment(boolean value) {
        values.put(FieldName.CardPayment, Integer.toString(value ? 1 : 0));
    }

    public void setYMPayment(boolean value) {
        values.put(FieldName.YMPayment, Integer.toString(value ? 1 : 0));
    }

    public void setWMPayment(boolean value) {
        values.put(FieldName.WMPayment, Integer.toString(value ? 1 : 0));
    }

    public void setQIWIPayment(boolean value) {
        values.put(FieldName.QIWIPayment, Integer.toString(value ? 1 : 0));
    }

    public void setQIWIMtsPayment(boolean value) {
        values.put(FieldName.QIWIMtsPayment, Integer.toString(value ? 1 : 0));
    }

    public void setQIWIMegafonPayment(boolean value) {
        values.put(FieldName.QIWIMegafonPayment, Integer.toString(value ? 1 : 0));
    }

    public void setQIWIBeelinePayment(boolean value) {
        values.put(FieldName.QIWIBeelinePayment, Integer.toString(value ? 1 : 0));
    }

    public void setMobileDevice(String value) {
        values.put(FieldName.MobileDevice, value);
    }

    public void setRecurringIndicator(boolean value) {
        values.put(FieldName.RecurringIndicator, Integer.toString(value ? 1 : 0));
    }

    public void setRecurringMinAmount(String value) {
        values.put(FieldName.RecurringMinAmount, value);
    }

    public void setRecurringMaxAmount(String value) {
        values.put(FieldName.RecurringMaxAmount, value);
    }

    public void setRecurringPeriod(int value) {
        values.put(FieldName.RecurringPeriod, Integer.toString(value));
    }

    public void setRecurringMaxDate(String value) {
        values.put(FieldName.RecurringMaxDate, value);
    }

    public void setPaymentMode(PaymentMode value) {
        values.put(FieldName.PaymentMode, Integer.toString(value.id));
    }

    public void setXPosTrack2Data(String value) {
        values.put(FieldName.Track2Data, value);
    }

    public void setXPosPinBlock(String value) {
        values.put(FieldName.PinBlock, value);
    }

    public void setXPosKioskNumber(String value) {
        values.put(FieldName.KioskNumber, value);
    }

    public void setXPosPlanMounth(String value) {
        values.put(FieldName.PlanMounth, value);
    }

    public void setXPosInvoiceNumber(String value) {
        values.put(FieldName.InvoiceNumber, value);
    }

    public void setXPosChargeType(String value) {
        values.put(FieldName.ChargeType, value);
    }

    public void setXPosCardType(String value) {
        values.put(FieldName.CardType, value);
    }

    public void setXPosCardNumber(String value) {
        values.put(FieldName.CardNumber, value);
    }

    public void setXPosCardHolder(String value) {
        values.put(FieldName.CardHolder, value);
    }

    public void setXPosExpireMonth(int value) {
        values.put(FieldName.ExpireMonth, Integer.toString(value));
    }

    public void setXPosExpireYear(int value) {
        values.put(FieldName.ExpireYear, Integer.toString(value));
    }

    public void setXPosCVC2(int value) {
        values.put(FieldName.CVC2, Integer.toString(value));
    }

    public void setXPosIssueBank(String value) {
        values.put(FieldName.IssueBank, value);
    }

    public void setXPosAirLineName(String value) {
        values.put(FieldName.AirlineName, value);
    }

    public void setXPosPassengerName(String value) {
        values.put(FieldName.PassengerName, value);
    }

    public void setXPosTicketNumber(String value) {
        values.put(FieldName.TicketNumber, value);
    }

    public void setXPosTicketIssuer(String value) {
        values.put(FieldName.TicketIssuer, value);
    }

    public void setXPosTicketIssuerAddress(String value) {
        values.put(FieldName.TicketIssuerAddress, value);
    }

    public void setXPosDepartureAirport(String value) {
        values.put(FieldName.DepartureAirport, value);
    }

    public void setXPosArrivalAirport1(String value) {
        values.put(FieldName.ArrivalAirport1, value);
    }

    public void setXPosCarrierCode1(String value) {
        values.put(FieldName.CarrierCode1, value);
    }

    public void setXPosAirlineClass1(String value) {
        values.put(FieldName.AirlineClass1, value);
    }

    public void setXPosStopoverCode1(String value) {
        values.put(FieldName.StopoverCode1, value);
    }

    public void setXPosArrivalAirport2(String value) {
        values.put(FieldName.ArrivalAirport2, value);
    }

    public void setXPosCarrierCode2(String value) {
        values.put(FieldName.CarrierCode2, value);
    }

    public void setXPosAirlineClass2(String value) {
        values.put(FieldName.AirlineClass2, value);
    }

    public void setXPosStopoverCode2(String value) {
        values.put(FieldName.StopoverCode2, value);
    }

    public void setXPosArrivalAirport3(String value) {
        values.put(FieldName.ArrivalAirport3, value);
    }

    public void setXPosCarrierCode3(String value) {
        values.put(FieldName.CarrierCode3, value);
    }

    public void setXPosAirlineClass3(String value) {
        values.put(FieldName.AirlineClass3, value);
    }

    public void setXPosStopoverCode3(String value) {
        values.put(FieldName.StopoverCode3, value);
    }

    public void setXPosArrivalAirport4(String value) {
        values.put(FieldName.ArrivalAirport4, value);
    }

    public void setXPosCarrierCode4(String value) {
        values.put(FieldName.CarrierCode4, value);
    }

    public void setXPosAirlineClass4(String value) {
        values.put(FieldName.AirlineClass4, value);
    }

    public void setXPosStopoverCode4(String value) {
        values.put(FieldName.StopoverCode4, value);
    }

    public void setXPosChargeDetails(String value) {
        values.put(FieldName.ChargeDetails, value);
    }

    public void setXPosAgentCode(String value) {
        values.put(FieldName.AgentCode, value);
    }

    public void setXPosRestrictedTicketInd(String value) {
        values.put(FieldName.RestrictedTickedInd, value);
    }

    public void setXPosPNR(String value) {
        values.put(FieldName.PNR, value);
    }

    public void setXPosDepartureDate(String value) {
        values.put(FieldName.DepartureDate, value);
    }

    public void setXPosEMV(String value) {
        values.put(FieldName.EMV, value);
    }

    public void setPrivateKey(PrivateKey keyRSA1024) {
        privateKey = keyRSA1024;
    }

    public void setSignature(String value) {
        values.put(FieldName.Signature, value);
    }

    public String getMerchantID() {
        return merchantID;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public Map<String, String> getFields() {
        return values;
    }

    public ArrayList<AssistOrderItem> getOrderItems() {
        return orderItems;
    }

    public void clear() {
        values = new HashMap<>();
    }

    /**
     * Language
     *
     * @author sergei
     */
    public enum Lang {
        /**
         * English
         **/
        EN,
        /**
         * Russian
         **/
        RU,
        UK
    }

    /**
     * Currency
     *
     * @author sergei
     */
    public enum Currency {
        /**
         * Russian Ruble
         **/
        RUB,
        /**
         * US Dollar
         **/
        USD,
        /**
         * Euro
         **/
        EUR,
        /**
         * Belorussian Ruble
         **/
        BYN,
        /**
         * Australian Dollar
         **/
        AUD,
        /**
         * Azerbaijan Manats
         **/
        AZN,
        /**
         * Bulgarian LeV
         **/
        BGN,
        /**
         * Brazilian Real
         **/
        BRL,
        /**
         * Canadian Dollar
         **/
        CAD,
        /**
         * Swiss Franc
         **/
        CHF,
        /**
         * Chinese Yuan
         **/
        CNY,
        /**
         * Czech Koruna
         **/
        CZK,
        /**
         * Danish Krone
         **/
        DKK,
        /**
         * Estonian Kroon
         **/
        EEK,
        /**
         * British Pound
         **/
        GBP,
        /**
         * Hungarian Forint
         **/
        HUF,
        /**
         * Indian Rupee
         **/
        INR,
        /**
         * Japanese Yen
         **/
        JPY,
        /**
         * Kyrgyzstan Som
         **/
        KGS,
        /**
         * Korean Won
         **/
        KRW,
        /**
         * Kazakh tenge
         **/
        KZT,
        /**
         * Lithuanian Lita
         **/
        LTL,
        /**
         * Latvian Lat
         **/
        LVL,
        /**
         * Moldovan Leu
         **/
        MDL,
        /**
         * Norwegian Krone
         **/
        NOK,
        /**
         * Polish Zloty
         **/
        PLN,
        /**
         * Romanian leu
         **/
        RON,
        /**
         * Swedish Krona
         **/
        SEK,
        /**
         * Singapore Dollar
         **/
        SGD,
        /**
         * Tajik Somoni
         **/
        TJS,
        /**
         * Turkmen manat
         **/
        TMT,
        /**
         * Turkish Lira
         **/
        TRY,
        /**
         * Ukrainian Hryvnia
         **/
        UAH,
        /**
         * Uzbekistan Sum
         **/
        UZS,
        /**
         * South African Rand
         **/
        ZAR
    }

    /**
     * Supported payment modes
     *
     * @author sergei
     */
    public enum PaymentMode {
        /**
         * POS with manual card entering
         **/
        POSKeyEntry(6),
        /**
         * Mail Order/Telephone order
         **/
        MOTO(8),
        /**
         * Chip card
         **/
        EMV(11);

        int id;

        PaymentMode(int id) {
            this.id = id;
        }

        public boolean equals(String value) {
            return id == Integer.valueOf(value);
        }
    }
}
