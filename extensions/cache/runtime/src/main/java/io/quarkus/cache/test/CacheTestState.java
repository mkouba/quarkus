package io.quarkus.cache.test;

import io.quarkus.runtime.test.TestState;
import io.smallrye.common.annotation.Experimental;

/**
 * A bean implementation is provided in the test mode.
 *
 * @see io.quarkus.runtime.LaunchMode#TEST
 */
@Experimental("This API is experimental and may change in the future")
public interface CacheTestState extends TestState {

    @Override
    default String extensionId() {
        return "io.quarkus:quarkus-cache";
    }

    /**
     * Remove all entries from all caches.
     */
    @Override
    void reset();

}
