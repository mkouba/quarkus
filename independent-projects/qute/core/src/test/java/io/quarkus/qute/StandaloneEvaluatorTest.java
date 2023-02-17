package io.quarkus.qute;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.junit.jupiter.api.Test;

import io.smallrye.mutiny.Uni;

public class StandaloneEvaluatorTest {

    @Test
    public void testEvaluation() {
        StandaloneEvaluator evaluator = Qute.engine().getStandaloneEvaluator();

        CompletionStage<Object> r1 = evaluator.evaluate("foo.bar", Map.of("foo", new Foo()));
        assertEquals(Boolean.TRUE, Uni.createFrom().completionStage(r1).await().indefinitely());

        CompletionStage<Object> r2 = evaluator.evaluate(" foo.bar ?: 'Moon'");
        assertEquals("Moon", Uni.createFrom().completionStage(r2).await().indefinitely());

        Foo foo3 = new Foo();
        CompletionStage<Object> r3 = evaluator.evaluate("foo", Map.of("foo", foo3));
        assertEquals(foo3, Uni.createFrom().completionStage(r3).await().indefinitely());
    }

    public static class Foo {

        public boolean getBar() {
            return true;
        }

    }

}
