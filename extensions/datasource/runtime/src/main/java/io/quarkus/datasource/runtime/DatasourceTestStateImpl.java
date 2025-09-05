package io.quarkus.datasource.runtime;

import java.util.ServiceLoader;

import jakarta.enterprise.context.Dependent;

import org.jboss.logging.Logger;

import io.quarkus.datasource.test.DatasourceTestState;

@Dependent
public class DatasourceTestStateImpl implements DatasourceTestState {

    private static final Logger LOG = Logger.getLogger(DatasourceTestStateImpl.class);

    @Override
    public void reset() {
        LOG.info("Resetting datasource state");
        ServiceLoader<DatabaseSchemaProvider> providers = ServiceLoader.load(DatabaseSchemaProvider.class,
                Thread.currentThread().getContextClassLoader());
        for (DatabaseSchemaProvider p : providers) {
            try {
                p.resetAllDatabases();
            } catch (Exception e) {
                LOG.errorf(e, "%s is unable to reset all databases", p.getClass().getName());
            }
        }
    }

}
