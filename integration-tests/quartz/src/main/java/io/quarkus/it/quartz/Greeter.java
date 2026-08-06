package io.quarkus.it.quartz;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Greeter {

    public String greet() {
        return "real-hello";
    }
}
