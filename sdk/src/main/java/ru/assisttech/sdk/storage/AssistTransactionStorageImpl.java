package ru.assisttech.sdk.storage;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import ru.assisttech.sdk.AssistPaymentData.Currency;
import ru.assisttech.sdk.AssistResult;

public class AssistTransactionStorageImpl implements AssistTransactionStorage {

    private final String TAG = "TransactionStorage";

    private static final String DB_NAME = "transactionsdb";
    private static final int DB_VERSION = 13;
    private static final String DB_TABLE_TRANSACTIONS = "trans";

    public static final String COLUMN_ORDER_MID = "omid";
    public static final String COLUMN_ORDER_DATE_UTC = "odate";
    public static final String COLUMN_ORDER_DATE_DEVICE = "ddate";
    public static final String COLUMN_ORDER_DATE_DEVICE_MILLIS = "otime_millis";
    public static final String COLUMN_ORDER_NUMBER = "onum";
    public static final String COLUMN_ORDER_COMMENT = "ocom";
    public static final String COLUMN_ORDER_AMOUNT = "oamt";
    public static final String COLUMN_ORDER_CURRENCY = "ocur";
    public static final String COLUMN_PAYMENT_METHOD = "pmtd";
    public static final String COLUMN_ORDER_STATE = "ostat";
    public static final String COLUMN_ORDER_APPROVAL_CODE = "oacode";
    public static final String COLUMN_ORDER_EXTRA_INFO = "oextra";
    public static final String COLUMN_BILL_NUMBER = "bnum";
    public static final String COLUMN_USER_SIGNATURE_REQUIREMENT = "req_usign";
    public static final String COLUMN_USER_SIGNATURE = "usign";
    public static final String COLUMN_ORDER_ITEMS_JSON = "oitems";
    public static final String COLUMN_MEANNUMBER = "meann";
    public static final String COLUMN_MEANTYPENAME = "meant";
    public static final String COLUMN_CARDEXPIRATIONDATE = "cexp";
    public static final String COLUMN_CARDHOLDER = "chold";

    private final Context mCtx;

    private String dbName;
    private DBHelper dbHelper;

    private AssistTransactionFilter transactionFilter;

    private static final String DB_CREATE =
            "create table " + DB_TABLE_TRANSACTIONS + "(" +
                    BaseColumns._ID + " integer primary key autoincrement, " +
                    COLUMN_ORDER_DATE_DEVICE_MILLIS + " integer not null, " +
                    COLUMN_ORDER_MID + " text not null, " +
                    COLUMN_ORDER_DATE_UTC + " text not null, " +
                    COLUMN_ORDER_DATE_DEVICE + " text not null, " +
                    COLUMN_ORDER_NUMBER + " text not null, " +
                    COLUMN_ORDER_COMMENT + " text, " +
                    COLUMN_ORDER_AMOUNT + " numeric not null, " +
                    COLUMN_ORDER_CURRENCY + " text not null, " +
                    COLUMN_ORDER_ITEMS_JSON + " text, " +
                    COLUMN_PAYMENT_METHOD + " text not null, " +
                    COLUMN_ORDER_STATE + " text not null, " +
                    COLUMN_ORDER_APPROVAL_CODE + " text, " +
                    COLUMN_ORDER_EXTRA_INFO + " text, " +
                    COLUMN_BILL_NUMBER + " text, " +
                    COLUMN_MEANNUMBER + " text, " +
                    COLUMN_MEANTYPENAME + " text, " +
                    COLUMN_CARDHOLDER + " text, " +
                    COLUMN_CARDEXPIRATIONDATE + " text, " +
                    COLUMN_USER_SIGNATURE_REQUIREMENT + " integer not null, " +
                    COLUMN_USER_SIGNATURE + " blob " +
                    ");";

    private static final String DB_DROP_TABLE = "drop table if exists " + DB_TABLE_TRANSACTIONS + ";";

    public AssistTransactionStorageImpl(Context ctx, String dbNameSuffix) {
        mCtx = ctx;
        if (!TextUtils.isEmpty(dbNameSuffix)) {
            dbName = DB_NAME + "." + dbNameSuffix;
        }
        transactionFilter = new AssistTransactionFilter();
    }

