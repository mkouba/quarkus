package io.quarkus.grpc.client.retry;

import java.util.concurrent.atomic.AtomicInteger;

import io.grpc.examples.helloworld.Greeter;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;

@GrpcService
public class HelloService implements Greeter {

    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public Uni<HelloReply> sayHello(HelloRequest request) {
        if (counter.incrementAndGet() < 2) {
            throw new IllegalStateException();
        }
        return Uni.createFrom().item(HelloReply.newBuilder().setMessage("OK:" + counter.get()).build());
    }

}
