package io.quarkus.arc.test.producer.generic;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.arc.test.ArcTestContainer;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

public class GenericProducerHierarchyTest {

    @Rule
    public ArcTestContainer container = new ArcTestContainer(Producer.class, Registry.class);

    @Test
    public void testPrimitiveProducers() {
        InstanceHandle<Registry> ri = Arc.container().instance(Registry.class);
        Registry registry = ri.get();
        Assert.assertNotNull(registry.longProducer.apply((l, s) -> System.out.println(l + s)));
        Assert.assertNotNull(registry.doubleProducer.apply((l, s) -> System.out.println(l + s)));
    }

    @Singleton
    static class Producer {

        @Produces
        public Produced<Long, String> produceLong() {
            return c -> Optional.of(10);
        }

        @Produces
        public Produced<Double, Long> produceDouble() {
            return c -> Optional.of(-10);
        }
    }

    interface Produced<T, R> extends Function<BiConsumer<T, R>, Optional<Integer>> {
    }

    @Singleton
    static class Registry {
        @Inject
        Function<BiConsumer<Long, String>, Optional<Integer>> longProducer;
        @Inject
        Function<BiConsumer<Double, String>, Optional<Integer>> doubleProducer;
    }
}
