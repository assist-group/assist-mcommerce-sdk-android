package ru.assisttech.sdk.engine;

import android.app.Activity;

import ru.assisttech.sdk.storage.AssistTransaction;

/**
 * Pay Engine callbacks
 */
public interface PayEngineListener {
    /**
     * Payment finished
     *
     * @param activity    Activity that started payment process or Activity created by PayEngine during payment.
     *                    Most likely top activity.
     * @param transaction current transaction {@link AssistTransaction}
     */
    void onFinished(Activity activity, AssistTransaction transaction);

    /**
     * Payment canceled by user
     *
     * @param activity    Activity that started payment process or Activity created by PayEngine during payment.
     *                    Most likely top activity.
     * @param transaction current transaction {@link AssistTransaction}
     */
    void onCanceled(Activity activity, AssistTransaction transaction);

    /**
     * Some error happened
     *
     * @param activity Activity that started payment process or Activity created by PayEngine during payment.
     *                 Most likely top activity.
     * @param info     failure description
     */
    void onFailure(Activity activity, String info);

    /**
     * Network error
     *
     * @param activity Activity that started payment process or Activity created by PayEngine during payment.
     *                 Most likely top activity.
     * @param message  error message
     */
    void onNetworkError(Activity activity, String message);
}
