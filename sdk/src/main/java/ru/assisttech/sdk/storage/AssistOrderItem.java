package ru.assisttech.sdk.storage;

import java.util.Locale;

public class AssistOrderItem {

    public int id = -1;
    private String name;
    private String product;
    private String quantity;
    private String price;
    private String amount;
    private String tax;
    private String fpmode;
    private String hscode;
    private String eancode;
    private String gs1code;
    private String furcode;
    private String uncode;
    private String egaiscode;
    private String subjtype;
    private String bonus;
    private String discount;
    private String agentMode;
    private String transferOperatorPhone;
    private String transferOperatorName;
    private String transferOperatorAddress;
    private String transferOperatorINN;
    private String paymentReceiverOperatorPhone;
    private String paymentAgentOperation;
    private String paymentAgentPhone;
    private String supplierPhone;
    private String supplierName;
    private String supplierINN;
    private String excise;
    private String countryOfOrigin;
    private String numberOfCustomsDeclaration;
    private String lineAttribute;

    public AssistOrderItem(String name, String quantity, String price, String amount) {
        this.name = name;
        this.quantity = quantity.replace(",", ".");
        this.price = processMoney(price.replace(",", "."));
        this.amount = processMoney(amount.replace(",", "."));
    }

    public String getName() {
        return name;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getPrice() {
        return price;
    }

    private String processMoney(String price) {
        Float p = Float.valueOf(price);
        return String.format(Locale.US, "%1$3.2f", p);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getAmount() {
        return amount;
    }

    public String getTax() {
        return tax;
    }

    public void setTax(String tax) {
        this.tax = tax;
    }

    public String getFpmode() {
        return fpmode;
    }

    public void setFpmode(String fpmode) {
        this.fpmode = fpmode;
    }

    public String getHscode() {
        return hscode;
    }

    public void setHscode(String hscode) {
        this.hscode = hscode;
    }

    public String getEancode() {
        return eancode;
    }

    public void setEancode(String eancode) {
        this.eancode = eancode;
    }

    public String getGs1code() {
        return gs1code;
    }

    public void setGs1code(String gs1code) {
        this.gs1code = gs1code;
    }

    public String getFurcode() {
        return furcode;
    }

    public void setFurcode(String furcode) {
        this.furcode = furcode;
    }

    public String getUncode() {
        return uncode;
    }

    public void setUncode(String uncode) {
        this.uncode = uncode;
    }

    public String getEgaiscode() {
        return egaiscode;
    }

    public void setEgaiscode(String egaiscode) {
        this.egaiscode = egaiscode;
    }

    public String getSubjtype() {
        return subjtype;
    }

    public void setSubjtype(String subjtype) {
        this.subjtype = subjtype;
    }

    public String getBonus() {
        return bonus;
    }

    public void setBonus(String bonus) {
        this.bonus = bonus;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getAgentMode() {
        return agentMode;
    }

    public void setAgentMode(String agentMode) {
        this.agentMode = agentMode;
    }

    public String getTransferOperatorPhone() {
        return transferOperatorPhone;
    }

    public void setTransferOperatorPhone(String transferOperatorPhone) {
        this.transferOperatorPhone = transferOperatorPhone;
    }

    public String getTransferOperatorName() {
        return transferOperatorName;
    }

    public void setTransferOperatorName(String transferOperatorName) {
        this.transferOperatorName = transferOperatorName;
    }

    public String getTransferOperatorAddress() {
        return transferOperatorAddress;
    }

    public void setTransferOperatorAddress(String transferOperatorAddress) {
        this.transferOperatorAddress = transferOperatorAddress;
    }

    public String getTransferOperatorINN() {
        return transferOperatorINN;
    }

    public void setTransferOperatorINN(String transferOperatorINN) {
        this.transferOperatorINN = transferOperatorINN;
    }

    public String getPaymentReceiverOperatorPhone() {
        return paymentReceiverOperatorPhone;
    }

    public void setPaymentReceiverOperatorPhone(String paymentReceiverOperatorPhone) {
        this.paymentReceiverOperatorPhone = paymentReceiverOperatorPhone;
    }

    public String getPaymentAgentOperation() {
        return paymentAgentOperation;
    }

    public void setPaymentAgentOperation(String paymentAgentOperation) {
        this.paymentAgentOperation = paymentAgentOperation;
    }

    public String getPaymentAgentPhone() {
        return paymentAgentPhone;
    }

    public void setPaymentAgentPhone(String paymentAgentPhone) {
        this.paymentAgentPhone = paymentAgentPhone;
    }

    public String getSupplierPhone() {
        return supplierPhone;
    }

    public void setSupplierPhone(String supplierPhone) {
        this.supplierPhone = supplierPhone;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierINN() {
        return supplierINN;
    }

    public void setSupplierINN(String supplierINN) {
        this.supplierINN = supplierINN;
    }

    public String getExcise() {
        return excise;
    }

    public void setExcise(String excise) {
        this.excise = excise;
    }

    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }

    public void setCountryOfOrigin(String countryOfOrigin) {
        this.countryOfOrigin = countryOfOrigin;
    }

    public String getNumberOfCustomsDeclaration() {
        return numberOfCustomsDeclaration;
    }

    public void setNumberOfCustomsDeclaration(String numberOfCustomsDeclaration) {
        this.numberOfCustomsDeclaration = numberOfCustomsDeclaration;
    }

    public String getLineAttribute() {
        return lineAttribute;
    }

    public void setLineAttribute(String lineAttribute) {
        this.lineAttribute = lineAttribute;
    }

    public String toJSON() {
        StringBuilder json = new StringBuilder("{");
        if (name != null) {
            json.append("\"name\"=\"").append(name).append("\",");
        }
        if (product != null) {
            json.append("\"product\"=\"").append(product).append("\",");
        }
        if (quantity != null) {
            json.append("\"quantity\"=").append(quantity).append(",");
        }
        if (price != null) {
            json.append("\"price\"=").append(price).append(",");
        }
        if (amount != null) {
            json.append("\"amount\"=").append(amount).append(",");
        }
        if (tax != null) {
            json.append("\"tax\"=\"").append(tax).append("\",");
        }
        if (fpmode != null) {
            json.append("\"fpmode\"=\"").append(fpmode).append("\",");
        }
        if (hscode != null) {
            json.append("\"hscode\"=\"").append(hscode).append("\",");
        }
        if (eancode != null) {
            json.append("\"eancode\"=\"").append(eancode).append("\",");
        }
        if (gs1code != null) {
            json.append("\"gs1code\"=\"").append(gs1code).append("\",");
        }
        if (furcode != null) {
            json.append("\"furcode\"=\"").append(furcode).append("\",");
        }
        if (uncode != null) {
            json.append("\"uncode\"=\"").append(uncode).append("\",");
        }
        if (egaiscode != null) {
            json.append("\"egaiscode\"=\"").append(egaiscode).append("\",");
        }
        if (subjtype != null) {
            json.append("\"subjtype\"=\"").append(subjtype).append("\",");
        }
        if (bonus != null) {
            json.append("\"bonus\"=\"").append(bonus).append("\",");
        }
        if (discount != null) {
            json.append("\"discount\"=\"").append(discount).append("\",");
        }
        if (agentMode != null) {
            json.append("\"agentMode\"=\"").append(agentMode).append("\",");
        }
        if (transferOperatorPhone != null) {
            json.append("\"transferOperatorPhone\"=\"").append(transferOperatorPhone).append("\",");
        }
        if (transferOperatorName != null) {
            json.append("\"transferOperatorName\"=\"").append(transferOperatorName).append("\",");
        }
        if (transferOperatorAddress != null) {
            json.append("\"transferOperatorAddress\"=\"").append(transferOperatorAddress).append("\",");
        }
        if (transferOperatorINN != null) {
            json.append("\"transferOperatorINN\"=\"").append(transferOperatorINN).append("\",");
        }
        if (paymentReceiverOperatorPhone != null) {
            json.append("\"paymentReceiverOperatorPhone\"=\"").append(paymentReceiverOperatorPhone).append("\",");
        }
        if (paymentAgentOperation != null) {
            json.append("\"paymentAgentOperation\"=\"").append(paymentAgentOperation).append("\",");
        }
        if (paymentAgentPhone != null) {
            json.append("\"paymentAgentPhone\"=\"").append(paymentAgentPhone).append("\",");
        }
        if (supplierPhone != null) {
            json.append("\"supplierPhone\"=\"").append(supplierPhone).append("\",");
        }
        if (supplierName != null) {
            json.append("\"supplierName\"=\"").append(supplierName).append("\",");
        }
        if (supplierINN != null) {
            json.append("\"supplierINN\"=\"").append(supplierINN).append("\",");
        }
        if (excise != null) {
            json.append("\"excise\"=\"").append(excise).append("\",");
        }
        if (countryOfOrigin != null) {
            json.append("\"countryOfOrigin\"=\"").append(countryOfOrigin).append("\",");
        }
        if (numberOfCustomsDeclaration != null) {
            json.append("\"numberOfCustomsDeclaration\"=\"").append(numberOfCustomsDeclaration).append("\",");
        }
        if (lineAttribute != null) {
            json.append("\"lineAttribute\"=\"").append(lineAttribute).append("\",");
        }
        json.append("\"id\"=").append(id).append("}");
        return json.toString();
    }
}
