package io.quarkus.datasource.test;

import io.quarkus.datasource.runtime.DatabaseSchemaProvider;
import io.quarkus.runtime.test.TestState;
import io.smallrye.common.annotation.Experimental;

/**
 * A bean implementation is provided in the test mode.
 *
 * @see io.quarkus.runtime.LaunchMode#TEST
 */
@Experimental("This API is experimental and may change in the future")
public interface DatasourceTestState extends TestState {

    @Override
    default String extensionId() {
        return "io.quarkus:quarkus-datasource";
    }

    /**
     * Reset all databases.
     *
     * @see DatabaseSchemaProvider#resetAllDatabases()
     */
    @Override
    void reset();

}
