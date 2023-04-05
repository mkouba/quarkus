package io.quarkus.qute;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AsyncThreadTest {

    private static ExecutorService executor;

    @BeforeAll
    static void initExecutor() {
        executor = Executors.newSingleThreadExecutor(new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(Thread.currentThread().getThreadGroup(), r,
                        "AsyncThreadTest",
                        0);
            }
        });
    }

    @AfterAll
    static void shutdownExecutor() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    @Test
    public void testAsyncData() {
        Engine engine = Engine.builder()
                .addDefaults()
                .addNamespaceResolver(NamespaceResolver.builder("thread").resolve(ec -> {
                    if (ec.getName().equals("name")) {
                        return Thread.currentThread().getName();
                    }
                    return Results.notFound();
                }).build())
                .addValueResolver(ValueResolver.builder().applyToBaseClass(Client.class)
                        .applyToName("tokens").resolveSync(ec -> ((Client) ec.getBase()).getTokens()).build())
                .build();
        Template template = engine.parse("{thread:name}::{#each client.tokens}{it} ({thread:name}){/each}");
        String threadName = Thread.currentThread().getName();
        assertEquals(threadName + "::AsyncThreadTest (" + threadName + ")", template.data("client", new Client()).render());
    }

    static class Client {

        public CompletionStage<List<String>> getTokens() {
            CompletableFuture<List<String>> tokens = new CompletableFuture<>();
            executor.execute(() -> {
                tokens.complete(List.of(Thread.currentThread().getName()));
            });
            return tokens;
        }

    }

}
