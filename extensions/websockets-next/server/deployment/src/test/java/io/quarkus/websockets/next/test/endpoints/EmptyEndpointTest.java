package io.quarkus.websockets.next.test.endpoints;

import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.websockets.next.WebSocket;
import jakarta.enterprise.inject.spi.DeploymentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class EmptyEndpointTest {

    @RegisterExtension
    public static final QuarkusUnitTest test = new QuarkusUnitTest()
            .withApplicationRoot(root -> {
                root.addClasses(EmptyEndpoint.class);
            })
            .setExpectedException(DeploymentException.class);

    @Test
    void verifyThatEndpointWithoutAnyMethodFailsToDeploy() {

    }

    @WebSocket("/ws")
    public static class EmptyEndpoint {

    }

}