    @Override
    public long add(AssistTransaction t) {

        /* Order date UTC */
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
        t.setOrderDateUTC(sdf.format(System.currentTimeMillis()));

        /* Order local date */
        Calendar now = Calendar.getInstance();
        sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.US);
        t.setOrderDateDevice(sdf.format(now.getTime()));

        DBHelper dbHelper = new DBHelper(mCtx, dbName, null, DB_VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ORDER_DATE_DEVICE_MILLIS, now.getTimeInMillis());
        cv.put(COLUMN_ORDER_MID, t.getMerchantID());
        cv.put(COLUMN_ORDER_DATE_UTC, t.getOrderDateUTC());
        cv.put(COLUMN_ORDER_DATE_DEVICE, t.getOrderDateDevice());

        cv.put(COLUMN_ORDER_NUMBER, t.getOrderNumber());
        cv.put(COLUMN_ORDER_COMMENT, t.getOrderComment());
        cv.put(COLUMN_ORDER_AMOUNT, t.getOrderAmount());
        cv.put(COLUMN_ORDER_CURRENCY, t.getOrderCurrency().toString());
        if (t.hasOrderItems()) {
            try {
                cv.put(COLUMN_ORDER_ITEMS_JSON, AssistOrderUtils.toJsonString(t.getOrderItems()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cv.put(COLUMN_PAYMENT_METHOD, t.getPaymentMethod().toString());
        cv.put(COLUMN_USER_SIGNATURE, t.getUserSignature());

        cv.put(COLUMN_ORDER_STATE, t.getResult().getOrderState().toString());
        cv.put(COLUMN_ORDER_APPROVAL_CODE, t.getResult().getApprovalCode());
        cv.put(COLUMN_ORDER_EXTRA_INFO, t.getResult().getExtra());
        cv.put(COLUMN_BILL_NUMBER, t.getResult().getBillNumber());
        cv.put(COLUMN_USER_SIGNATURE_REQUIREMENT, t.isRequireUserSignature() ? 1 : 0);
        cv.put(COLUMN_CARDHOLDER, t.getResult().getCardholder());
        cv.put(COLUMN_CARDEXPIRATIONDATE, t.getResult().getCardExpirationDate());
        cv.put(COLUMN_MEANNUMBER, t.getResult().getMeanNumber());
        cv.put(COLUMN_MEANTYPENAME, t.getResult().getMeantypeName());

        long id = db.insert(DB_TABLE_TRANSACTIONS, null, cv);
        dbHelper.close();
        if (id != ERROR) {
            t.setId(id);
        }
        return id;
    }

    @Override
    public AssistTransaction getTransaction(long id) {

        DBHelper dbHelper = new DBHelper(mCtx, dbName, null, DB_VERSION);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DB_TABLE_TRANSACTIONS, null, BaseColumns._ID + " = " + id, null, null, null, null);
        AssistTransaction t = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                t = transactionFromCursor(cursor);
            }
            cursor.close();
        }
        dbHelper.close();
        return t;
    }

