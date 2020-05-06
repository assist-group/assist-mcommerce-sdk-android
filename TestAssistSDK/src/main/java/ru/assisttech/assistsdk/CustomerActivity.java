package ru.assisttech.assistsdk;

import android.os.Bundle;
import android.widget.EditText;

import ru.assisttech.sdk.AssistPaymentData;

public class CustomerActivity extends UpButtonActivity {

    private static final String DEFAULT_LAST_NAME = "Shumakher";
    private static final String DEFAULT_FIRST_NAME = "Michael";
    private static final String DEFAULT_MIDDLE_NAME = "Alexandrovich";
    private static final String DEFAULT_EMAIL = "shumka@customs.ps";
    private static final String DEFAULT_ADDRESS = "Russia";
    private static final String DEFAULT_PHONE = "1234567";
    private static final String DEFAULT_WORK_PHONE = "2345678";
    private static final String DEFAULT_MOBILE_PHONE = "3456789";
    private static final String DEFAULT_FAX = "4567890";
    private static final String DEFAULT_COUNTRY = "Russia";
    private static final String DEFAULT_STATE = "Leningradskaya oblast";
    private static final String DEFAULT_CITY = "Saint-Petersburg";
    private static final String DEFAULT_ZIP = "192334";
    private static final String DEFAULT_TAXPAYER_ID = "";
    private static final String DEFAULT_CUSTOMER_DOC_ID = "";
    private static final String DEFAULT_PAYMENT_ADDRESS = "";
    private static final String DEFAULT_PAYMENT_PLACE = "";
    private static final String DEFAULT_CASHIER = "";
    private static final String DEFAULT_CASHIER_INN = "";
    private static final String DEFAULT_PAYMENT_TERMINAL = "";
    private static final String DEFAULT_TRANSFER_OPERATOR_PHONE = "";
    private static final String DEFAULT_TRANSFER_OPERATOR_NAME = "";
    private static final String DEFAULT_TRANSFER_OPERATOR_ADDRESS = "";
    private static final String DEFAULT_TRANSFER_OPERATOR_INN = "";
    private static final String DEFAULT_PAYMENT_RECEIVER_OPERATOR_PHONE = "";
    private static final String DEFAULT_PAYMENT_AGENT_PHONE = "";
    private static final String DEFAULT_PAYMENT_AGENT_OPERATION = "";
    private static final String DEFAULT_SUPPLIER_PHONE = "";
    private static final String DEFAULT_PAYMENT_AGENT_MODE = "";
    private static final String DEFAULT_DOCUMENT_REQUISITE = "";
    private static final String DEFAULT_USER_REQUISITES = "";
    private static final String DEFAULT_COMPANY_NAME = "";

    static String lastName = DEFAULT_LAST_NAME;
    static String firstName = DEFAULT_FIRST_NAME;
    static String middleName = DEFAULT_MIDDLE_NAME;

    static String email = DEFAULT_EMAIL;
    static String address;
    static String homePhone;
    static String workPhone;
    static String mobilePhone;
    static String fax;

    static String country;
    static String state;
    static String city;
    static String zip;

    static String taxpayerID;
    static String customerDocID;
    static String paymentAddress;
    static String paymentPlace;
    static String cashier;
    static String cashierINN;
    static String paymentTerminal;
    static String transferOperatorPhone;
    static String transferOperatorName;
    static String transferOperatorAddress;
    static String transferOperatorINN;
    static String paymentReceiverOperatorPhone;
    static String paymentAgentPhone;
    static String paymentAgentOperation;
    static String supplierPhone;
    static String paymentAgentMode;
    static String documentRequisite;
    static String userRequisites;
    static String companyName;

    private EditText etLastName;
    private EditText etFirstName;
    private EditText etMiddleName;

    private EditText etEmail;
    private EditText etAddress;
    private EditText etHomePhone;
    private EditText etWorkPhone;
    private EditText etMobilePhone;
    private EditText etFax;

    private EditText etCountry;
    private EditText etState;
    private EditText etCity;
    private EditText etZip;

