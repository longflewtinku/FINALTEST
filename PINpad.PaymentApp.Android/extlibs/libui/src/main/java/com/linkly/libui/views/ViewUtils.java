package com.linkly.libui.views;

import android.view.View;

import timber.log.Timber;

/**
 * Tidying of code and logic around View handling in preparation for a Kotlin migration (which
 *  could easily re-implement the same utils).
 */
public class ViewUtils {
    private ViewUtils() {}

    /**
     * A common pattern across the apps is to suppress NPEs when using findViewById and continue
     *  if not found, with no effort to log the error. If the error is never going to happen then
     *  why do the null check? It turns out that some edge cases will yield unexpected View NPEs,
     *  if those take place we are avoiding a crash but the state of the app will be undesirable and
     *  no reporting of the NPE is being done. So use this instead!
     *  In Kotlin, we'll use requireView() a lot and along with Kotlin's null handling the related
     *  code will get even neater.
     * @param haystack root view being searched in.
     * @param resId Needle's resource ID.
     * @param notFoundLogMessage Message to log if view not found. Make sure the message is useful.
     * @param operation Lambda to hold code that would have previously been in the if block, i.e.
     *                  code that expects the returned View to not be null.
     * @param <V> Type of View being searched for.
     */

    @SuppressWarnings("unchecked")
    public static <V> void findViewByIdAndRun(View haystack, int resId, String notFoundLogMessage, OnRunOperation<V> operation) {
        Timber.d("findViewByIdAndRun...notFoundLogMessage: %s", notFoundLogMessage);
        V view = (V) haystack.findViewById(resId);
        if (view == null) {
            Timber.e(notFoundLogMessage);
            return;
        }

        operation.run(view);
    }

    /**
     * The guarantee is that nonNullInput is never null.
     * Callers must ensure that nonNullInput is never null!
     */
    public interface OnRunOperation<V> {
         void run(V nonNullInput);
    }
}
