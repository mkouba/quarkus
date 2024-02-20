package io.quarkus.websockets.next.test.endpoints;

import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.websockets.next.OnMessage;
import io.quarkus.websockets.next.WebSocket;
import jakarta.enterprise.inject.spi.DeploymentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class TooManyOnMessageInSubEndpointTest {

    @RegisterExtension
    public static final QuarkusUnitTest test = new QuarkusUnitTest()
            .withApplicationRoot(root -> {
                root.addClasses(ParentEndpoint.class, ParentEndpoint.SubEndpointWithTooManyOnMessage.class);
            })
            .setExpectedException(DeploymentException.class);

    @Test
    void verifyThatSubEndpointWithoutTooManyOnMessageFailsToDeploy() {

    }

    @WebSocket("/ws")
    public static class ParentEndpoint {

        @OnMessage
        public void onMessage(String message) {
            // Ignored.
        }

        @WebSocket("/sub")
        public static class SubEndpointWithTooManyOnMessage {

            @OnMessage
            public void onMessage(String message) {
                // Ignored.
            }

            @OnMessage
            public void onMessage2(String message) {
                // Ignored.
            }
        }

    }

}