    private EditText etTaxpayerID;
    private EditText etCustomerDocID;
    private EditText etPaymentAddress;
    private EditText etPaymentPlace;
    private EditText etCashier;
    private EditText etCashierINN;
    private EditText etPaymentTerminal;
    private EditText etTransferOperatorPhone;
    private EditText etTransferOperatorName;
    private EditText etTransferOperatorAddress;
    private EditText etTransferOperatorINN;
    private EditText etPaymentReceiverOperatorPhone;
    private EditText etPaymentAgentPhone;
    private EditText etPaymentAgentOperation;
    private EditText etSupplierPhone;
    private EditText etPaymentAgentMode;
    private EditText etDocumentRequisite;
    private EditText etUserRequisites;
    private EditText etCompanyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);
        initUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        collectData();
    }

    public static void setCustomerData(AssistPaymentData params) {
        if (lastName != null) {
            params.setLastname(lastName);
        } else {
            params.setLastname(DEFAULT_LAST_NAME);
        }
        if (firstName != null) {
            params.setFirstname(firstName);
        } else {
            params.setFirstname(DEFAULT_FIRST_NAME);
        }
        if (middleName != null) {
            params.setMiddlename(middleName);
        } else {
            params.setMiddlename(DEFAULT_MIDDLE_NAME);
        }
    }

    public static void setContactData(AssistPaymentData params) {
        if (email != null) {
            params.setEmail(email);
        } else {
            params.setEmail(DEFAULT_EMAIL);
        }
        if (address != null) {
            params.setAddress(address);
        } else {
            params.setAddress(DEFAULT_ADDRESS);
        }
        if (homePhone != null) {
            params.setHomePhone(homePhone);
        } else {
            params.setHomePhone(DEFAULT_PHONE);
        }
        if (workPhone != null) {
            params.setWorkPhone(workPhone);
        } else {
            params.setWorkPhone(DEFAULT_WORK_PHONE);
        }
        if (mobilePhone != null) {
            params.setMobilePhone(mobilePhone);
        } else {
            params.setMobilePhone(DEFAULT_MOBILE_PHONE);
        }
        if (fax != null) {
            params.setFax(fax);
        } else {
            params.setFax(DEFAULT_FAX);
        }
    }

    public static void setCustomerExData(AssistPaymentData params) {
        if (country != null) {
            params.setCountry(country);
        } else {
            params.setCountry(DEFAULT_COUNTRY);
        }
        if (state != null) {
            params.setState(state);
        } else {
            params.setState(DEFAULT_STATE);
        }
        if (city != null) {
            params.setCity(city);
        } else {
            params.setCity(DEFAULT_CITY);
        }
        if (zip != null) {
            params.setZip(zip);
        } else {
            params.setZip(DEFAULT_ZIP);
        }
    }

    public static void setCustomerOtherData(AssistPaymentData params) {
        if (taxpayerID != null) {
            params.setTaxpayerID(taxpayerID);
        } else {
            params.setTaxpayerID(DEFAULT_TAXPAYER_ID);
        }
        if (customerDocID != null) {
            params.setCustomerDocID(customerDocID);
        } else {
            params.setCustomerDocID(DEFAULT_CUSTOMER_DOC_ID);
        }
        if (paymentAddress != null) {
            params.setPaymentAddress(paymentAddress);
        } else {
            params.setPaymentAddress(DEFAULT_PAYMENT_ADDRESS);
        }
        if (paymentPlace != null) {
            params.setPaymentPlace(paymentPlace);
        } else {
            params.setPaymentPlace(DEFAULT_PAYMENT_PLACE);
        }
        if (cashier != null) {
            params.setCashier(cashier);
        } else {
            params.setCashier(DEFAULT_CASHIER);
        }
        if (cashierINN != null) {
            params.setCashierINN(cashierINN);
        } else {
            params.setCashierINN(DEFAULT_CASHIER_INN);
        }
        if (paymentTerminal != null) {
            params.setPaymentTerminal(paymentTerminal);
        } else {
            params.setPaymentTerminal(DEFAULT_PAYMENT_TERMINAL);
        }
        if (transferOperatorPhone != null) {
            params.setTransferOperatorPhone(transferOperatorPhone);
        } else {
            params.setTransferOperatorPhone(DEFAULT_TRANSFER_OPERATOR_PHONE);
        }
        if (transferOperatorName != null) {
            params.setTransferOperatorName(transferOperatorName);
        } else {
            params.setTransferOperatorName(DEFAULT_TRANSFER_OPERATOR_NAME);
        }
        if (transferOperatorAddress != null) {
            params.setTransferOperatorAddress(transferOperatorAddress);
        } else {
            params.setTransferOperatorAddress(DEFAULT_TRANSFER_OPERATOR_ADDRESS);
        }
        if (transferOperatorINN != null) {
            params.setTransferOperatorINN(transferOperatorINN);
        } else {
            params.setTransferOperatorINN(DEFAULT_TRANSFER_OPERATOR_INN);
        }
        if (paymentReceiverOperatorPhone != null) {
            params.setPaymentReceiverOperatorPhone(paymentReceiverOperatorPhone);
        } else {
            params.setPaymentReceiverOperatorPhone(DEFAULT_PAYMENT_RECEIVER_OPERATOR_PHONE);
        }
        if (paymentAgentPhone != null) {
            params.setPaymentAgentPhone(paymentAgentPhone);
        } else {
            params.setPaymentAgentPhone(DEFAULT_PAYMENT_AGENT_PHONE);
        }
        if (paymentAgentOperation != null) {
            params.setPaymentAgentOperation(paymentAgentOperation);
        } else {
            params.setPaymentAgentOperation(DEFAULT_PAYMENT_AGENT_OPERATION);
        }
        if (supplierPhone != null) {
            params.setSupplierPhone(supplierPhone);
        } else {
            params.setSupplierPhone(DEFAULT_SUPPLIER_PHONE);
        }
        if (paymentAgentMode != null) {
            params.setPaymentAgentMode(paymentAgentMode);
        } else {
            params.setPaymentAgentMode(DEFAULT_PAYMENT_AGENT_MODE);
        }
        if (documentRequisite != null) {
            params.setDocumentRequisite(documentRequisite);
        } else {
            params.setDocumentRequisite(DEFAULT_DOCUMENT_REQUISITE);
        }
        if (userRequisites != null) {
            params.setUserRequisites(userRequisites);
        } else {
            params.setUserRequisites(DEFAULT_USER_REQUISITES);
        }
        if (companyName != null) {
            params.setCompanyName(companyName);
        } else {
            params.setCompanyName(DEFAULT_COMPANY_NAME);
        }
    }

    private void collectData() {
        lastName = etLastName.getText().toString();
        firstName = etFirstName.getText().toString();
        middleName = etMiddleName.getText().toString();

        email = etEmail.getText().toString();
        address = etAddress.getText().toString();
        homePhone = etHomePhone.getText().toString();
        workPhone = etWorkPhone.getText().toString();
        mobilePhone = etMobilePhone.getText().toString();
        fax = etFax.getText().toString();

        country = etCountry.getText().toString();
        state = etState.getText().toString();
        city = etCity.getText().toString();
        zip = etZip.getText().toString();

        taxpayerID = etTaxpayerID.getText().toString();
        customerDocID = etCustomerDocID.getText().toString();
        paymentAddress = etPaymentAddress.getText().toString();
        paymentPlace = etPaymentPlace.getText().toString();
        cashier = etCashier.getText().toString();
        cashierINN = etCashierINN.getText().toString();
        paymentTerminal = etPaymentTerminal.getText().toString();
        transferOperatorPhone = etTransferOperatorPhone.getText().toString();
        transferOperatorName = etTransferOperatorName.getText().toString();
        transferOperatorAddress = etTransferOperatorAddress.getText().toString();
        transferOperatorINN = etTransferOperatorINN.getText().toString();
        paymentReceiverOperatorPhone = etPaymentReceiverOperatorPhone.getText().toString();
        paymentAgentPhone = etPaymentAgentPhone.getText().toString();
        paymentAgentOperation = etPaymentAgentOperation.getText().toString();
        supplierPhone = etSupplierPhone.getText().toString();
        paymentAgentMode = etPaymentAgentMode.getText().toString();
        documentRequisite = etDocumentRequisite.getText().toString();
        userRequisites = etUserRequisites.getText().toString();
        companyName = etCompanyName.getText().toString();
    }

    private void initUI() {
        etLastName = findViewById(R.id.etLastname);
        etFirstName = findViewById(R.id.etFirstname);
        etMiddleName = findViewById(R.id.etMiddlename);

        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);
        etHomePhone = findViewById(R.id.etHomePhone);
        etWorkPhone = findViewById(R.id.etWorkPhone);
        etMobilePhone = findViewById(R.id.etMobilePhone);
        etFax = findViewById(R.id.etFax);

        etCountry = findViewById(R.id.etCountry);
        etState = findViewById(R.id.etState);
        etCity = findViewById(R.id.etCity);
        etZip = findViewById(R.id.etZip);

        etTaxpayerID = findViewById(R.id.etTaxpayerID);
        etCustomerDocID = findViewById(R.id.etCustomerDocID);
        etPaymentAddress = findViewById(R.id.etPaymentAddress);
        etPaymentPlace = findViewById(R.id.etPaymentPlace);
        etCashier = findViewById(R.id.etCashier);
        etCashierINN = findViewById(R.id.etCashierINN);
        etPaymentTerminal = findViewById(R.id.etPaymentTerminal);
        etTransferOperatorPhone = findViewById(R.id.etTransferOperatorPhone);
        etTransferOperatorName = findViewById(R.id.etTransferOperatorName);
        etTransferOperatorAddress = findViewById(R.id.etTransferOperatorAddress);
        etTransferOperatorINN = findViewById(R.id.etTransferOperatorINN);
        etPaymentReceiverOperatorPhone = findViewById(R.id.etPaymentReceiverOperatorPhone);
        etPaymentAgentPhone = findViewById(R.id.etPaymentAgentPhone);
        etPaymentAgentOperation = findViewById(R.id.etPaymentAgentOperation);
        etSupplierPhone = findViewById(R.id.etSupplierPhone);
        etPaymentAgentMode = findViewById(R.id.etPaymentAgentMode);
        etDocumentRequisite = findViewById(R.id.etDocumentRequisite);
        etUserRequisites = findViewById(R.id.etUserRequisites);
        etCompanyName = findViewById(R.id.etCompanyName);

        if (lastName != null) {
            etLastName.setText(lastName);
        }

        if (firstName != null) {
            etFirstName.setText(firstName);
        }

        if (middleName != null) {
            etMiddleName.setText(middleName);
        }

        if (email != null) {
            etEmail.setText(email);
        }

        if (address != null) {
            etAddress.setText(address);
        }

        if (homePhone != null) {
            etHomePhone.setText(homePhone);
        }

        if (workPhone != null) {
            etWorkPhone.setText(workPhone);
        }

        if (mobilePhone != null) {
            etMobilePhone.setText(mobilePhone);
        }

        if (fax != null) {
            etFax.setText(fax);
        }

        if (country != null) {
            etCountry.setText(country);
        }

        if (state != null) {
            etState.setText(state);
        }

        if (city != null) {
            etCity.setText(city);
        }

        if (zip != null) {
            etZip.setText(zip);
        }

        if (taxpayerID != null) {
            etTaxpayerID.setText(taxpayerID);
        }

        if (customerDocID != null) {
            etCustomerDocID.setText(customerDocID);
        }

        if (paymentAddress != null) {
            etPaymentAddress.setText(paymentAddress);
        }

        if (paymentPlace != null) {
            etPaymentPlace.setText(paymentPlace);
        }

        if (cashier != null) {
            etCashier.setText(cashier);
        }

        if (cashierINN != null) {
            etCashierINN.setText(cashierINN);
        }

        if (paymentTerminal != null) {
            etPaymentTerminal.setText(paymentTerminal);
        }

        if (transferOperatorPhone != null) {
            etTransferOperatorPhone.setText(transferOperatorPhone);
        }

        if (transferOperatorName != null) {
            etTransferOperatorName.setText(transferOperatorName);
        }

        if (transferOperatorAddress != null) {
            etTransferOperatorAddress.setText(transferOperatorAddress);
        }

        if (transferOperatorINN != null) {
            etTransferOperatorINN.setText(transferOperatorINN);
        }

        if (paymentReceiverOperatorPhone != null) {
            etPaymentReceiverOperatorPhone.setText(paymentReceiverOperatorPhone);
        }

        if (paymentAgentPhone != null) {
            etPaymentAgentPhone.setText(paymentAgentPhone);
        }

        if (paymentAgentOperation != null) {
            etPaymentAgentOperation.setText(paymentAgentOperation);
        }

        if (supplierPhone != null) {
            etSupplierPhone.setText(supplierPhone);
        }

        if (paymentAgentMode != null) {
            etPaymentAgentMode.setText(paymentAgentMode);
        }

        if (documentRequisite != null) {
            etDocumentRequisite.setText(documentRequisite);
        }

        if (userRequisites != null) {
            etUserRequisites.setText(userRequisites);
        }

        if (companyName != null) {
            etCompanyName.setText(companyName);
        }
    }
}