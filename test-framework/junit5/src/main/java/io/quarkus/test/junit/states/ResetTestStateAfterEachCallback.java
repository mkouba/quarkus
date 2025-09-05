package io.quarkus.test.junit.states;

import org.jboss.logging.Logger;

import io.quarkus.test.junit.ResetTestState.Mode;
import io.quarkus.test.junit.callback.QuarkusTestAfterEachCallback;
import io.quarkus.test.junit.callback.QuarkusTestMethodContext;

public class ResetTestStateAfterEachCallback extends ResetTestStateCallback implements QuarkusTestAfterEachCallback {

    private static final Logger LOG = Logger.getLogger(ResetTestStateAfterEachCallback.class);

    @Override
    public void afterEach(QuarkusTestMethodContext context) {
        if (resetIfNeeded(context)) {
            LOG.infof("Reset to initial state after %s#%s()", context.getTestInstance().getClass().getName(),
                    context.getTestMethod().getName());
        }
    }

    @Override
    boolean testMode(Mode mode) {
        return mode == Mode.AFTER || mode == Mode.BEFORE_AFTER;
    }

}
