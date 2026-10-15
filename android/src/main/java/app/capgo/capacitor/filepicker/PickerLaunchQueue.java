package app.capgo.capacitor.filepicker;

import android.content.ActivityNotFoundException;
import com.getcapacitor.PluginCall;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Serializes picker activity launches so only one Capacitor saved activity call is active.
 * All queue mutations and launches run on the main thread.
 */
final class PickerLaunchQueue {

    static final class PendingPickerLaunch {

        final PluginCall call;
        final IntentHolder intent;
        final String callbackName;
        final boolean truncateResultsToLimit;

        PendingPickerLaunch(PluginCall call, android.content.Intent intent, String callbackName, boolean truncateResultsToLimit) {
            this.call = call;
            this.intent = new IntentHolder(intent);
            this.callbackName = callbackName;
            this.truncateResultsToLimit = truncateResultsToLimit;
        }

        android.content.Intent getIntent() {
            return intent.get();
        }
    }

    /**
     * Holds a copy of the intent so queued launches are not affected if callers mutate the original.
     */
    private static final class IntentHolder {

        private final android.content.Intent intent;

        IntentHolder(android.content.Intent intent) {
            this.intent = new android.content.Intent(intent);
        }

        android.content.Intent get() {
            return intent;
        }
    }

    interface MainThreadScheduler {
        void run(Runnable runnable);
    }

    /**
     * @return true when the activity was launched and the call is retained for the result callback
     */
    interface ActivityLauncher {
        boolean launch(PendingPickerLaunch launch);
    }

    private final MainThreadScheduler mainThreadScheduler;
    private final ActivityLauncher activityLauncher;
    private final Queue<PendingPickerLaunch> pendingPickerLaunches = new ArrayDeque<>();
    private PendingPickerLaunch activePickerLaunch;

    PickerLaunchQueue(MainThreadScheduler mainThreadScheduler, ActivityLauncher activityLauncher) {
        this.mainThreadScheduler = mainThreadScheduler;
        this.activityLauncher = activityLauncher;
    }

    void enqueue(PendingPickerLaunch launch) {
        mainThreadScheduler.run(() -> enqueueOnMainThread(launch));
    }

    void completeActive() {
        mainThreadScheduler.run(this::completeActiveOnMainThread);
    }

    PendingPickerLaunch getActiveLaunch() {
        return activePickerLaunch;
    }

    boolean isTruncateResultsForCall(PluginCall call) {
        PendingPickerLaunch active = activePickerLaunch;
        if (active != null && active.call == call) {
            return active.truncateResultsToLimit;
        }
        return false;
    }

    private void enqueueOnMainThread(PendingPickerLaunch launch) {
        if (activePickerLaunch != null) {
            pendingPickerLaunches.add(launch);
            return;
        }
        startPickerLaunch(launch);
    }

    private void completeActiveOnMainThread() {
        activePickerLaunch = null;
        startNextQueuedLaunch();
    }

    private void startNextQueuedLaunch() {
        PendingPickerLaunch next = pendingPickerLaunches.poll();
        if (next != null) {
            startPickerLaunch(next);
        }
    }

    private void startPickerLaunch(PendingPickerLaunch launch) {
        boolean launched;
        try {
            launched = activityLauncher.launch(launch);
        } catch (ActivityNotFoundException e) {
            launch.call.reject("Unable to open file picker", e);
            startNextQueuedLaunch();
            return;
        }

        if (!launched) {
            startNextQueuedLaunch();
            return;
        }

        activePickerLaunch = launch;
    }
}
