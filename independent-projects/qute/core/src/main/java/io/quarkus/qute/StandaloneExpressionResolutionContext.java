package io.quarkus.qute;

import java.util.Map;
import java.util.concurrent.CompletionStage;

final class StandaloneExpressionResolutionContext implements ResolutionContext {

    private final Map<String, Object> attributes;
    private final Object data;
    private final Evaluator evaluator;

    public StandaloneExpressionResolutionContext(Evaluator evaluator, Object data, Map<String, Object> attributes) {
        this.evaluator = evaluator;
        this.data = data;
        this.attributes = attributes;
    }

    @Override
    public CompletionStage<Object> evaluate(String value) {
        return evaluate(ExpressionImpl.from(value));
    }

    @Override
    public CompletionStage<Object> evaluate(Expression expression) {
        return evaluator.evaluate(expression, this);
    }

    @Override
    public ResolutionContext createChild(Object data, Map<String, SectionBlock> extendingBlocks) {
        throw new IllegalStateException();
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public ResolutionContext getParent() {
        return null;
    }

    @Override
    public SectionBlock getExtendingBlock(String name) {
        return null;
    }

    @Override
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    @Override
    public Evaluator getEvaluator() {
        throw new IllegalStateException();
    }

}
