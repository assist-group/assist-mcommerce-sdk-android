package ru.assisttech.sdk.storage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class AssistOrderUtils {

    private static final String ORDER_ITEM_NAME = "n";
    private static final String ORDER_ITEM_UNIT_PRICE = "up";
    private static final String ORDER_ITEM_QUANTITY = "q";

    public static ArrayList<AssistOrderItem> fromJsonString(String jsonString) throws Exception {
        JSONArray jsonItems = new JSONArray(jsonString);
        ArrayList<AssistOrderItem> items = new ArrayList<>();
        for (int i = 0; i < jsonItems.length(); i++) {
            JSONObject jsonItem = jsonItems.getJSONObject(i);
            String name = jsonItem.getString(ORDER_ITEM_NAME);
            String unitPrice = jsonItem.getString(ORDER_ITEM_UNIT_PRICE);
            String quantity = jsonItem.getString(ORDER_ITEM_QUANTITY);
            items.add(new AssistOrderItem(name, quantity, unitPrice));
        }
        return items;
    }

    public static String toJsonString(ArrayList<AssistOrderItem> items) throws Exception {
        if (items == null || items.isEmpty()) {
            return null;
        }
        JSONArray jsonItems = new JSONArray();
        for (int i = 0; i < items.size(); i++) {
            AssistOrderItem item = items.get(i);
            JSONObject jsonItem = new JSONObject();
            jsonItem.put(ORDER_ITEM_NAME, item.getName());
            jsonItem.put(ORDER_ITEM_UNIT_PRICE, item.getUnitPrice());
            jsonItem.put(ORDER_ITEM_QUANTITY, item.getQuantity());
            jsonItems.put(jsonItem);
        }
        return jsonItems.toString();
    }
}
