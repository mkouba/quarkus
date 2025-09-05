package io.quarkus.arc.runtime.test;

import java.util.List;

import jakarta.enterprise.context.Dependent;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.runtime.test.TestState;

public final class TestStates {

    private TestStates() {
    }

    // This method is called directly from the io.quarkus.test.junit.ResetTestStatesCallback
    public static void reset() {
        List<InstanceHandle<TestState>> testStates = Arc.container().listAll(TestState.class);
        for (InstanceHandle<TestState> handle : testStates) {
            handle.get().reset();
            if (Dependent.class.equals(handle.getBean().getScope())) {
                handle.destroy();
            }
        }
    }

}
