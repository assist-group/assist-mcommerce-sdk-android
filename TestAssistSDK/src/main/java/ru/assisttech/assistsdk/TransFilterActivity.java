package ru.assisttech.assistsdk;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ru.assisttech.sdk.AssistPaymentData;
import ru.assisttech.sdk.AssistResult;
import ru.assisttech.sdk.storage.AssistTransactionFilter;
import ru.assisttech.sdk.storage.AssistTransactionStorage;

public class TransFilterActivity extends UpButtonActivity {

    private final SimpleDateFormat formatDate = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
    private final SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat formatDateTime = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());

    private AssistTransactionFilter filter;
    private AssistTransactionStorage storage;

    private EditText etAmountMin;
    private EditText etAmountMax;
    private Spinner spStatus;
    private Spinner spCurrency;
    private EditText etFilterDateBegin;
    private EditText etFilterTimeBegin;
    private EditText etFilterDateEnd;
    private EditText etFilterTimeEnd;

    private ArrayList<String> orderStatuses;
    private ArrayList<String> currencies;

    private String errorMsg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        ApplicationConfiguration cfg = ApplicationConfiguration.getInstance();
        storage = cfg.getPaymentEngine().transactionStorage();
        filter = storage.getFilter();

        initUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filter_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_reset_filter:
                filter.reset();
                initUI();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean checkValuesAndApply() {

        Date dtBegin = getDate(etFilterDateBegin, etFilterTimeBegin);
        Date dtEnd = getDate(etFilterDateEnd, etFilterTimeEnd);
        if (dtBegin != null && dtEnd != null) {
            if (!dtBegin.before(dtEnd)) {
                errorMsg = "Dates are wrong";
                return false;
            }
        }

        filter.setDateBegin(dtBegin);
        filter.setDateEnd(dtEnd);

        final int NOT_SPECIFIED = -1;
        int min = NOT_SPECIFIED;
        int max = NOT_SPECIFIED;

        if (etAmountMin.getText().length() > 0) {
            min = Integer.parseInt(etAmountMin.getText().toString());
        }
        if (etAmountMax.getText().length() > 0) {
            max = Integer.parseInt(etAmountMax.getText().toString());
        }

        if (min != NOT_SPECIFIED && max != NOT_SPECIFIED) {
            if (min > max) {
                errorMsg = "Amounts are wrong";
                return false;
            }
        }

        if (min != NOT_SPECIFIED) {
            filter.setAmountMin(etAmountMin.getText().toString());
        } else {
            filter.setAmountMin(null);
        }
        if (max != NOT_SPECIFIED) {
            filter.setAmountMax(etAmountMax.getText().toString());
        } else {
            filter.setAmountMax(null);
        }

        int position = spStatus.getSelectedItemPosition();
        if (position == 0) {
            filter.setOrderState(null);
        } else {
            AssistResult.OrderState os = AssistResult.OrderState.fromString(orderStatuses.get(position));
            filter.setOrderState(os);
        }

        position = spCurrency.getSelectedItemPosition();
        if (position == 0) {
            filter.setCurrency(null);
        } else {
            filter.setCurrency(AssistPaymentData.Currency.valueOf(currencies.get(position)));
        }

        storage.setFilter(filter);
        return true;
    }

    private Date getDate(EditText day, EditText time) {
        String d = day.getText().toString();
        String t = time.getText().toString();

        if (TextUtils.isEmpty(d) && TextUtils.isEmpty(t))
            return null;
        try {
            if (TextUtils.isEmpty(d)) {
                return formatTime.parse(t);
            } else if (TextUtils.isEmpty(t)) {
                return formatDate.parse(d);
            } else {
                return formatDateTime.parse(d + " " + t);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Calendar getDateCalendar(EditText et) {
        Calendar cl = Calendar.getInstance();
        String date = et.getText().toString();
        if (!TextUtils.isEmpty(date)) {
            try {
                Date dt = formatDate.parse(date);
                cl.setTime(dt);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return cl;
    }

    private Calendar getTimeCalendar(EditText et) {
        Calendar cl = Calendar.getInstance();
        String time = et.getText().toString();
        if (!TextUtils.isEmpty(time)) {
            try {
                Date dt = formatTime.parse(time);
                cl.setTime(dt);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return cl;
    }

    private void initUI() {
        etFilterDateBegin = (EditText) findViewById(R.id.etFilterDateBegin);
        etFilterTimeBegin = (EditText) findViewById(R.id.etFilterTimeBegin);
        etFilterDateEnd = (EditText) findViewById(R.id.etFilterDateEnd);
        etFilterTimeEnd = (EditText) findViewById(R.id.etFilterTimeEnd);
        spStatus = (Spinner) findViewById(R.id.spFilterStatus);
        etAmountMin = (EditText) findViewById(R.id.etFilterAmountMin);
        etAmountMax = (EditText) findViewById(R.id.etFilterAmountMax);
        spCurrency = (Spinner) findViewById(R.id.spFilterCurrency);
        Button btApply = (Button) findViewById(R.id.btApply);
        btApply.setOnClickListener(v -> {
            if (checkValuesAndApply()) {
                finish();
            } else {
                showErrorDlg(errorMsg);
            }
        });

        if (filter.getDateBegin() != null) {
            etFilterDateBegin.setText(formatDate.format(filter.getDateBegin()));
            etFilterTimeBegin.setText(formatTime.format(filter.getDateBegin()));
        } else {
            etFilterDateBegin.setText("");
            etFilterTimeBegin.setText("");
        }
        if (filter.getDateEnd() != null) {
            etFilterDateEnd.setText(formatDate.format(filter.getDateEnd()));
            etFilterTimeEnd.setText(formatTime.format(filter.getDateEnd()));
        } else {
            etFilterDateEnd.setText("");
            etFilterTimeEnd.setText("");
        }
        etFilterDateBegin.setOnClickListener(new DateClickListener(etFilterDateBegin, etFilterTimeBegin));
        etFilterTimeBegin.setOnClickListener(new TimeClickListener(etFilterTimeBegin, etFilterDateBegin));

        etFilterDateEnd.setOnClickListener(new DateClickListener(etFilterDateEnd, etFilterTimeEnd));
        etFilterTimeEnd.setOnClickListener(new TimeClickListener(etFilterTimeEnd, etFilterDateEnd));

        orderStatuses = new ArrayList<>();
        orderStatuses.add("Все");
        AssistResult.OrderState[] os = AssistResult.OrderState.values();
        for (AssistResult.OrderState o : os) {
            orderStatuses.add(o.toText());
        }
        ArrayAdapter<String> osa = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                orderStatuses
        );
        osa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatus.setAdapter(osa);
        AssistResult.OrderState state = filter.getOrderState();
        int index = 0;
        if (state != null) {
            index = orderStatuses.indexOf(state.toText());
        }
        spStatus.setSelection(index);

        if (filter.getAmountMin() != null) {
            etAmountMin.setText(filter.getAmountMin());
        } else {
            etAmountMin.setText("");
        }
        if (filter.getAmountMax() != null) {
            etAmountMax.setText(filter.getAmountMax());
        } else {
            etAmountMax.setText("");
        }
        etAmountMin.addTextChangedListener(new AmountWatcher(etAmountMin));
        etAmountMax.addTextChangedListener(new AmountWatcher(etAmountMax));

        currencies = new ArrayList<>();
        currencies.add("Все");
        AssistPaymentData.Currency[] curr = AssistPaymentData.Currency.values();
        for (AssistPaymentData.Currency aCurr : curr) {
            currencies.add(aCurr.toString());
        }
        ArrayAdapter<String> adCur = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                currencies
        );
        adCur.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCurrency.setAdapter(adCur);
        spCurrency.setOnItemSelectedListener(new CurrencySelectedListener(spCurrency));

        AssistPaymentData.Currency cur = filter.getCurrency();
        index = 0;
        if (cur != null) {
            index = currencies.indexOf(cur.toString());
        }
        spCurrency.setSelection(index);

        findViewById(R.id.btnClearBeginDate).setOnClickListener(v -> {
            etFilterDateBegin.setText("");
            etFilterTimeBegin.setText("");
        });

        findViewById(R.id.btnClearEndDate).setOnClickListener(v -> {
            etFilterDateEnd.setText("");
            etFilterTimeEnd.setText("");
        });
    }

    private class CurrencySelectedListener implements OnItemSelectedListener {
        private Spinner spBinded;

        public CurrencySelectedListener(Spinner binded) {
            spBinded = binded;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (spBinded.getSelectedItemPosition() != position) {
                spBinded.setSelection(position);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private class DateClickListener implements OnClickListener {
        private final EditText etDate;
        private final EditText etTime;

        public DateClickListener(EditText etDate, EditText etTime) {
            this.etDate = etDate;
            this.etTime = etTime;
        }

        @Override
        public void onClick(View v) {
            Calendar cl = getDateCalendar(etDate);
            DatePickerDialog dlg = new DatePickerDialog(
                    TransFilterActivity.this,
                    new OnFilterDateSetListener(etDate, etTime),
                    cl.get(Calendar.YEAR),
                    cl.get(Calendar.MONTH),
                    cl.get(Calendar.DAY_OF_MONTH)
            );
            dlg.show();
        }
    }

    private class OnFilterDateSetListener implements DatePickerDialog.OnDateSetListener {
        private final EditText etDate;
        private final EditText etTime;

        public OnFilterDateSetListener(EditText etDate, EditText etTime) {
            this.etDate = etDate;
            this.etTime = etTime;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, monthOfYear);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            Date dt = cal.getTime();
            etDate.setText(formatDate.format(dt));
            if (etTime.getText().length() == 0)
                etTime.setText("00:00");
        }
    }

    private class TimeClickListener implements OnClickListener {
        private final EditText etTime;
        private final EditText etDate;

        public TimeClickListener(EditText etTime, EditText etDate) {
            this.etTime = etTime;
            this.etDate = etDate;
        }

        @Override
        public void onClick(View v) {
            Calendar c = getTimeCalendar(etTime);
            TimePickerDialog dlg = new TimePickerDialog(
                TransFilterActivity.this,
                new OnFilterTimeSetListener(etTime, etDate),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
            );
            dlg.show();
        }
    }

    private class OnFilterTimeSetListener implements TimePickerDialog.OnTimeSetListener {
        private final EditText etTime;
        private final EditText etDate;

        public OnFilterTimeSetListener(EditText etTime, EditText etDate) {
            this.etTime = etTime;
            this.etDate = etDate;
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);

            Date dt = c.getTime();
            etTime.setText(formatTime.format(dt));
            if (etDate.getText().length() == 0) {
                etDate.setText(formatDate.format(dt));
            }
        }
    }

    private void showErrorDlg(String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Error");
        builder.setMessage(text);
        builder.show();
    }
}