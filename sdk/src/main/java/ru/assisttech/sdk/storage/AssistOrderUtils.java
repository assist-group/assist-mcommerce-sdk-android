package ru.assisttech.sdk.storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AssistOrderUtils {

    private static final String ORDER_ITEM_ID = "id";
    private static final String ORDER_ITEM_NAME = "name";
    private static final String ORDER_ITEM_PRICE = "price";
    private static final String ORDER_ITEM_QUANTITY = "quantity";
    private static final String ORDER_ITEM_AMOUNT = "amount";
    private static final String ORDER_ITEM_PRODUCT = "product";
    private static final String ORDER_ITEM_TAX = "tax";
    private static final String ORDER_ITEM_FPMODE = "fpmode";
    private static final String ORDER_ITEM_HSCODE = "hscode";
    private static final String ORDER_ITEM_EANCODE = "eancode";
    private static final String ORDER_ITEM_GS1CODE = "gs1code";
    private static final String ORDER_ITEM_FURCODE = "furcode";
    private static final String ORDER_ITEM_UNCODE = "uncode";
    private static final String ORDER_ITEM_EGAISCODE = "egaiscode";
    private static final String ORDER_ITEM_SUBJTYPE = "subjtype";
    private static final String ORDER_ITEM_BONUS = "bonus";
    private static final String ORDER_ITEM_DISCOUNT = "discount";
    private static final String ORDER_ITEM_AGENT_MODE = "agentMode";
    private static final String ORDER_ITEM_TRANSFER_OPERATOR_PHONE = "transferOperatorPhone";
    private static final String ORDER_ITEM_TRANSFER_OPERATOR_NAME = "transferOperatorName";
    private static final String ORDER_ITEM_TRANSFER_OPERATOR_ADDRESS = "transferOperatorAddress";
    private static final String ORDER_ITEM_TRANSFER_OPERATOR_INN = "transferOperatorINN";
    private static final String ORDER_ITEM_PAYMENT_RECEIVER_OPERATOR_PHONE = "paymentReceiverOperatorPhone";
    private static final String ORDER_ITEM_PAYMENT_AGENT_OPERATION = "paymentAgentOperation";
    private static final String ORDER_ITEM_PAYMENT_AGENT_PHONE = "paymentAgentPhone";
    private static final String ORDER_ITEM_SUPPLIER_PHONE = "supplierPhone";
    private static final String ORDER_ITEM_SUPPLIER_NAME = "supplierName";
    private static final String ORDER_ITEM_SUPPLIER_INN = "supplierINN";
    private static final String ORDER_ITEM_EXCISE = "excise";
    private static final String ORDER_ITEM_COUNTRY_OF_ORIGIN = "countryOfOrigin";
    private static final String ORDER_ITEM_NUMBER_OF_CUSTOMS_DECLARATION = "numberOfCustomsDeclaration";
    private static final String ORDER_ITEM_LINE_ATTRIBUTE = "lineAttribute";

    public static List<AssistOrderItem> fromJsonString(String jsonString) throws Exception {
        JSONArray jsonItems = new JSONArray(jsonString);
        List<AssistOrderItem> items = new ArrayList<>();
        for (int i = 0; i < jsonItems.length(); i++) {
            JSONObject jsonItem = jsonItems.getJSONObject(i);

            String name = jsonItem.getString(ORDER_ITEM_NAME);
            String price = jsonItem.getString(ORDER_ITEM_PRICE);
            String quantity = jsonItem.getString(ORDER_ITEM_QUANTITY);
            String amount = jsonItem.getString(ORDER_ITEM_AMOUNT);

            AssistOrderItem item = new AssistOrderItem(name, quantity, price, amount);

            item.setId(jsonItem.getInt(ORDER_ITEM_ID));
            item.setProduct(getElement(jsonItem, ORDER_ITEM_PRODUCT));
            item.setTax(getElement(jsonItem, ORDER_ITEM_TAX));
            item.setFpmode(getElement(jsonItem, ORDER_ITEM_FPMODE));
            item.setHscode(getElement(jsonItem, ORDER_ITEM_HSCODE));
            item.setEancode(getElement(jsonItem, ORDER_ITEM_EANCODE));
            item.setGs1code(getElement(jsonItem, ORDER_ITEM_GS1CODE));
            item.setFurcode(getElement(jsonItem, ORDER_ITEM_FURCODE));
            item.setUncode(getElement(jsonItem, ORDER_ITEM_UNCODE));
            item.setEgaiscode(getElement(jsonItem, ORDER_ITEM_EGAISCODE));
            item.setSubjtype(getElement(jsonItem, ORDER_ITEM_SUBJTYPE));
            item.setBonus(getElement(jsonItem, ORDER_ITEM_BONUS));
            item.setDiscount(getElement(jsonItem, ORDER_ITEM_DISCOUNT));
            item.setAgentMode(getElement(jsonItem, ORDER_ITEM_AGENT_MODE));
            item.setTransferOperatorPhone(getElement(jsonItem, ORDER_ITEM_TRANSFER_OPERATOR_PHONE));
            item.setTransferOperatorName(getElement(jsonItem, ORDER_ITEM_TRANSFER_OPERATOR_NAME));
            item.setTransferOperatorAddress(getElement(jsonItem, ORDER_ITEM_TRANSFER_OPERATOR_ADDRESS));
            item.setTransferOperatorINN(getElement(jsonItem, ORDER_ITEM_TRANSFER_OPERATOR_INN));
            item.setPaymentReceiverOperatorPhone(getElement(jsonItem, ORDER_ITEM_PAYMENT_RECEIVER_OPERATOR_PHONE));
            item.setPaymentAgentOperation(getElement(jsonItem, ORDER_ITEM_PAYMENT_AGENT_OPERATION));
            item.setPaymentAgentPhone(getElement(jsonItem, ORDER_ITEM_PAYMENT_AGENT_PHONE));
            item.setSupplierPhone(getElement(jsonItem, ORDER_ITEM_SUPPLIER_PHONE));
            item.setSupplierName(getElement(jsonItem, ORDER_ITEM_SUPPLIER_NAME));
            item.setSupplierINN(getElement(jsonItem, ORDER_ITEM_SUPPLIER_INN));
            item.setExcise(getElement(jsonItem, ORDER_ITEM_EXCISE));
            item.setCountryOfOrigin(getElement(jsonItem, ORDER_ITEM_COUNTRY_OF_ORIGIN));
            item.setNumberOfCustomsDeclaration(getElement(jsonItem, ORDER_ITEM_NUMBER_OF_CUSTOMS_DECLARATION));
            item.setLineAttribute(getElement(jsonItem, ORDER_ITEM_LINE_ATTRIBUTE));

            items.add(item);
        }
        return items;
    }

    private static String getElement(JSONObject obj, String name) {
        try {
            return obj.getString(name);
        } catch (JSONException ex) {
            // nothing
        }
        return null;
    }

    static String toJsonString(List<AssistOrderItem> items) throws Exception {
        if (items == null || items.isEmpty()) {
            return null;
        }
        JSONArray jsonItems = new JSONArray();
        for (int i = 0; i < items.size(); i++) {
            AssistOrderItem item = items.get(i);
            JSONObject jsonItem = new JSONObject();
            jsonItem.put(ORDER_ITEM_ID, item.getId());
            jsonItem.put(ORDER_ITEM_NAME, item.getName());
            jsonItem.put(ORDER_ITEM_PRICE, item.getPrice());
            jsonItem.put(ORDER_ITEM_QUANTITY, item.getQuantity());
            jsonItem.put(ORDER_ITEM_AMOUNT, item.getAmount());
            jsonItem.put(ORDER_ITEM_PRODUCT, item.getProduct());
            jsonItem.put(ORDER_ITEM_TAX, item.getTax());
            jsonItem.put(ORDER_ITEM_FPMODE, item.getFpmode());
            jsonItem.put(ORDER_ITEM_HSCODE, item.getHscode());
            jsonItem.put(ORDER_ITEM_EANCODE, item.getEancode());
            jsonItem.put(ORDER_ITEM_GS1CODE, item.getGs1code());
            jsonItem.put(ORDER_ITEM_UNCODE, item.getUncode());
            jsonItem.put(ORDER_ITEM_FURCODE, item.getFurcode());
            jsonItem.put(ORDER_ITEM_EGAISCODE, item.getEgaiscode());
            jsonItem.put(ORDER_ITEM_SUBJTYPE, item.getSubjtype());
            jsonItem.put(ORDER_ITEM_BONUS, item.getBonus());
            jsonItem.put(ORDER_ITEM_DISCOUNT, item.getDiscount());
            jsonItem.put(ORDER_ITEM_AGENT_MODE, item.getAgentMode());
            jsonItem.put(ORDER_ITEM_TRANSFER_OPERATOR_PHONE, item.getTransferOperatorPhone());
            jsonItem.put(ORDER_ITEM_TRANSFER_OPERATOR_NAME, item.getTransferOperatorName());
            jsonItem.put(ORDER_ITEM_TRANSFER_OPERATOR_ADDRESS, item.getTransferOperatorAddress());
            jsonItem.put(ORDER_ITEM_TRANSFER_OPERATOR_INN, item.getTransferOperatorINN());
            jsonItem.put(ORDER_ITEM_PAYMENT_RECEIVER_OPERATOR_PHONE, item.getPaymentReceiverOperatorPhone());
            jsonItem.put(ORDER_ITEM_PAYMENT_AGENT_OPERATION, item.getPaymentAgentOperation());
            jsonItem.put(ORDER_ITEM_PAYMENT_AGENT_PHONE, item.getPaymentAgentPhone());
            jsonItem.put(ORDER_ITEM_SUPPLIER_PHONE, item.getSupplierPhone());
            jsonItem.put(ORDER_ITEM_SUPPLIER_NAME, item.getSupplierName());
            jsonItem.put(ORDER_ITEM_SUPPLIER_INN, item.getSupplierINN());
            jsonItem.put(ORDER_ITEM_EXCISE, item.getExcise());
            jsonItem.put(ORDER_ITEM_COUNTRY_OF_ORIGIN, item.getCountryOfOrigin());
            jsonItem.put(ORDER_ITEM_NUMBER_OF_CUSTOMS_DECLARATION, item.getNumberOfCustomsDeclaration());
            jsonItem.put(ORDER_ITEM_LINE_ATTRIBUTE, item.getLineAttribute());

            jsonItems.put(jsonItem);
        }
        return jsonItems.toString();
    }
}