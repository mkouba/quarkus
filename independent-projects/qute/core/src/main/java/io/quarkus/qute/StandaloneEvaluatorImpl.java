package io.quarkus.qute;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletionStage;

class StandaloneEvaluatorImpl implements StandaloneEvaluator {

    private final Evaluator evaluator;

    StandaloneEvaluatorImpl(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public CompletionStage<Object> evaluate(String value) {
        return evaluate(value, null);
    }

    @Override
    public CompletionStage<Object> evaluate(String value, Object data) {
        return evaluate(value, data, Collections.emptyMap());
    }

    @Override
    public CompletionStage<Object> evaluate(String value, Object data, Map<String, Object> attributes) {
        return new StandaloneExpressionResolutionContext(evaluator, data, attributes).evaluate(value);
    }

}
