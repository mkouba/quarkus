package io.quarkus.websockets.next.test.connection;

import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.websockets.next.OnMessage;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketServerConnection;
import io.quarkus.websockets.next.test.utils.WSClient;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceConnectionScopeTest {

    @RegisterExtension
    public static final QuarkusUnitTest test = new QuarkusUnitTest()
            .withApplicationRoot(root -> {
                root.addClasses(MyEndpoint.class, WSClient.class);
            });
    @Inject MyEndpoint endpoint;
    @Inject Vertx vertx;

    @TestHTTPResource("/")
    URI baseUri;

    @Test
    void verifyThatConnectionIsNotAccessibleOutsideOfTheSessionScope() {
        endpoint.testConnectionNotAccessibleOutsideOfWsMethods();
    }

    @Test
    void verifyThatConnectionIsAccessibleInSessionScope() {
        WSClient client = WSClient.create(vertx);
        var resp = client.connect(WSClient.toWS(baseUri, "/ws"))
                        .sendAndAwaitReply("hello");
        assertThat(resp.toString()).isEqualTo("HELLO");
    }

    @WebSocket("/ws")
    public static class MyEndpoint {


        @Inject
        WebSocketServerConnection connection;

        @OnMessage
        public String onMessage(String message) {
            assertThat(CDI.current().getBeanContainer().isScope(SessionScoped.class)).isTrue();
            assertThat(CDI.current().getBeanContainer().isScope(RequestScoped.class)).isFalse();
            assertThat(connection).isNotNull();
            return message.toUpperCase();
        }

        @ActivateRequestContext
        void testConnectionNotAccessibleOutsideOfWsMethods() {
            assertThat(CDI.current().getBeanContainer().isScope(SessionScoped.class)).isFalse();
            assertThat(CDI.current().getBeanContainer().isScope(RequestScoped.class)).isTrue();
            assertThat(connection).isNull();
        }

    }


}
