package ru.assisttech.assistsdk;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.Date;

import ru.assisttech.sdk.AssistResult;
import ru.assisttech.sdk.storage.AssistTransaction;
import ru.assisttech.sdk.storage.AssistTransactionFilter;
import ru.assisttech.sdk.storage.AssistTransactionStorage;
import ru.assisttech.sdk.storage.AssistTransactionStorageImpl;
import ru.assisttech.sdk.storage.AssistTransactionsLoader;

public class TransactionsActivity extends UpButtonActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = "TransactionsActivity";

    private static final int LOADER_ID = 0;
    public static final String TRANS_ID = "id";

    private AssistTransactionStorage storage;
    private AssistTransactionFilter deleteFilter;

    private TransactionSimpleAdapter transactionsAdapter;

	private String[] from = new String[] {AssistTransactionStorageImpl.COLUMN_ORDER_DATE_DEVICE,
                                          AssistTransactionStorageImpl.COLUMN_ORDER_NUMBER,
                                          AssistTransactionStorageImpl.COLUMN_ORDER_AMOUNT,
                                          AssistTransactionStorageImpl.COLUMN_ORDER_CURRENCY};

	private int[] to = new int[] {R.id.tvTransItemOrderDate,
                                  R.id.tvTransItemOrderNumber,
                                  R.id.tvTransItemOrderAmount,
                                  R.id.tvTransItemOrderCurrency};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transactions);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ApplicationConfiguration cfg = ApplicationConfiguration.getInstance();
        storage = cfg.getPaymentEngine().transactionStorage();

        getLoaderManager().initLoader(LOADER_ID, null, this);

        transactionsAdapter = new TransactionSimpleAdapter(this, R.layout.trans_item, null, from, to);
        ListView lvTransactions = (ListView) findViewById(R.id.lvTransactions);
        lvTransactions.setAdapter(transactionsAdapter);
		lvTransactions.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("TransactionsActivity", "onClick(). position: " + position);
                Intent intent = new Intent(TransactionsActivity.this, TransDetailsActivity.class);
                Bundle extras = new Bundle();
                extras.putLong(TRANS_ID, id);
                intent.putExtras(extras);
                startActivity(intent);
            }
        });
	}

	@Override
	public void onResume() {
        super.onResume();
		updateList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.transactions_activity_menu, menu);
        SearchView searchView = (SearchView)(menu.findItem(R.id.action_search).getActionView());
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                updateListWithCertainOrderNumber(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                updateListWithCertainOrderNumber(newText);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Date date;
		long msDay = 24*60*60*1000;

		switch (item.getItemId()) {
			case R.id.miTransFilter:
                Intent intent = new Intent(this, TransFilterActivity.class);
                startActivity(intent);
				break;
			case R.id.miDelete2weeks:
				deleteFilter = new AssistTransactionFilter();
				date = new Date();
				date.setTime(date.getTime() - msDay * 14L);
				deleteFilter.setDateEnd(date);
				showAlertDialog();
				break;
			case R.id.miDelete1month:
				deleteFilter = new AssistTransactionFilter();
				date = new Date();
				date.setTime(date.getTime() - msDay * 30L);
				deleteFilter.setDateEnd(date);
				showAlertDialog();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

    /*
     * LoaderManager.LoaderCallbacks<>
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader()");
        return new AssistTransactionsLoader(storage, this, args);
    }

    /*
     * LoaderManager.LoaderCallbacks<>
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset()");
        transactionsAdapter.swapCursor(null);
        transactionsAdapter.notifyDataSetChanged();
    }

    /*
     * LoaderManager.LoaderCallbacks<>
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished(). Cursor size: " + data.getCount());
        transactionsAdapter.swapCursor(data);
        transactionsAdapter.notifyDataSetChanged();
    }

	private void updateList() {
        Log.d(TAG, "updateList");
        if (getLoaderManager().getLoader(LOADER_ID).isStarted()) {
            getLoaderManager().getLoader(LOADER_ID).forceLoad();
        } else {
            getLoaderManager().initLoader(LOADER_ID, null, this);
        }
	}

    private void updateListWithCertainOrderNumber(String orderNumber) {
        Bundle bundle = null;
        if (!TextUtils.isEmpty(orderNumber)) {
            bundle = new Bundle();
            bundle.putString(AssistTransactionsLoader.ARG_STRING_ORDER_NUMBER, orderNumber);
        }
        getLoaderManager().restartLoader(LOADER_ID, bundle, TransactionsActivity.this);
    }

	private class TransactionSimpleAdapter extends SimpleCursorAdapter {
		TransactionSimpleAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
			super(context, layout, c, from, to, 0);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			Cursor c = getCursor();
			c.moveToPosition(position);
            AssistTransaction t = storage.transactionFromCursor(c);
            AssistResult.OrderState os = t.getResult().getOrderState();
            Log.d(TAG, "State: " + os.toString());
			switch (os) {
				case APPROVED:
                    view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.transaction_approved));
					break;
				case IN_PROCESS:
                    view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.transaction_in_process));
					break;
				case UNKNOWN:
                    view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.transaction_unknown));
					break;
                case CANCEL_ERROR:
                    view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.transaction_cancel_error));
                    break;
				default:
                    view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.transaction_napproved));
					break;
			}
            TextView tvAmount = (TextView)view.findViewById(R.id.tvTransItemOrderAmount);
            tvAmount.setText(AssistTransaction.formatAmount(t.getOrderAmount()));

            ImageView ivFilterIcon = (ImageView)view.findViewById(R.id.ivFilterIcon);
            if (storage.getFilter().match(t)) {
                ivFilterIcon.setVisibility(View.VISIBLE);
            } else {
                ivFilterIcon.setVisibility(View.INVISIBLE);
            }

			return view;
		}
	}

	private void showAlertDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Удаление транзакций");
		builder.setMessage("Действие не может быть отменено!");
		builder.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                storage.deleteTransactions(deleteFilter);
                updateList();
            }
        });
		builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
		builder.show();
	}
}
