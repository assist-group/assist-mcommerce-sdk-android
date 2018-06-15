package ru.assisttech.sdk;

/**
 * Result of payment
 *
 * @author sergei
 * @version 1.0
 */
public class AssistResult {

    /**
     * Status of payment result
     **/
    private OrderState state;
    /**
     * Result code
     **/
    private String approvalCode;
    /**
     * Number of payment in Assist
     **/
    private String billNumber;
    /**
     * Additional information
     **/
    private String extraInfo;
    /**
     * Order number returned by Assist payment system
     * May differ from the order number sent by mobile application
     * in case if order number auto-generating enabled for certain Merchant ID
     */
    private String orderNumber;

    private String meantypeName;
    private String meanNumber;
    private String cardholder;
    private String cardExpirationDate;


    /**
     * Possible order state values
     */
    public enum OrderState {
        UNKNOWN, /**
         * application level status (does not exist in Assist)
         **/
        IN_PROCESS,
        DELAYED,
        APPROVED,
        PARTIAL_APPROVED,
        PARTIAL_DELAYED,
        CANCELED,
        PARTIAL_CANCELED,
        DECLINED,
        TIMEOUT,
        CANCEL_ERROR, /**
         * application level status (does not exist in Assist)
         **/
        NON_EXISTENT;

        /**
         * application level status (does not exist in Assist)
         **/

        public String toString() {
            switch (this) {
                case UNKNOWN:
                    return "Unknown";
                case IN_PROCESS:
                    return "In Process";
                case DELAYED:
                    return "Delayed";
                case APPROVED:
                    return "Approved";
                case PARTIAL_APPROVED:
                    return "PartialApproved";
                case PARTIAL_DELAYED:
                    return "PartialDelayed";
                case CANCELED:
                    return "Canceled";
                case PARTIAL_CANCELED:
                    return "PartialCanceled";
                case DECLINED:
                    return "Declined";
                case TIMEOUT:
                    return "Timeout";
                case CANCEL_ERROR:
                    return "Cancel error";
                case NON_EXISTENT:
                    return "Non-existent";
                default:
                    return "Unknown";
            }
        }

        public String toText() {

            switch (this) {
                case UNKNOWN:
                    return "Неизвестно";
                case IN_PROCESS:
                    return "В процессе";
                case DELAYED:
                    return "Ожидает подтверждения оплаты";
                case APPROVED:
                    return "Оплачен";
                case PARTIAL_APPROVED:
                    return "Оплачен частично";
                case PARTIAL_DELAYED:
                    return "Подтвержден частично";
                case CANCELED:
                    return "Отменён";
                case PARTIAL_CANCELED:
                    return "Отменён частично";
                case DECLINED:
                    return "Отклонён";
                case TIMEOUT:
                    return "Закрыт по истечении времени";
                case CANCEL_ERROR:
                    return "Ошибка отмены";
                case NON_EXISTENT:
                    return "Не существует";
                default:
                    return "Неизвестно";
            }
        }

        public static OrderState fromString(String value) {
            switch (value) {
                case "Unknown":
                case "Неизвестно":
                    return UNKNOWN;
                case "In Process":
                case "В процессе":
                    return IN_PROCESS;
                case "Delayed":
                case "Ожидает подтверждения оплаты":
                    return DELAYED;
                case "Approved":
                case "Оплачен":
                    return APPROVED;
                case "PartialApproved":
                case "Оплачен частично":
                    return PARTIAL_APPROVED;
                case "PartialDelayed":
                case "Подтвержден частично":
                    return PARTIAL_DELAYED;
                case "Canceled":
                case "Отменён":
                    return CANCELED;
                case "PartialCanceled":
                case "Отменён частично":
                    return PARTIAL_CANCELED;
                case "Declined":
                case "Отклонён":
                    return DECLINED;
                case "Timeout":
                case "Закрыт по истечении времени":
                    return TIMEOUT;
                case "Cancel error":
                case "Ошибка отмены":
                    return CANCEL_ERROR;
                case "Non-existent":
                case "Не существует":
                    return NON_EXISTENT;
                default:
                    return UNKNOWN;
            }
        }

    }

