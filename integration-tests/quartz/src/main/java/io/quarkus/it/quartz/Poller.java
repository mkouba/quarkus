package io.quarkus.it.quartz;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class Poller {

    @Inject
    Greeter greeter;

    private final CopyOnWriteArrayList<String> greetings = new CopyOnWriteArrayList<>();

    @Scheduled(every = "1s")
    void tick() {
        greetings.add(greeter.greet());
    }

    public List<String> getGreetings() {
        return greetings;
    }
}
