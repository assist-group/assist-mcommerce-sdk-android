package ru.assisttech.assistsdk;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import ru.assisttech.sdk.engine.AssistPayEngine;
import ru.assisttech.sdk.engine.PayEngineListener;
import ru.assisttech.sdk.storage.AssistTransaction;
import ru.assisttech.sdk.storage.AssistTransactionStorage;

public class TransDetailsActivity extends UpButtonActivity implements PayEngineListener {

    private TextView odv;
    private TextView onv;
    private TextView ocv;
    private TextView oav;
    private TextView osv;
    private TextView oiv;

    private AssistPayEngine engine;
    private AssistTransaction tr;

    private ProgressDialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trans_details);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ApplicationConfiguration cfg = ApplicationConfiguration.getInstance();
        engine = cfg.getPaymentEngine();
        AssistTransactionStorage storage = engine.transactionStorage();
		
		odv = (TextView) findViewById(R.id.tvOrderDate);
		onv = (TextView) findViewById(R.id.tvOrderNumber);
		ocv = (TextView) findViewById(R.id.tvOrderComment);
		oav = (TextView) findViewById(R.id.tvOrderAmount);
		osv = (TextView) findViewById(R.id.tvOrderStatus);
		oiv = (TextView) findViewById(R.id.tvOrderExtraInfo);

		Bundle extras = getIntent().getExtras();
        if (extras != null) {
            tr = storage.getTransaction(extras.getLong(TransactionsActivity.TRANS_ID));
            updateUI(tr);
        }

		findViewById(R.id.btnUpdateStatus).setOnClickListener(v -> {
            showProgress();
            engine.setEngineListener(TransDetailsActivity.this);
            engine.getOrderResult(TransDetailsActivity.this, tr.getId());
        });
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

    /**
     * {@link PayEngineListener}
     */
    @Override
    public void onFinished(Activity activity, AssistTransaction assistTransaction) {
        updateUI(assistTransaction);
        hideProgress();
    }

    /**
     * {@link PayEngineListener}
     */
    @Override
    public void onCanceled(Activity activity, AssistTransaction assistTransaction) {
    }

    /**
     * {@link PayEngineListener}
     */
    @Override
    public void onFailure(Activity activity, String info) {
        hideProgress();
        Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
    }

    /**
     * {@link PayEngineListener}
     */
    @Override
    public void onNetworkError(Activity activity, String message) {
        hideProgress();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateUI(AssistTransaction tr) {
        odv.setText(tr.getOrderDateDevice());
        onv.setText(tr.getOrderNumber());
        ocv.setText(tr.getOrderComment());
        oav.setText(tr.getOrderAmount() + " " + tr.getOrderCurrency());
        osv.setText(tr.getResult().getOrderState().toText());
        String extraInfo = tr.getResult().getExtra();
        if (TextUtils.isEmpty(extraInfo)) {
            oiv.setText("-");
        } else {
            oiv.setText(extraInfo);
        }
    }

    private void showProgress() {
        // Construct a progress dialog to prevent user from actions until connection is finished.
        if (dialog == null) {
            dialog = new ProgressDialog(this);
            dialog.setMessage(getString(R.string.please_wait));
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnKeyListener((dialog, keyCode, event) -> true);
            dialog.show();
        }
    }

    private void hideProgress() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }
}