    public AssistResult() {
        state = OrderState.UNKNOWN;
    }

    public AssistResult(OrderState os) {
        if (os == null) {
            state = OrderState.UNKNOWN;
        } else {
            state = os;
        }
    }

    public AssistResult(OrderState os, String extra) {
        if (os == null) {
            state = OrderState.UNKNOWN;
        } else {
            state = os;
        }
        extraInfo = extra;
    }

    /**
     * Set {@link AssistResult#state}
     *
     * @param os
     */
    public void setOrderState(OrderState os) {
        state = os;
    }

    /**
     * Set {@link AssistResult#state} as String
     *
     * @param value
     */
    public void setOrderState(String value) {
        if (value == null) {
            setOrderState(OrderState.UNKNOWN);
        } else {
            setOrderState(OrderState.fromString(value));
        }
    }

    /**
     * Returns order state
     *
     * @return {@link AssistResult#state} as String
     */
    public OrderState getOrderState() {
        return state;
    }

    /**
     * Get payment number in Assist
     *
     * @return {@link AssistResult#billNumber}
     */
    public String getBillNumber() {
        return billNumber;
    }

    /**
     * Set {@link AssistResult#billNumber}
     *
     * @param value
     */
    public void setBillNumber(String value) {
        billNumber = value;
    }

    /**
     * Set {@link AssistResult#approvalCode}
     *
     * @param code
     */
    public void setApprovalCode(String code) {
        approvalCode = code;
    }

    /**
     * Get result code
     *
     * @return {@link AssistResult#approvalCode}
     */
    public String getApprovalCode() {
        return approvalCode;
    }

    /**
     * Set {@link AssistResult#extraInfo}
     *
     * @param value
     */
    public void setExtra(String value) {
        extraInfo = value;
    }

    /**
     * Get additional information (i.e. error reason, ..)
     *
     * @return {@link AssistResult#extraInfo}
     */
    public String getExtra() {
        return extraInfo;
    }

    /**
     * Used in web payment
     *
     * @return order number
     */
    public String getOrderNumber() {
        return orderNumber;
    }

    public void setMeantypeName(String value) {
        meantypeName = value;
    }

    public String getMeantypeName() {
        return meantypeName;
    }

    public void setMeanNumber(String value) {
        meanNumber = value;
    }

    public String getMeanNumber() {
        return meanNumber;
    }

    public void setCardholder(String value) {
        cardholder = value;
    }

    public String getCardholder() {
        return cardholder;
    }

    public void setCardExpirationDate(String value) {
        cardExpirationDate = value;
    }

    public String getCardExpirationDate() {
        return cardExpirationDate;
    }

    /**
     * Used in web payment
     *
     * @param value order number returned by Assist
     */
    public void setOrderNumber(String value) {
        orderNumber = value;
    }

    public boolean isUnknown() {
        return (OrderState.UNKNOWN == state);
    }

    public boolean isInProcess() {
        return (OrderState.IN_PROCESS == state);
    }

    public boolean isDelayed() {
        return (OrderState.DELAYED == state);
    }

    public boolean isApproved() {
        return (OrderState.APPROVED == state);
    }

    public boolean isPartialApproved() {
        return (OrderState.PARTIAL_APPROVED == state);
    }

    public boolean isPartialDelayed() {
        return (OrderState.PARTIAL_DELAYED == state);
    }

    public boolean isCanceled() {
        return (OrderState.CANCELED == state);
    }

    public boolean isPartialCanceled() {
        return (OrderState.PARTIAL_CANCELED == state);
    }

    public boolean isDeclined() {
        return (OrderState.DECLINED == state);
    }

    public boolean isTimeout() {
        return (OrderState.TIMEOUT == state);
    }

    public boolean isNonExistent() {
        return (OrderState.NON_EXISTENT == state);
    }

    public boolean isPositive() {
        return (isInProcess() ||
                isDelayed() ||
                isApproved() ||
                isPartialApproved() ||
                isPartialDelayed() ||
                isCanceled() ||
                isPartialCanceled());
    }

    public boolean isNegative() {
        return (isDeclined() || isTimeout());
    }

    public boolean isCancelError() {
        return (OrderState.CANCEL_ERROR == state);
    }
}
