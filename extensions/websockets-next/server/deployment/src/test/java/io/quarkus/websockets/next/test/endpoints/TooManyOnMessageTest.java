package io.quarkus.websockets.next.test.endpoints;

import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.websockets.next.OnMessage;
import io.quarkus.websockets.next.WebSocket;
import jakarta.enterprise.inject.spi.DeploymentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class TooManyOnMessageTest {

    @RegisterExtension
    public static final QuarkusUnitTest test = new QuarkusUnitTest()
            .withApplicationRoot(root -> {
                root.addClasses(TooManyOnMessage.class);
            })
            .setExpectedException(DeploymentException.class);

    @Test
    void verifyThatEndpointWithMultipleOnMessageMethodsFailsToDeploy() {

    }

    @WebSocket("/ws")
    public static class TooManyOnMessage {
        @OnMessage
        public void onMessage(String message) {
        }

        @OnMessage
        public void onMessage2(String message) {
        }
    }

}
