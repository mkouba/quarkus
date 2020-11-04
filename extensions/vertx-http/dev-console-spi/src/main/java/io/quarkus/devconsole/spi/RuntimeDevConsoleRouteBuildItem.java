package io.quarkus.devconsole.spi;

import java.util.function.Consumer;

import io.quarkus.builder.item.MultiBuildItem;
import io.quarkus.dev.console.DevConsoleRequest;

/**
 * A route for handling requests in the dev console.
 *
 * Routes are registered under /@dev/{groupId}.{artifactId}/
 * 
 * This handler executes in the runtime class loader.
 * 
 * As the runtime part may not have a dependency on Vert.x Web this uses the
 * DevConsoleRequest as the HTTP abstraction.
 *
 */
public final class RuntimeDevConsoleRouteBuildItem extends MultiBuildItem {

    private final String groupId;
    private final String artifactId;
    private final String path;
    private final String method;
    private final Consumer<DevConsoleRequest> handler;

    public RuntimeDevConsoleRouteBuildItem(String groupId, String artifactId, String path, String method,
            Consumer<DevConsoleRequest> handler) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.path = path;
        this.method = method;
        this.handler = handler;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

    public Consumer<DevConsoleRequest> getHandler() {
        return handler;
    }

}
