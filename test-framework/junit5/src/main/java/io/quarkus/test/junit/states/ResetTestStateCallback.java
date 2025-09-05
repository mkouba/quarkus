package io.quarkus.test.junit.states;

import org.jboss.logging.Logger;

import io.quarkus.test.junit.ResetTestState;
import io.quarkus.test.junit.callback.QuarkusTestMethodContext;

abstract class ResetTestStateCallback {

    private static final Logger LOG = Logger.getLogger(ResetTestStateAfterEachCallback.class);

    boolean resetIfNeeded(QuarkusTestMethodContext context) {
        ResetTestState annotation = context.getTestMethod().getAnnotation(ResetTestState.class);
        if (annotation != null
                && testMode(annotation.value())) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            try {
                // We need to use reflection here because quarkus-junit5 does not depend on ArC
                Class<?> arcTestStateImplClass = cl.loadClass("io.quarkus.arc.runtime.test.TestStates");
                arcTestStateImplClass.getDeclaredMethod("reset").invoke(null);
                return true;
            } catch (ClassNotFoundException e) {
                LOG.warn("@ResetTestStates used but the CDI container is not available");
            } catch (Exception e) {
                LOG.error("Unable to reset test states", e);
            }
        }
        return false;
    }

    abstract boolean testMode(ResetTestState.Mode mode);

}
