package ru.assisttech.assistsdk;

import java.util.regex.Pattern;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

class AmountWatcher implements TextWatcher {
    private final Pattern sPatternDot = Pattern.compile("\\-?\\d+(\\.\\d{0,2})?");
    private final Pattern sPatternComma = Pattern.compile("\\-?\\d+(,\\d{0,2})?");

    private CharSequence mText;
    private EditText mEdit;

    AmountWatcher(EditText et) {
        mEdit = et;
    }

    private boolean isValid(CharSequence s) {
        return sPatternDot.matcher(s).matches() || sPatternComma.matcher(s).matches();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (s.length() != 0) {
            mText = isValid(s) ? s.toString() : "";
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() != 0 && !isValid(s)) {
            mEdit.setText(mText);
            mEdit.setSelection(mEdit.getText().length(), mEdit.getText().length());
        }
        mText = null;
    }
}