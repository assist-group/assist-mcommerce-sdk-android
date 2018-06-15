package ru.assisttech.sdk.storage;

import android.database.Cursor;

import ru.assisttech.sdk.AssistResult;

public interface AssistTransactionStorage {

    int ERROR = -1;

    long add(AssistTransaction t);

    void updateTransactionSignature(long id, byte[] signature);

    void updateTransactionOrderNumber(long id, String on);

    void updateTransactionResult(long id, AssistResult result);

    Cursor getData();

    Cursor getData(String orderNumber);

    Cursor getDataWithNoFiltration(String orderNumber);

    AssistTransaction getTransaction(long id);

    AssistTransactionFilter getFilter();

    void setFilter(AssistTransactionFilter filter);

    void resetFilter();

    int deleteTransactions(AssistTransactionFilter filter);

    int deleteTransaction(long id);

    AssistTransaction transactionFromCursor(Cursor cursor);
}
