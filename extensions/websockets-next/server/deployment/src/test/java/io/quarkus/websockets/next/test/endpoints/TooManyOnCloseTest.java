package io.quarkus.websockets.next.test.endpoints;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketServerException;

public class TooManyOnCloseTest {

    @RegisterExtension
    public static final QuarkusUnitTest test = new QuarkusUnitTest()
            .withApplicationRoot(root -> {
                root.addClasses(TooManyOnClose.class);
            })
            .setExpectedException(WebSocketServerException.class);

    @Test
    void verifyThatEndpointWithMultipleOnCloseMethodsFailsToDeploy() {

    }

    @WebSocket("/ws")
    public static class TooManyOnClose {
        @OnOpen
        public void onOpen() {
        }

        @OnClose
        public void onClose() {
        }

        @OnClose
        public void onClose2() {
        }
    }

}
