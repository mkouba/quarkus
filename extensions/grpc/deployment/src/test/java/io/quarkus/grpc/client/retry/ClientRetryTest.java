package io.quarkus.grpc.client.retry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.grpc.examples.helloworld.Greeter;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.QuarkusUnitTest;

public class ClientRetryTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class)
                    .addPackage(GreeterGrpc.class.getPackage()).addClasses(HelloService.class))
            .withConfigurationResource("hello-config-retry.properties");

    @GrpcClient("hello-service")
    Greeter service;

    @Test
    public void testRetry() {
        HelloReply reply = service.sayHello(HelloRequest.newBuilder().setName("RETRY!").build()).await()
                .atMost(Duration.ofSeconds(5));
        assertEquals("OK:3", reply.getMessage());
    }

}