    @Override
    public void updateTransactionSignature(long id, byte[] signature) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USER_SIGNATURE, signature);
        updateTransactionsTable(id, cv);
    }

    @Override
    public void updateTransactionOrderNumber(long id, String on) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ORDER_NUMBER, on);
        updateTransactionsTable(id, cv);
    }

    @Override
    public void updateTransactionResult(long id, AssistResult result) {
        ContentValues cv = new ContentValues();

        if (result != null) {
            cv.put(COLUMN_ORDER_STATE, result.getOrderState().toString());
            cv.put(COLUMN_ORDER_APPROVAL_CODE, result.getApprovalCode());
            cv.put(COLUMN_ORDER_EXTRA_INFO, result.getExtra());
            cv.put(COLUMN_BILL_NUMBER, result.getBillNumber());
            cv.put(COLUMN_MEANNUMBER, result.getMeanNumber());
            cv.put(COLUMN_MEANTYPENAME, result.getMeantypeName());
            cv.put(COLUMN_CARDHOLDER, result.getCardholder());
            cv.put(COLUMN_CARDEXPIRATIONDATE, result.getCardExpirationDate());
        }
        updateTransactionsTable(id, cv);
    }

    @Override
    public Cursor getData() {
        return getData(transactionFilter, null);
    }

    @Override
    public Cursor getData(String orderNumber) {
        return getData(transactionFilter, orderNumber);
    }

    @Override
    public Cursor getDataWithNoFiltration(String orderNumber) {
        return getData(new AssistTransactionFilter(), orderNumber);
    }

    @Override
    public int deleteTransactions(AssistTransactionFilter filter) {
        DBHelper dbHelper = new DBHelper(mCtx, dbName, null, DB_VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String sel = null;
        ArrayList<String> args = new ArrayList<>();

        if (filter.getDateBegin() != null) {
            sel = appendToSelection(sel, COLUMN_ORDER_DATE_DEVICE_MILLIS + " >= ?");
            args.add(String.valueOf(filter.getDateBegin().getTime()));
        }

        if (filter.getDateEnd() != null) {
            sel = appendToSelection(sel, COLUMN_ORDER_DATE_DEVICE_MILLIS + " <= ?");
            args.add(String.valueOf(filter.getDateEnd().getTime()));
        }

        if (filter.getOrderState() != null) {
            sel = appendToSelection(sel, COLUMN_ORDER_STATE + " = ?");
            args.add("\'" + filter.getOrderState().toString() + "\'");
        }

        if (filter.getAmountMin() != null) {
            sel = appendToSelection(sel, COLUMN_ORDER_AMOUNT + " >= ?");
            args.add(String.valueOf(filter.getAmountMin()));
        }

        if (filter.getAmountMax() != null) {
            sel = appendToSelection(sel, COLUMN_ORDER_AMOUNT + " <= ?");
            args.add(String.valueOf(filter.getAmountMax()));
        }

        if (filter.getCurrency() != null) {
            sel = appendToSelection(sel, COLUMN_ORDER_CURRENCY + " = ?");
            args.add("\'" + filter.getCurrency().toString() + "\'");
        }

        String[] whereArgs = null;
        if (!args.isEmpty()) {
            whereArgs = args.toArray(new String[args.size()]);
        }

        int res = db.delete(DB_TABLE_TRANSACTIONS, sel, whereArgs);
        db.close();
        dbHelper.close();
        return res;
    }

    @Override
    public int deleteTransaction(long id) {
        DBHelper dbHelper = new DBHelper(mCtx, dbName, null, DB_VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String sel = BaseColumns._ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        int res = db.delete(DB_TABLE_TRANSACTIONS, sel, whereArgs);
        db.close();
        dbHelper.close();
        return res;
    }

    @Override
    public AssistTransactionFilter getFilter() {
        return transactionFilter;
    }

    @Override
    public void setFilter(AssistTransactionFilter filter) {
        transactionFilter = filter;
    }

    @Override
    public void resetFilter() {
        transactionFilter = new AssistTransactionFilter();
    }

    private void updateTransactionsTable(long id, ContentValues cv) {
        if (id != ERROR) {
            DBHelper dbHelper = new DBHelper(mCtx, dbName, null, DB_VERSION);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.update(DB_TABLE_TRANSACTIONS, cv, BaseColumns._ID + " = ?", new String[]{String.valueOf(id)});
            dbHelper.close();
        }
    }

    private String appendToSelection(@Nullable String selection, String dataToAppend) {
        if (dataToAppend != null) {
            if (TextUtils.isEmpty(selection)) {
                return dataToAppend;
            } else {
                return selection + " and " + dataToAppend;
            }
        }
        return selection;
    }

    @SuppressLint("SimpleDateFormat")
    private Cursor getData(AssistTransactionFilter filter, String orderNumber) {
        dbHelper = new DBHelper(mCtx, dbName, null, DB_VERSION);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sel = null;

        if (filter.getDateBegin() != null) {
            sel = appendToSelection(sel, COLUMN_ORDER_DATE_DEVICE_MILLIS + " >= " + filter.getDateBegin().getTime());
        }

        if (filter.getDateEnd() != null) {
            sel = appendToSelection(sel, COLUMN_ORDER_DATE_DEVICE_MILLIS + " <= " + filter.getDateEnd().getTime());
        }

        if (filter.getOrderState() != null) {
            sel = appendToSelection(sel, COLUMN_ORDER_STATE + " = \'" + filter.getOrderState().toString() + "\'");
        }

        if (filter.getAmountMin() != null) {
            sel = appendToSelection(sel, COLUMN_ORDER_AMOUNT + " >= " + filter.getAmountMin());
        }

        if (filter.getAmountMax() != null) {
            sel = appendToSelection(sel, COLUMN_ORDER_AMOUNT + " <= " + filter.getAmountMax());
        }

        if (filter.getCurrency() != null) {
            sel = appendToSelection(sel, COLUMN_ORDER_CURRENCY + " = \'" + filter.getCurrency().toString() + "\'");
        }

        if (!TextUtils.isEmpty(orderNumber)) {
            sel = appendToSelection(sel, COLUMN_ORDER_NUMBER + " LIKE " + "\'%" + orderNumber + "%\'");
        }

        String orderBy = COLUMN_ORDER_DATE_DEVICE_MILLIS + " desc;";
        return db.query(DB_TABLE_TRANSACTIONS, null, sel, null, null, null, orderBy);
    }

    public AssistTransaction transactionFromCursor(Cursor cursor) {
        if (cursor.isAfterLast() || cursor.isBeforeFirst())
            return null;

        AssistResult result = new AssistResult();
        result.setOrderState(cursor.getString(cursor.getColumnIndex(COLUMN_ORDER_STATE)));
        result.setApprovalCode(cursor.getString(cursor.getColumnIndex(COLUMN_ORDER_APPROVAL_CODE)));
        result.setExtra(cursor.getString(cursor.getColumnIndex(COLUMN_ORDER_EXTRA_INFO)));
        result.setBillNumber(cursor.getString(cursor.getColumnIndex(COLUMN_BILL_NUMBER)));
        result.setCardExpirationDate(cursor.getString(cursor.getColumnIndex(COLUMN_CARDEXPIRATIONDATE)));
        result.setCardholder(cursor.getString(cursor.getColumnIndex(COLUMN_CARDHOLDER)));
        result.setMeantypeName(cursor.getString(cursor.getColumnIndex(COLUMN_MEANTYPENAME)));
        result.setMeanNumber(cursor.getString(cursor.getColumnIndex(COLUMN_MEANNUMBER)));

        AssistTransaction tr = new AssistTransaction();
        tr.setResult(result);
        tr.setId(cursor.getLong(cursor.getColumnIndex(BaseColumns._ID)));
        tr.setMerchantID(cursor.getString(cursor.getColumnIndex(COLUMN_ORDER_MID)));
        tr.setOrderDateUTC(cursor.getString(cursor.getColumnIndex(COLUMN_ORDER_DATE_UTC)));
        tr.setOrderDateDevice(cursor.getString(cursor.getColumnIndex(COLUMN_ORDER_DATE_DEVICE)));
        tr.setOrderNumber(cursor.getString(cursor.getColumnIndex(COLUMN_ORDER_NUMBER)));
        tr.setOrderAmount(cursor.getString(cursor.getColumnIndex(COLUMN_ORDER_AMOUNT)));
        tr.setOrderComment(cursor.getString(cursor.getColumnIndex(COLUMN_ORDER_COMMENT)));
        tr.setUserSignature(cursor.getBlob(cursor.getColumnIndex(COLUMN_USER_SIGNATURE)));
        Currency c = Currency.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_ORDER_CURRENCY)));
        tr.setOrderCurrency(c);
        int index = cursor.getColumnIndex(COLUMN_ORDER_ITEMS_JSON);
        if (!cursor.isNull(index)) {
            try {
                tr.setOrderItems(AssistOrderUtils.fromJsonString(cursor.getString(index)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        AssistTransaction.PaymentMethod pm = AssistTransaction.PaymentMethod.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_PAYMENT_METHOD)));
        tr.setPaymentMethod(pm);
        if (cursor.getInt(cursor.getColumnIndex(COLUMN_USER_SIGNATURE_REQUIREMENT)) != 0) {
            tr.setRequireUserSignature(true);
        }
        return tr;
    }

    class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(DB_CREATE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion != newVersion) {
                db.delete(DB_TABLE_TRANSACTIONS, null, null);
                db.execSQL(DB_DROP_TABLE);
                db.execSQL(DB_CREATE);
            }
        }
    }
}
