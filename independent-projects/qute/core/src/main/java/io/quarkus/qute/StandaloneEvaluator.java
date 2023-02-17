package io.quarkus.qute;

import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * Evaluates standalone expressions.
 */
public interface StandaloneEvaluator {

    CompletionStage<Object> evaluate(String value);

    CompletionStage<Object> evaluate(String value, Object data);

    CompletionStage<Object> evaluate(String value, Object data, Map<String, Object> attributes);

}
