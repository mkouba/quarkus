package io.quarkus.websockets.next.test.endpoints;

import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.WebSocket;
import jakarta.enterprise.inject.spi.DeploymentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class NoOnOpenOrOnMessageTest {

    @RegisterExtension
    public static final QuarkusUnitTest test = new QuarkusUnitTest()
            .withApplicationRoot(root -> {
                root.addClasses(NoOnOpenOrOnMessage.class);
            })
            .setExpectedException(DeploymentException.class);

    @Test
    void verifyThatEndpointWithoutOnMessageOrOnOpenFailsToDeploy() {

    }

    @WebSocket("/ws")
    public static class NoOnOpenOrOnMessage {

        // Invalid endpoint, must have at least one @OnOpen or @OnMessage method.

        @OnClose
        public void onClose() {
        }


    }

}
