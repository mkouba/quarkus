package io.quarkus.arc.test.interceptors.parameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;

import org.junit.Rule;
import org.junit.Test;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InjectParameters;
import io.quarkus.arc.InjectParameters.Skip;
import io.quarkus.arc.MethodParamsInjectionInterceptor;
import io.quarkus.arc.test.ArcTestContainer;

public class InjectMethodParametersTest {

    @Rule
    public ArcTestContainer container = new ArcTestContainer(Foo.class, Bar.class, InjectParameters.class,
            MethodParamsInjectionInterceptor.class);

    @Test
    public void testInjection() {
        Bar bar = Arc.container().instance(Bar.class).get();
        assertFalse(bar.ping(null, null, "ignored", Collections.singletonList("yes")));
        assertEquals(2, Foo.DESTROYS.get());
    }

    @Dependent
    static class Foo {

        static final AtomicInteger DESTROYS = new AtomicInteger(0);

        String id;

        @PostConstruct
        void init() {
            this.id = UUID.randomUUID().toString();
        }

        @PreDestroy
        void destroy() {
            DESTROYS.incrementAndGet();
        }

    }

    @ApplicationScoped
    static class Bar {

        @InjectParameters
        boolean ping(Foo foo1, Foo foo2, String shouldBeNull, @Skip List<String> hellos) {
            assertNull(shouldBeNull);
            return foo1.id.equals(foo2.id) && hellos.contains("yes");
        }

    }

}
