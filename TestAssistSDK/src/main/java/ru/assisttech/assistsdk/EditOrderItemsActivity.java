package ru.assisttech.assistsdk;

import android.os.Bundle;
import android.widget.EditText;

public class EditOrderItemsActivity extends UpButtonActivity {

    static String itemsJSON;

    private EditText etItems;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_order_items);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        initUI();
	}

	@Override
	protected void onPause() {
		super.onPause();
        collectData();
	}

    private void initUI() {
        etItems = findViewById(R.id.etItems);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            itemsJSON = extras.getString("items");
        }

        if (itemsJSON != null) {
            etItems.setText(itemsJSON);
        }
    }

    private void collectData() {
        itemsJSON = etItems.getText().toString();
    }
}