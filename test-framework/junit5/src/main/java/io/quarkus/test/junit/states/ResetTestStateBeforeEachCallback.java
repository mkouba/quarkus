package io.quarkus.test.junit.states;

import org.jboss.logging.Logger;

import io.quarkus.test.junit.ResetTestState.Mode;
import io.quarkus.test.junit.callback.QuarkusTestBeforeEachCallback;
import io.quarkus.test.junit.callback.QuarkusTestMethodContext;

public class ResetTestStateBeforeEachCallback extends ResetTestStateCallback implements QuarkusTestBeforeEachCallback {

    private static final Logger LOG = Logger.getLogger(ResetTestStateBeforeEachCallback.class);

    @Override
    public void beforeEach(QuarkusTestMethodContext context) {
        if (resetIfNeeded(context)) {
            LOG.infof(
                    "Reset to initial state before %s#%s()", context.getTestInstance().getClass().getName(),
                    context.getTestMethod().getName());
        }
    }

    @Override
    boolean testMode(Mode mode) {
        return mode == Mode.BEFORE || mode == Mode.BEFORE_AFTER;
    }

}
