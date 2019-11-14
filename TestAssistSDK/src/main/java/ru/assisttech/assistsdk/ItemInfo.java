package ru.assisttech.assistsdk;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Hard coded info about items for sale.
 */
public class ItemInfo {

    String sellerData;
    String merchantName;
    String name;
    String description;
    String quantity;
    String price;
    String currencyCode;
    String shippingPrice;
    String shippingDescription;
    String tax;
    String taxDescription;

    private ItemInfo() {
    }

    String getTotalPrice() {
        return new BigDecimal(price)
                .multiply(new BigDecimal(quantity))
                .add(new BigDecimal(tax))
                .add(new BigDecimal(shippingPrice))
                .setScale(2, RoundingMode.HALF_EVEN)
                .toString();
    }

    @Override
    public String toString() {
        return name;
    }

    public static class Builder {
        private ItemInfo item;

        public Builder() {
            item = new ItemInfo();
            item.sellerData = "";
            item.merchantName = "";
            item.description = "";
            item.quantity = "1";
            item.tax = "0";
            item.taxDescription = "Tax";
            item.shippingPrice = "0";
            item.shippingDescription = "Shipping";
        }
        public ItemInfo build() {
            return item;
        }
        public Builder setSellerData(String value) {
            item.sellerData = value;
            return this;
        }
        public Builder setMerchantName(String value) {
            item.merchantName = value;
            return this;
        }
        public Builder setName(String value) {
            item.name = value;
            return this;
        }
        public Builder setDescription(String value) {
            item.description = value;
            return this;
        }
        public Builder setQuantity(String value) {
            item.quantity = value;
            return this;
        }
        public Builder setPrice(String value) {
            item.price = value;
            return this;
        }
        public Builder setCurrencyCode(String value) {
            item.currencyCode = value;
            return this;
        }
        public Builder setShippingPrice(String value) {
            item.shippingPrice = value;
            return this;
        }
        public Builder setShippingDescription(String value) {
            item.shippingDescription = value;
            return this;
        }
        public Builder setTax(String value) {
            item.tax = value;
            return this;
        }
        public Builder setTaxDescription(String value) {
            item.taxDescription = value;
            return this;
        }
    }
}
