package io.quarkus.websockets.next.test.endpoints;

import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnMessage;
import io.quarkus.websockets.next.WebSocket;
import jakarta.enterprise.inject.spi.DeploymentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class NoOnOpenOrOnMessageInSubEndpointTest {

    @RegisterExtension
    public static final QuarkusUnitTest test = new QuarkusUnitTest()
            .withApplicationRoot(root -> {
                root.addClasses(ParentEndpoint.class, ParentEndpoint.SubEndpointWithoutOnOpenAndOnMessage.class);
            })
            .setExpectedException(DeploymentException.class);

    @Test
    void verifyThatSubEndpointWithoutOnOpenOrOnMessageFailsToDeploy() {

    }

    @WebSocket("/ws")
    public static class ParentEndpoint {

        @OnMessage
        public void onMessage(String message) {
            // Ignored.
        }

        @WebSocket("/sub")
        public static class SubEndpointWithoutOnOpenAndOnMessage {

            @OnClose
            public void onClose() {
                // Ignored.
            }

        }

    }

}
