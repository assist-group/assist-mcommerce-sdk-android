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
    }

    private void initUI() {
        etLastName = (EditText) findViewById(R.id.etLastname);
        etFirstName = (EditText) findViewById(R.id.etFirstname);
        etMiddleName = (EditText) findViewById(R.id.etMiddlename);

        etEmail = (EditText) findViewById(R.id.etEmail);
        etAddress = (EditText) findViewById(R.id.etAddress);
        etHomePhone = (EditText) findViewById(R.id.etHomePhone);
        etWorkPhone = (EditText) findViewById(R.id.etWorkPhone);
        etMobilePhone = (EditText) findViewById(R.id.etMobilePhone);
        etFax = (EditText) findViewById(R.id.etFax);

        etCountry = (EditText) findViewById(R.id.etCountry);
        etState = (EditText) findViewById(R.id.etState);
        etCity = (EditText) findViewById(R.id.etCity);
        etZip = (EditText) findViewById(R.id.etZip);

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
    }
}
