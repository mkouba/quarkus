package io.quarkus.runtime.test;

import io.smallrye.common.annotation.Experimental;

/**
 * Extensions provide CDI beans that implement this interface in order to allow developers to reset to the initial state in the
 * test mode.
 * <p>
 * Test states can be injected in a {@code io.quarkus.test.junit.QuarkusTest} and invoked in {@code @BeforeEach} and
 * {@code @AfterEach} callbacks, or directly in a test method.
 * <p>
 * An idiomatic code to reset all available states may look like:
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
 * It is also possible to inject specific implementations:
 *
 * <pre>
 * <code>
 * import io.quarkus.datasource.test.DatasourceTestState;
 * import jakarta.inject.Inject;
 * import org.junit.jupiter.api.BeforeEach;
 *
 * class MyTest {
 *
 *     {@literal @Inject}
 *     DatasourceTestState db;
 *
 *     {@literal @BeforeEach}
 *     void beforeEach() {
 *        db.reset(); // reset all databases
 *     }
 *  }
 *  </code>
 * </pre>
 *
 * @see io.quarkus.runtime.LaunchMode#TEST
 */
@Experimental("This API is experimental and may change in the future")
public interface TestState {

    /**
     * @return {@code groupId:artifactId} of the extension runtime module
     */
    String extensionId();

    /**
     * Reset to initial state.
     */
    void reset();

}
