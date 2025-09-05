package io.quarkus.test.junit;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.smallrye.common.annotation.Experimental;

/**
 * If declared on a test method then the test state is reset in the phase derived from the current mode.
 * <p>
 * By default, it is functionally equivalent to the following code:
 *
 * <pre>
 * <code>
 * import io.quarkus.runtime.test.TestState;
 * import io.quarkus.arc.All;
 * import org.junit.jupiter.api.BeforeEach;
 *
 * class MyTest {
 *
 *     {@literal @All}
 *     List&lt;TestState&gt; testStates;
 *
 *     {@literal @BeforeEach}
 *     void beforeEach() {
 *        testStates.forEach(TestState::reset);
 *     }
 *  }
 *  </code>
 * </pre>
 *
 * @see io.quarkus.runtime.test.TestState
 */
@Retention(RUNTIME)
@Target(METHOD)
@Experimental("This API is experimental and may change in the future")
public @interface ResetTestState {

    /**
     * The mode defines when exactly are the test states reset.
     */
    Mode value() default Mode.BEFORE;

    enum Mode {
        /**
         * @see org.junit.jupiter.api.BeforeEach
         */
        BEFORE,
        /**
         * @see org.junit.jupiter.api.AfterEach
         */
        AFTER,
        /**
         * @see org.junit.jupiter.api.BeforeEach
         * @see org.junit.jupiter.api.AfterEach
         */
        BEFORE_AFTER
    }

}
