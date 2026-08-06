package io.quarkus.it.quartz;

import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import io.quarkus.scheduler.SchedulerPaused;
import io.quarkus.scheduler.SchedulerResumed;

@ApplicationScoped
public class SchedulerPauseTracker {

    private final AtomicBoolean pausedEventFired = new AtomicBoolean();
    private final AtomicBoolean resumedEventFired = new AtomicBoolean();

    void onPause(@Observes SchedulerPaused e) {
        pausedEventFired.set(true);
    }

    void onResume(@Observes SchedulerResumed e) {
        resumedEventFired.set(true);
    }

    public boolean wasPaused() {
        return pausedEventFired.get();
    }

    public boolean wasResumed() {
        return resumedEventFired.get();
    }
}
