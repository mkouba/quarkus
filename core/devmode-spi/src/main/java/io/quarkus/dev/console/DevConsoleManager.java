package io.quarkus.dev.console;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;

public class DevConsoleManager {

    private static volatile Consumer<DevConsoleRequest> handler;
    private static volatile Function<String, Object> resolver;

    public static void registerHandler(Consumer<DevConsoleRequest> requestHandler) {
        handler = requestHandler;
    }

    public static void sentRequest(DevConsoleRequest request) {
        Consumer<DevConsoleRequest> handler = DevConsoleManager.handler;
        if (handler == null) {
            request.getResponse().complete(new DevConsoleResponse(503, Collections.emptyMap(), new byte[0])); //service unavailable
        } else {
            handler.accept(request);
        }
    }

    public static Object resolve(String name) {
        return resolver.apply(name);
    }

    public static void setResolver(Function<String, Object> resolver) {
        DevConsoleManager.resolver = resolver;
    }
}
