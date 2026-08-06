package io.quarkus.it.quartz;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.quarkus.scheduler.Scheduler;
import io.quarkus.scheduler.SchedulerTest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@SchedulerTest
public class SchedulerTestInterceptorTestCase {

    @Inject
    Scheduler scheduler;

    @InjectMock
    Greeter greeter;

    @Inject
    Poller poller;

    @Inject
    SchedulerPauseTracker tracker;

    @Test
    public void testSchedulerPausedAndResumed() {
        Mockito.when(greeter.greet()).thenReturn("mocked-hello");

        assertTrue(tracker.wasPaused());
        assertTrue(tracker.wasResumed());
        assertTrue(scheduler.isRunning());

        int before = poller.getGreetings().size();
        await().atMost(Duration.ofSeconds(3)).until(() -> poller.getGreetings().size() > before);

        List<String> newEntries = poller.getGreetings().subList(before, poller.getGreetings().size());
        for (String greeting : newEntries) {
            assertEquals("mocked-hello", greeting);
        }
    }
}
