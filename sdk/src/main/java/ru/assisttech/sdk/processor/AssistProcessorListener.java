package ru.assisttech.sdk.processor;

import android.app.Activity;

import ru.assisttech.sdk.AssistResult;
import ru.assisttech.sdk.storage.AssistTransaction;

/**
 * Callbacks for AssistProcessor {@link AssistBaseProcessor}
 */
public interface AssistProcessorListener {

    /**
     * Called when AssistProcessor finished without errors.
     *
     * @param id     transaction ID {@link AssistTransaction}
     * @param result result given by AssistProcessor
     */
    void onFinished(long id, AssistResult result);

    /**
     * Called when error encountered by AssistProcessor
     *
     * @param id      transaction ID {@link AssistTransaction}
     * @param message error description.
     */
    void onError(long id, String message);

    /**
     * Called when there is network error
     *
     * @param id      transaction ID {@link AssistTransaction}
     * @param message error description.
     */
    void onNetworkError(long id, String message);

    /**
     * Called when AssistProcessor was stopped (e.g. by user).
     *
     * @param id transaction ID {@link AssistTransaction}
     */
    void onTerminated(long id);

    /**
     * Called when AssistProcessor creates new top Activity.
     *
     * @param activity new created Activity
     */
    void onActivityCreated(Activity activity);
}
