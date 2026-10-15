package app.capgo.capacitor.filepicker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import com.getcapacitor.PluginCall;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class PickerLaunchQueueTest {

    private final List<ExecutorService> executors = new ArrayList<>();

    @After
    public void tearDown() {
        for (ExecutorService executor : executors) {
            executor.shutdownNow();
        }
    }

    @Test
    public void serializesLaunchesUntilComplete() {
        List<String> launched = new ArrayList<>();
        PickerLaunchQueue queue = newQueue(
            launch -> {
                launched.add(launch.callbackName);
                return true;
            },
            Runnable::run
        );

        queue.enqueue(launch("first", "pickMediaResult"));
        queue.enqueue(launch("second", "pickMediaResult"));

        assertEquals(1, launched.size());
        assertEquals("pickMediaResult", launched.get(0));
        assertEquals("first", queue.getActiveLaunch().call.getCallbackId());

        queue.completeActive();

        assertEquals(2, launched.size());
        assertEquals("second", queue.getActiveLaunch().call.getCallbackId());
    }

    @Test
    public void failedLaunchRejectsAndStartsNextQueued() {
        AtomicInteger attempts = new AtomicInteger();
        PluginCall failedCall = mockCall("failed");
        PluginCall nextCall = mockCall("next");

        PickerLaunchQueue queue = newQueue(
            launch -> {
                if (attempts.incrementAndGet() == 1) {
                    throw new ActivityNotFoundException("No picker");
                }
                return true;
            },
            Runnable::run
        );

        queue.enqueue(new PickerLaunchQueue.PendingPickerLaunch(failedCall, new Intent(), "cb", false));
        queue.enqueue(new PickerLaunchQueue.PendingPickerLaunch(nextCall, new Intent(), "cb", false));

        verify(failedCall).reject(eq("Unable to open file picker"), any(ActivityNotFoundException.class));
        assertEquals(nextCall, queue.getActiveLaunch().call);
    }

    @Test
    public void launcherReturningFalseAdvancesWithoutActiveLaunch() {
        PluginCall first = mockCall("first");
        PluginCall second = mockCall("second");
        AtomicInteger attempts = new AtomicInteger();

        PickerLaunchQueue queue = newQueue(
            launch -> {
                attempts.incrementAndGet();
                return launch.call == second;
            },
            Runnable::run
        );

        queue.enqueue(new PickerLaunchQueue.PendingPickerLaunch(first, new Intent(), "cb", false));
        queue.enqueue(new PickerLaunchQueue.PendingPickerLaunch(second, new Intent(), "cb", false));

        assertEquals(2, attempts.get());
        assertEquals(second, queue.getActiveLaunch().call);
    }

    @Test
    public void truncateFlagFollowsActiveLaunch() {
        PluginCall call = mockCall("active");
        PickerLaunchQueue queue = newQueue(launch -> true, Runnable::run);

        queue.enqueue(new PickerLaunchQueue.PendingPickerLaunch(call, new Intent(), "cb", true));

        assertTrue(queue.isTruncateResultsForCall(call));
        assertFalse(queue.isTruncateResultsForCall(mockCall("other")));
    }

    @Test
    public void concurrentEnqueuesOnlyStartOneLaunch() throws Exception {
        AtomicInteger launchCount = new AtomicInteger();
        CountDownLatch startSecond = new CountDownLatch(1);
        ExecutorService background = Executors.newFixedThreadPool(4);
        executors.add(background);

        PickerLaunchQueue queue = new PickerLaunchQueue(
            runnable -> background.execute(runnable),
            launch -> {
                launchCount.incrementAndGet();
                startSecond.countDown();
                return true;
            }
        );

        for (int i = 0; i < 8; i++) {
            final int index = i;
            background.execute(() -> queue.enqueue(launch("call-" + index, "pickFilesResult")));
        }

        assertTrue(startSecond.await(5, TimeUnit.SECONDS));
        Thread.sleep(200);
        assertEquals(1, launchCount.get());
    }

    private static PickerLaunchQueue newQueue(PickerLaunchQueue.ActivityLauncher launcher, PickerLaunchQueue.MainThreadScheduler scheduler) {
        return new PickerLaunchQueue(scheduler, launcher);
    }

    private static PickerLaunchQueue.PendingPickerLaunch launch(String callbackId, String callbackName) {
        return new PickerLaunchQueue.PendingPickerLaunch(mockCall(callbackId), new Intent(), callbackName, false);
    }

    private static PluginCall mockCall(String callbackId) {
        PluginCall call = mock(PluginCall.class);
        when(call.getCallbackId()).thenReturn(callbackId);
        return call;
    }
}
